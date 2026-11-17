package com.simeis512.arkatazuke.gemini

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Gemini API (REST, generateContent) の最小クライアント。
 * BYOK 方式なので、ユーザーが入力したキーで直接呼び出す。外部ライブラリは使わない。
 */
class GeminiClient(
    private val apiKey: String,
    private val model: String,
) {
    class GeminiException(message: String) : IOException(message)

    /** JPEG 画像を送り、片付け対象の一覧を構造化 JSON で受け取る。 */
    suspend fun analyzeClutter(jpeg: ByteArray): ClutterAnalysis {
        val body = JSONObject()
            .put(
                "contents", JSONArray().put(
                    JSONObject().put(
                        "parts", JSONArray()
                            .put(JSONObject().put("text", CLUTTER_PROMPT))
                            .put(
                                JSONObject().put(
                                    "inline_data", JSONObject()
                                        .put("mime_type", "image/jpeg")
                                        .put("data", Base64.encodeToString(jpeg, Base64.NO_WRAP))
                                )
                            )
                    )
                )
            )
            .put(
                "generationConfig", JSONObject()
                    .put("responseMimeType", "application/json")
                    .put("responseSchema", ClutterSchema.responseSchema)
            )
        val text = generateContent(body)
        return ClutterSchema.parse(text)
    }

    private suspend fun generateContent(body: JSONObject): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw GeminiException("APIキーが未設定です")
        val modelPath = URLEncoder.encode(model, "UTF-8")
        val url = URL("$BASE_URL/models/$modelPath:generateContent")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-goog-api-key", apiKey)
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val msg = runCatching {
                    JSONObject(response).getJSONObject("error").getString("message")
                }.getOrDefault(response.take(300))
                throw GeminiException("Gemini API エラー ($code): $msg")
            }
            extractText(response)
        } finally {
            conn.disconnect()
        }
    }

    private fun extractText(response: String): String {
        val candidates = JSONObject(response).optJSONArray("candidates")
            ?: throw GeminiException("応答に候補がありません: ${response.take(300)}")
        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
            ?: throw GeminiException("応答にテキストがありません: ${response.take(300)}")
        return buildString {
            for (i in 0 until parts.length()) append(parts.getJSONObject(i).optString("text"))
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

        private val CLUTTER_PROMPT = """
            あなたは部屋の片付けアドバイザーです。
            この写真に写っている、片付けが必要な物（床や机に置きっぱなしの物、出しっぱなしの物、ゴミなど）を挙げてください。
            元の場所にきちんと収まっている物は含めないでください。
            各項目について、画像内の位置と、どう片付けるかを短い日本語で答えてください。
        """.trimIndent()
    }
}
