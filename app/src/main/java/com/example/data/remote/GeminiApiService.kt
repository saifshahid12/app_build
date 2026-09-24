package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GeneratedImageResult(
    val bitmap: Bitmap,
    val filePath: String,
    val base64Data: String,
    val textNote: String?
)

class GeminiApiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiService"
        // Nano-Banana model mapped to gemini-2.5-flash-image per guidelines
        const val NANO_BANANA_MODEL = "gemini-2.5-flash-image"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    /**
     * Resolves the active Gemini API key from user preferences or BuildConfig
     */
    fun getResolvedApiKey(): String {
        val sharedPrefs = context.getSharedPreferences("brand_builder_prefs", Context.MODE_PRIVATE)
        val userCustomKey = sharedPrefs.getString("custom_gemini_api_key", null)?.trim()
        if (!userCustomKey.isNullOrEmpty()) {
            return userCustomKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            buildKey
        } else {
            ""
        }
    }

    fun saveCustomApiKey(key: String) {
        val sharedPrefs = context.getSharedPreferences("brand_builder_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    fun hasValidApiKey(): Boolean {
        val key = getResolvedApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Calls Nano-Banana (gemini-2.5-flash-image) with prompt and optional reference image for visual consistency.
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        referenceImageBase64: String? = null,
        referenceMimeType: String = "image/jpeg"
    ): Result<GeneratedImageResult> = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is not configured. Please add your key in Settings or AI Studio Secrets.")
            )
        }

        try {
            // Build JSON request body per Gemini Image Generation specifications
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // 1. Text prompt part
            val textPart = JSONObject()
            textPart.put("text", prompt)
            partsArray.put(textPart)

            // 2. Multimodal reference image for product consistency (if provided)
            if (!referenceImageBase64.isNullOrEmpty()) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", referenceMimeType)
                inlineData.put("data", referenceImageBase64)
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // 3. Generation configuration for Nano-Banana image output
            val generationConfig = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            imageConfig.put("imageSize", "1K")
            generationConfig.put("imageConfig", imageConfig)

            val modalitiesArray = JSONArray()
            modalitiesArray.put("TEXT")
            modalitiesArray.put("IMAGE")
            generationConfig.put("responseModalities", modalitiesArray)

            rootJson.put("generationConfig", generationConfig)

            val url = "$BASE_URL$NANO_BANANA_MODEL:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            Log.d(TAG, "Requesting image generation with model $NANO_BANANA_MODEL, aspectRatio=$aspectRatio")
            val response = client.newCall(request).execute()

            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ($responseCode): $responseBody")
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "API returned error code $responseCode"
                } catch (_: Exception) {
                    "API returned error code $responseCode: $responseBody"
                }
                return@withContext Result.failure(Exception("Nano-Banana Error ($responseCode): $errorMsg"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val promptFeedback = jsonResponse.optJSONObject("promptFeedback")
                val blockReason = promptFeedback?.optString("blockReason")
                return@withContext Result.failure(
                    Exception(if (!blockReason.isNullOrEmpty()) "Generation blocked: $blockReason" else "No image candidate returned.")
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return@withContext Result.failure(Exception("No output parts returned from Nano-Banana."))
            }

            var imageBase64: String? = null
            var textDescription: String? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("inlineData")) {
                    val inlineData = part.getJSONObject("inlineData")
                    imageBase64 = inlineData.optString("data")
                } else if (part.has("text")) {
                    textDescription = part.optString("text")
                }
            }

            if (imageBase64.isNullOrEmpty()) {
                return@withContext Result.failure(
                    Exception(textDescription ?: "Nano-Banana model did not return image data.")
                )
            }

            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext Result.failure(Exception("Failed to decode image bytes from Nano-Banana."))

            // Save to internal storage
            val imagesDir = File(context.filesDir, "brand_images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val fileName = "shot_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val imageFile = File(imagesDir, fileName)

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            Result.success(
                GeneratedImageResult(
                    bitmap = bitmap,
                    filePath = imageFile.absolutePath,
                    base64Data = imageBase64,
                    textNote = textDescription
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during generation", e)
            Result.failure(e)
        }
    }

    /**
     * Reads image file and encodes it to base64 for multimodal consistency conditioning
     */
    suspend fun encodeImageFileToBase64(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode image to base64", e)
            null
        }
    }
}
