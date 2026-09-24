package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BrandDatabase
import com.example.data.local.CampaignEntity
import com.example.data.local.MediumShotEntity
import com.example.data.model.MediumType
import com.example.data.remote.GeminiApiService
import com.example.data.repository.BrandRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BrandViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BrandDatabase.getInstance(application)
    private val geminiService = GeminiApiService(application)
    val repository = BrandRepository(application, database.brandDao(), geminiService)

    val allCampaigns: StateFlow<List<CampaignEntity>> = repository.allCampaigns
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeCampaignId = MutableStateFlow<Long?>(null)
    val activeCampaignId: StateFlow<Long?> = _activeCampaignId.asStateFlow()

    val activeCampaign: StateFlow<CampaignEntity?> = _activeCampaignId.flatMapLatest { id ->
        if (id != null) repository.observeCampaign(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeCampaignShots: StateFlow<List<MediumShotEntity>> = _activeCampaignId.flatMapLatest { id ->
        if (id != null) repository.observeShotsForCampaign(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatingProgressMessage = MutableStateFlow<String?>(null)
    val generatingProgressMessage: StateFlow<String?> = _generatingProgressMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hasApiKey = MutableStateFlow(geminiService.hasValidApiKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun refreshApiKeyStatus() {
        _hasApiKey.value = geminiService.hasValidApiKey()
    }

    fun saveCustomApiKey(key: String) {
        geminiService.saveCustomApiKey(key)
        refreshApiKeyStatus()
    }

    fun selectCampaign(id: Long?) {
        _activeCampaignId.value = id
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createCampaign(
        title: String,
        category: String,
        tagline: String,
        description: String,
        primaryColorHex: String,
        accentColorHex: String,
        materialFinish: String,
        autoGenerateMaster: Boolean = true,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val campaign = CampaignEntity(
                    title = title.trim(),
                    category = category.trim(),
                    tagline = tagline.trim(),
                    description = description.trim(),
                    primaryColorHex = primaryColorHex.trim(),
                    accentColorHex = accentColorHex.trim(),
                    materialFinish = materialFinish.trim()
                )
                val id = repository.insertCampaign(campaign)
                _activeCampaignId.value = id
                onCreated(id)

                if (autoGenerateMaster && hasApiKey.value) {
                    val saved = repository.getCampaign(id)
                    if (saved != null) {
                        generateMasterShot(saved)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create campaign: ${e.message}"
            }
        }
    }

    fun deleteCampaign(campaignId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteCampaign(campaignId)
                if (_activeCampaignId.value == campaignId) {
                    _activeCampaignId.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun generateMasterShot(campaign: CampaignEntity) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            _generatingProgressMessage.value = "Creating Master Studio Shot with Nano-Banana..."
            _errorMessage.value = null
            try {
                val result = repository.generateMasterShot(campaign)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to generate master shot"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Generation error"
            } finally {
                _isGenerating.value = false
                _generatingProgressMessage.value = null
            }
        }
    }

    fun generateMediumShot(
        campaign: CampaignEntity,
        mediumType: MediumType,
        customTitle: String? = null,
        customDetails: String? = null
    ) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            val name = customTitle ?: mediumType.displayName
            _generatingProgressMessage.value = "Imagining on $name (Nano-Banana consistency lock)..."
            _errorMessage.value = null
            try {
                val result = repository.generateMediumShot(campaign, mediumType, customTitle, customDetails)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to imagine on $name"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Generation error"
            } finally {
                _isGenerating.value = false
                _generatingProgressMessage.value = null
            }
        }
    }

    fun generateCoreMediumsSuite(campaign: CampaignEntity) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            try {
                val coreMediums = listOf(
                    MediumType.BILLBOARD,
                    MediumType.NEWSPAPER,
                    MediumType.SOCIAL_POST
                )

                for (medium in coreMediums) {
                    _generatingProgressMessage.value = "Synthesizing ${medium.displayName} with Nano-Banana..."
                    val currentCampaign = repository.getCampaign(campaign.id) ?: campaign
                    val res = repository.generateMediumShot(currentCampaign, medium)
                    if (res.isFailure) {
                        _errorMessage.value = "Medium ${medium.displayName} stopped: ${res.exceptionOrNull()?.message}"
                        break
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Batch generation failed"
            } finally {
                _isGenerating.value = false
                _generatingProgressMessage.value = null
            }
        }
    }

    fun deleteShot(shotId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteShot(shotId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete shot: ${e.message}"
            }
        }
    }
}
