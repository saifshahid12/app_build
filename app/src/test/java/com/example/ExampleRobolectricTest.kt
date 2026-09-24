package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CampaignEntity
import com.example.data.model.MediumType
import com.example.data.remote.GeminiApiService
import com.example.data.repository.BrandRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Brand Builder", appName)
    }

    @Test
    fun `nano banana model is configured correctly`() {
        assertEquals("gemini-2.5-flash-image", GeminiApiService.NANO_BANANA_MODEL)
    }

    @Test
    fun `master product prompt mandates no people and product specs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = GeminiApiService(context)
        val dummyDao = object : com.example.data.local.BrandDao {
            override fun getAllCampaigns() = kotlinx.coroutines.flow.flowOf(emptyList<CampaignEntity>())
            override suspend fun getCampaignById(id: Long) = null
            override fun observeCampaignById(id: Long) = kotlinx.coroutines.flow.flowOf(null)
            override suspend fun insertCampaign(campaign: CampaignEntity) = 1L
            override suspend fun updateCampaign(campaign: CampaignEntity) {}
            override suspend fun deleteCampaignById(id: Long) {}
            override fun getShotsForCampaign(campaignId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<com.example.data.local.MediumShotEntity>())
            override suspend fun insertShot(shot: com.example.data.local.MediumShotEntity) = 1L
            override suspend fun deleteShotById(id: Long) {}
            override suspend fun deleteShotsByMedium(campaignId: Long, mediumType: String) {}
        }
        val repository = BrandRepository(context, dummyDao, service)

        val campaign = CampaignEntity(
            title = "AeroPulse Spark",
            category = "Beverage",
            tagline = "Pure Electric Hydration",
            description = "330ml sleek can",
            primaryColorHex = "#0EA5E9",
            accentColorHex = "#F59E0B",
            materialFinish = "Matte aluminum"
        )

        val masterPrompt = repository.buildMasterProductPrompt(campaign)
        assertTrue(masterPrompt.contains("ZERO PEOPLE") || masterPrompt.contains("NO PEOPLE") || masterPrompt.contains("NO HUMANS"))
        assertTrue(masterPrompt.contains("AeroPulse Spark"))

        val billboardPrompt = repository.buildMediumPrompt(campaign, MediumType.BILLBOARD)
        assertTrue(billboardPrompt.contains("billboard", ignoreCase = true))
        assertTrue(billboardPrompt.contains("NO PEOPLE", ignoreCase = true))
        assertTrue(billboardPrompt.contains("consistency", ignoreCase = true))

        val newspaperPrompt = repository.buildMediumPrompt(campaign, MediumType.NEWSPAPER)
        assertTrue(newspaperPrompt.contains("newspaper", ignoreCase = true))
        assertTrue(newspaperPrompt.contains("NO PEOPLE", ignoreCase = true))

        val socialPrompt = repository.buildMediumPrompt(campaign, MediumType.SOCIAL_POST)
        assertTrue(socialPrompt.contains("social", ignoreCase = true))
        assertTrue(socialPrompt.contains("NO PEOPLE", ignoreCase = true))
    }
}
