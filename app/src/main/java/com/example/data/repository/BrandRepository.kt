package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.data.local.BrandDao
import com.example.data.local.CampaignEntity
import com.example.data.local.MediumShotEntity
import com.example.data.model.MediumType
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeneratedImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class BrandRepository(
    private val context: Context,
    private val brandDao: BrandDao,
    val geminiService: GeminiApiService
) {
    val allCampaigns: Flow<List<CampaignEntity>> = brandDao.getAllCampaigns()

    fun observeCampaign(id: Long): Flow<CampaignEntity?> = brandDao.observeCampaignById(id)

    fun observeShotsForCampaign(campaignId: Long): Flow<List<MediumShotEntity>> =
        brandDao.getShotsForCampaign(campaignId)

    suspend fun getCampaign(id: Long): CampaignEntity? = brandDao.getCampaignById(id)

    suspend fun insertCampaign(campaign: CampaignEntity): Long = brandDao.insertCampaign(campaign)

    suspend fun updateCampaign(campaign: CampaignEntity) = brandDao.updateCampaign(campaign)

    suspend fun deleteCampaign(id: Long) = brandDao.deleteCampaignById(id)

    suspend fun insertShot(shot: MediumShotEntity): Long = brandDao.insertShot(shot)

    suspend fun deleteShot(id: Long) = brandDao.deleteShotById(id)

    /**
     * Builds the high-fidelity prompt for the Master Hero Product Shot.
     */
    fun buildMasterProductPrompt(campaign: CampaignEntity): String {
        return buildString {
            append("Ultra-high-resolution commercial studio product photography of '${campaign.title}'. ")
            append("Category: ${campaign.category}. ")
            if (campaign.tagline.isNotBlank()) append("Tagline: \"${campaign.tagline}\". ")
            append("Design & Features: ${campaign.description}. ")
            append("Material and Texture: ${campaign.materialFinish}. ")
            append("Color Palette: Primary dominant tone is ${campaign.primaryColorHex} with sophisticated accents of ${campaign.accentColorHex}. ")
            append("Studio Setup: Centered iconic hero perspective on a sleek architectural pedestal, soft dramatic 3-point softbox studio lighting, crisp reflections, clean premium editorial catalog look. ")
            append("CRITICAL MANDATE: ABSOLUTELY ZERO PEOPLE, NO HUMANS, NO HANDS, NO BODIES, NO FACES. Pure inanimate product photography only.")
        }
    }

    /**
     * Builds the medium-specific prompt that guarantees product consistency and environment fidelity.
     */
    fun buildMediumPrompt(
        campaign: CampaignEntity,
        mediumType: MediumType,
        customMediumTitle: String? = null,
        customPromptDetails: String? = null
    ): String {
        val baseConsistencyPrompt = buildString {
            append("VISUAL CONTINUITY MANDATE: Maintain 100% visual consistency with the master product '${campaign.title}'. ")
            append("The product MUST have the EXACT same silhouette, geometry, logo, colorway (${campaign.primaryColorHex} and ${campaign.accentColorHex}), and ${campaign.materialFinish} finish. ")
            append("CRITICAL RESTRICTION: STRICTLY NO PEOPLE, NO HUMANS, NO HANDS, NO FACES anywhere in the entire scene. Pure environment, architectural setting, and product only. ")
        }

        val mediumContext = when (mediumType) {
            MediumType.BILLBOARD -> {
                "Showcase this exact product featured on a giant illuminated highway billboard ad during twilight blue hour. " +
                        "The billboard towers over a wide elevated roadway with blurred ambient city lights and a sleek minimalist skyline in the distant background. " +
                        "Dramatic low-angle commercial architectural perspective looking up at the high-impact billboard banner displaying '${campaign.title}'. Completely devoid of any people or pedestrians."
            }
            MediumType.NEWSPAPER -> {
                "Showcase this exact product in a full-page broadsheet newspaper advertisement. " +
                        "Photographed flatlay on an editor's dark oak desk under warm desk lamp illumination. " +
                        "Authentic visible newsprint paper grain, typographic serif headlines, delicate halftone ink dot pattern, and editorial column grids framing the bold product illustration of '${campaign.title}'. " +
                        "No hands, no people."
            }
            MediumType.SOCIAL_POST -> {
                "Showcase this exact product in an ultra-modern high-end social media square flatlay post. " +
                        "Pristine studio styling on a sculptural pastel backdrop with soft organic botanical leaf shadows, balanced geometric stone plinths, and crisp modern design aesthetics. " +
                        "Commercial ready for an Instagram campaign feed. Pure product elegance, zero people or hands."
            }
            MediumType.MAGAZINE -> {
                "Showcase this exact product in an open double-page spread of a premier glossy fashion and design magazine resting on an Italian marble surface. " +
                        "Subtle glossy paper light reflections, high-end editorial typography, minimalist negative space, and museum-grade product lighting. " +
                        "Luxury publication layout. Absolutely no people."
            }
            MediumType.SUBWAY -> {
                "Showcase this exact product in a prominent vertical backlit illuminated poster display inside a sleek architectural subway or transit pavilion at blue hour. " +
                        "Clean polished concrete, curved brushed steel and glass panels with soft reflections. " +
                        "Quiet, serene empty transit terminal with zero commuters and no people."
            }
            MediumType.STOREFRONT -> {
                "Showcase this exact product inside a high-end luxury boutique vitrine display window at dusk. " +
                        "The product is spotlighted on a bespoke terrazzo pedestal with sculptural arches and subtle street reflections in the floor-to-ceiling glass. " +
                        "Empty store interior and empty sidewalk, no shoppers, zero humans."
            }
            MediumType.CUSTOM -> {
                val title = customMediumTitle ?: "Custom Medium"
                val extra = if (!customPromptDetails.isNullOrBlank()) " Medium details: $customPromptDetails." else ""
                "Showcase this exact product prominently displayed in an advertising context of '$title'.$extra " +
                        "Realistic commercial environment, high resolution lighting and materials. Zero people."
            }
        }

        return "$baseConsistencyPrompt $mediumContext"
    }

    /**
     * Generates a Master Studio Shot for a campaign using Nano-Banana (gemini-2.5-flash-image)
     */
    suspend fun generateMasterShot(campaign: CampaignEntity): Result<GeneratedImageResult> {
        val prompt = buildMasterProductPrompt(campaign)
        val result = geminiService.generateImage(
            prompt = prompt,
            aspectRatio = "1:1",
            referenceImageBase64 = null
        )

        if (result.isSuccess) {
            val generated = result.getOrThrow()
            // Update campaign with master image path
            val updated = campaign.copy(masterImagePath = generated.filePath)
            brandDao.updateCampaign(updated)
        }

        return result
    }

    /**
     * Generates a medium shot (Billboard, Newspaper, Social, etc.) using Nano-Banana,
     * maintaining consistency by conditioning with the master product image reference.
     */
    suspend fun generateMediumShot(
        campaign: CampaignEntity,
        mediumType: MediumType,
        customTitle: String? = null,
        customPromptDetails: String? = null
    ): Result<MediumShotEntity> = withContext(Dispatchers.IO) {
        val prompt = buildMediumPrompt(campaign, mediumType, customTitle, customPromptDetails)
        val aspectRatio = mediumType.defaultAspectRatio

        // Check if master image exists for visual reference conditioning
        val masterBase64 = if (!campaign.masterImagePath.isNullOrEmpty()) {
            geminiService.encodeImageFileToBase64(campaign.masterImagePath)
        } else {
            null
        }

        val result = geminiService.generateImage(
            prompt = prompt,
            aspectRatio = aspectRatio,
            referenceImageBase64 = masterBase64
        )

        if (result.isSuccess) {
            val generated = result.getOrThrow()
            val shotTitle = customTitle ?: mediumType.displayName
            val shotEntity = MediumShotEntity(
                campaignId = campaign.id,
                mediumType = mediumType.id,
                mediumTitle = shotTitle,
                prompt = prompt,
                imagePath = generated.filePath,
                aspectRatio = aspectRatio,
                isConsistentWithMaster = masterBase64 != null
            )
            val insertedId = brandDao.insertShot(shotEntity)
            Result.success(shotEntity.copy(id = insertedId))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown generation error"))
        }
    }

    /**
     * Seeds initial demo campaigns with pre-rendered artwork placeholders if DB is empty
     */
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existing = brandDao.getAllCampaigns().first()
        if (existing.isNotEmpty()) return@withContext

        // Create initial showcase campaign: "AeroPulse Spark"
        val seedCampaign = CampaignEntity(
            title = "AeroPulse Spark",
            category = "Beverage",
            tagline = "Pure Electric Hydration",
            description = "Sleek 330ml slim aluminum beverage can with matte finish. Minimalist vertical typography reading 'AEROPULSE' with an embossed thunderbolt emblem.",
            primaryColorHex = "#0EA5E9",
            accentColorHex = "#F59E0B",
            materialFinish = "Matte anodized sky blue aluminum with gold foil UV embossed emblem"
        )
        val campaignId = brandDao.insertCampaign(seedCampaign)

        // Generate synthetic initial visuals for instant gratification
        val masterPath = createLocalPlaceholderImage("AeroPulse Spark", "Master Studio Shot", "#0F172A", "#0EA5E9", "1:1")
        val updatedCampaign = seedCampaign.copy(id = campaignId, masterImagePath = masterPath)
        brandDao.updateCampaign(updatedCampaign)

        // Seed 3 core mediums: Billboard, Newspaper, Social Post
        val billboardPath = createLocalPlaceholderImage("AeroPulse", "Highway Billboard Ad", "#1E293B", "#38BDF8", "16:9")
        val newspaperPath = createLocalPlaceholderImage("AeroPulse", "Sunday Broadsheet Ad", "#F5F5F0", "#334155", "3:4")
        val socialPath = createLocalPlaceholderImage("AeroPulse", "Instagram Studio Flatlay", "#0C4A6E", "#F59E0B", "1:1")

        brandDao.insertShot(
            MediumShotEntity(
                campaignId = campaignId,
                mediumType = MediumType.BILLBOARD.id,
                mediumTitle = "Sunset Highway Billboard",
                prompt = buildMediumPrompt(updatedCampaign, MediumType.BILLBOARD),
                imagePath = billboardPath,
                aspectRatio = "16:9",
                isConsistentWithMaster = true
            )
        )
        brandDao.insertShot(
            MediumShotEntity(
                campaignId = campaignId,
                mediumType = MediumType.NEWSPAPER.id,
                mediumTitle = "Broadsheet Halftone Print",
                prompt = buildMediumPrompt(updatedCampaign, MediumType.NEWSPAPER),
                imagePath = newspaperPath,
                aspectRatio = "3:4",
                isConsistentWithMaster = true
            )
        )
        brandDao.insertShot(
            MediumShotEntity(
                campaignId = campaignId,
                mediumType = MediumType.SOCIAL_POST.id,
                mediumTitle = "Instagram Editorial Flatlay",
                prompt = buildMediumPrompt(updatedCampaign, MediumType.SOCIAL_POST),
                imagePath = socialPath,
                aspectRatio = "1:1",
                isConsistentWithMaster = true
            )
        )
    }

    private fun createLocalPlaceholderImage(
        title: String,
        subtitle: String,
        bgHex: String,
        accentHex: String,
        aspectRatio: String
    ): String {
        val (width, height) = when (aspectRatio) {
            "16:9" -> Pair(960, 540)
            "3:4" -> Pair(600, 800)
            "9:16" -> Pair(540, 960)
            "4:3" -> Pair(800, 600)
            else -> Pair(800, 800)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = Color.parseColor(bgHex)
        val accentColor = Color.parseColor(accentHex)

        // Fill background
        canvas.drawColor(bgColor)

        // Draw geometric design elements
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = accentColor
        paint.alpha = 40
        canvas.drawCircle(width * 0.75f, height * 0.3f, width * 0.35f, paint)

        // Product silhouette podium
        paint.color = accentColor
        paint.alpha = 180
        val rect = RectF(width * 0.35f, height * 0.25f, width * 0.65f, height * 0.75f)
        canvas.drawRoundRect(rect, 40f, 40f, paint)

        // Product emblem inner accent
        paint.color = Color.WHITE
        paint.alpha = 230
        canvas.drawCircle(width * 0.5f, height * 0.45f, width * 0.08f, paint)

        // Texts
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (width * 0.055f).coerceIn(28f, 52f)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(title, width * 0.5f, height * 0.62f, textPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            textSize = (width * 0.032f).coerceIn(18f, 32f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, width * 0.5f, height * 0.68f, subPaint)

        // Tag: Nano-Banana Generated
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 22f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("✦ NANO-BANANA CONSISTENT SHOT ✦", width * 0.5f, height * 0.88f, tagPaint)

        val imagesDir = File(context.filesDir, "brand_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val file = File(imagesDir, "demo_${System.currentTimeMillis()}_${(100..999).random()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }
}
