package com.example.ai

import android.graphics.Bitmap
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
import java.util.concurrent.TimeUnit

data class ParsedReceipt(
    val merchant: String,
    val totalAmount: Double,
    val vatRate: Int,
    val vatAmount: Double,
    val baseAmount: Double,
    val suggestedCategory: String,
    val date: String
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun resolveApiKey(apiKeyOverride: String?): String {
        val override = apiKeyOverride?.trim()
        if (!override.isNullOrBlank()) {
            return override
        }
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "null") {
            return buildKey
        }
        return ""
    }

    suspend fun scanReceipt(bitmap: Bitmap, apiKeyOverride: String? = null): Result<ParsedReceipt> = withContext(Dispatchers.IO) {
        try {
            val key = resolveApiKey(apiKeyOverride)
            if (key.isBlank()) {
                return@withContext Result.failure(
                    Exception("Gemini API Anahtarı bulunamadı. Lütfen sağ üstteki Ayarlar (⚙️) menüsünden kendi Gemini API Key'inizi girip kaydediniz.")
                )
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                Sen bir Türk vergi ve muhasebe uzmanısın. Motosikletli esnaf kuryeler için fiş/fatura taraması yapıyorsun.
                Bu fiş/fatura görselini incele ve aşağıdaki alanları içeren geçerli bir JSON döndür:
                - merchant: İş yeri veya akaryakıt istasyonu adı (örn. 'Shell Petrol', 'Opet', 'Usta Motor', 'Migros')
                - totalAmount: Toplam ödenecek tutar (sayısal, örn. 350.50)
                - vatRate: Fişteki KDV oranı (0, 1, 10 veya 20 olarak tam sayı)
                - vatAmount: Hesaplanan/okunan KDV tutarı (sayısal)
                - baseAmount: KDV hariç tutar/matrah (sayısal)
                - suggestedCategory: 'Benzin & Yakıt', 'Motor Bakım & Servis', 'Kask, Mont & Ekipman', 'Market & Mutfak', 'Kişisel Yemek & Kahve' veya 'Genel Gider' seçeneklerinden en uygunu
                - date: Fiş tarihi (YYYY-MM-DD veya DD.MM.YYYY)

                Sadece saf JSON döndür. Başında veya sonunda markdown kod bloğu (```json) kullanma.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("response_mime_type", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$key")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "API error: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Fiş okunamadı (HTTP ${response.code})"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text").trim()

            val cleanJson = if (rawText.startsWith("```")) {
                rawText.substringAfter("\n").substringBeforeLast("```").trim()
            } else {
                rawText
            }

            val parsedObj = JSONObject(cleanJson)
            val total = parsedObj.optDouble("totalAmount", 0.0)
            val vatRate = parsedObj.optInt("vatRate", 20)
            val calculatedVat = if (parsedObj.has("vatAmount") && parsedObj.getDouble("vatAmount") > 0) {
                parsedObj.getDouble("vatAmount")
            } else {
                total * vatRate / (100.0 + vatRate)
            }
            val base = if (parsedObj.has("baseAmount") && parsedObj.getDouble("baseAmount") > 0) {
                parsedObj.getDouble("baseAmount")
            } else {
                total - calculatedVat
            }

            val parsed = ParsedReceipt(
                merchant = parsedObj.optString("merchant", "Bilinmeyen İş Yeri"),
                totalAmount = total,
                vatRate = vatRate,
                vatAmount = (Math.round(calculatedVat * 100.0) / 100.0),
                baseAmount = (Math.round(base * 100.0) / 100.0),
                suggestedCategory = parsedObj.optString("suggestedCategory", "Benzin & Yakıt"),
                date = parsedObj.optString("date", "")
            )

            Result.success(parsed)
        } catch (e: Exception) {
            Log.e("GeminiService", "Fiş tarama hatası", e)
            Result.failure(e)
        }
    }

    suspend fun chatWithAssistant(
        history: List<ChatMessage>,
        userMessage: String,
        courierContext: String,
        apiKeyOverride: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = resolveApiKey(apiKeyOverride)
            if (key.isBlank()) {
                return@withContext Result.failure(
                    Exception("Gemini API Anahtarı bulunamadı. Lütfen sağ üstteki Ayarlar (⚙️) menüsünden kendi Gemini API Key'inizi girip kaydediniz.")
                )
            }

            val systemInstruction = """
                Sen şahıs şirketi olan motosikletli kuryelerin (Trendyol Go, Getir, Yemeksepeti, Vigo vb.) kişisel mali müşaviri ve vergi uzmanısın.
                Adın: 'KuryeCüzdan Yapay Zeka Asistanı'.
                
                Kullanıcının güncel cüzdan durumu:
                $courierContext

                Türk Vergi Mevzuatı Kuralları:
                1. KDV 1 Beyannamesi her ayın 28'i akşamına kadar verilir.
                2. Şirket giderlerinin (Benzin, motor yedek parça, kask/ekipman, muhasebe) KDV'si (%20), hakedişten kesilen hesaplanan KDV'den İNDİRİLİR (mahsup edilir).
                3. Market, ev kirası ve kişisel yemek harcamaları şirketin KDV beyannamesinden düşülemez.
                4. Motosiklet amortismanı, sigorta, kasko ve servis bakım masrafları gelir vergisi matrahından gider olarak düşülebilir.

                Cevapların kısa, net, kurye dostu, samimi ve anlaşılır olsun. Gerekirse madde imleriyle açıkla.
            """.trimIndent()

            val contents = JSONArray().apply {
                // Add system context in prompt
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", "Sistem Bilgisi ve Vergi Kuralları:\n$systemInstruction") })
                    })
                })
                put(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", "Anladım. Kurye arkadaşımın cüzdan verileri ve Türk vergi mevzuatına göre sorularını yanıtlamaya hazırım. Nasıl yardımcı olabilirim?") })
                    })
                })

                // Add past 6 conversation turns
                val recentHistory = history.takeLast(6)
                for (msg in recentHistory) {
                    put(JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }

                // Add current question
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$key")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiService", "Chat API error: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Asistan şu anda yanıt veremiyor (HTTP ${response.code})"))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text").trim()

            Result.success(text)
        } catch (e: Exception) {
            Log.e("GeminiService", "Chat hatası", e)
            Result.failure(e)
        }
    }
}
