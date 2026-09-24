package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ApiKeySettingsDialog
import com.example.ui.screens.CampaignDetailScreen
import com.example.ui.screens.CampaignListScreen
import com.example.ui.screens.CreateCampaignScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BrandViewModel

sealed class Screen {
    data object List : Screen()
    data object Create : Screen()
    data class Detail(val campaignId: Long) : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: BrandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BrandBuilderApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BrandBuilderApp(viewModel: BrandViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val campaigns by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val activeCampaign by viewModel.activeCampaign.collectAsStateWithLifecycle()
    val activeShots by viewModel.activeCampaignShots.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val progressMessage by viewModel.generatingProgressMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val hasApiKey by viewModel.hasApiKey.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.List -> {
                CampaignListScreen(
                    campaigns = campaigns,
                    hasApiKey = hasApiKey,
                    onSelectCampaign = { id ->
                        viewModel.selectCampaign(id)
                        currentScreen = Screen.Detail(id)
                    },
                    onCreateCampaignClick = {
                        currentScreen = Screen.Create
                    },
                    onOpenSettingsClick = {
                        showApiKeyDialog = true
                    },
                    onDeleteCampaign = { id ->
                        viewModel.deleteCampaign(id)
                    }
                )
            }

            is Screen.Create -> {
                CreateCampaignScreen(
                    onNavigateBack = { currentScreen = Screen.List },
                    onCreateCampaign = { title, category, tagline, description, primaryColorHex, accentColorHex, materialFinish ->
                        viewModel.createCampaign(
                            title = title,
                            category = category,
                            tagline = tagline,
                            description = description,
                            primaryColorHex = primaryColorHex,
                            accentColorHex = accentColorHex,
                            materialFinish = materialFinish,
                            autoGenerateMaster = true,
                            onCreated = { newId ->
                                currentScreen = Screen.Detail(newId)
                            }
                        )
                    }
                )
            }

            is Screen.Detail -> {
                val campaign = activeCampaign
                if (campaign != null && campaign.id == screen.campaignId) {
                    CampaignDetailScreen(
                        campaign = campaign,
                        shots = activeShots,
                        isGenerating = isGenerating,
                        progressMessage = progressMessage,
                        errorMessage = errorMessage,
                        hasApiKey = hasApiKey,
                        onNavigateBack = {
                            viewModel.selectCampaign(null)
                            currentScreen = Screen.List
                        },
                        onOpenSettings = {
                            showApiKeyDialog = true
                        },
                        onClearError = {
                            viewModel.clearError()
                        },
                        onGenerateMaster = { c ->
                            viewModel.generateMasterShot(c)
                        },
                        onGenerateMedium = { c, type, title, details ->
                            viewModel.generateMediumShot(c, type, title, details)
                        },
                        onGenerateCoreSuite = { c ->
                            viewModel.generateCoreMediumsSuite(c)
                        },
                        onDeleteShot = { shotId ->
                            viewModel.deleteShot(shotId)
                        }
                    )
                } else {
                    // Fallback if not loaded yet
                    viewModel.selectCampaign(screen.campaignId)
                    CampaignListScreen(
                        campaigns = campaigns,
                        hasApiKey = hasApiKey,
                        onSelectCampaign = { id ->
                            viewModel.selectCampaign(id)
                            currentScreen = Screen.Detail(id)
                        },
                        onCreateCampaignClick = { currentScreen = Screen.Create },
                        onOpenSettingsClick = { showApiKeyDialog = true },
                        onDeleteCampaign = { id -> viewModel.deleteCampaign(id) }
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeySettingsDialog(
            currentHasKey = hasApiKey,
            onSaveKey = { newKey ->
                viewModel.saveCustomApiKey(newKey)
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}
