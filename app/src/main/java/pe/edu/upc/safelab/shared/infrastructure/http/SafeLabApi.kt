package pe.edu.upc.safelab.shared.infrastructure.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pe.edu.upc.safelab.BuildConfig
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP único para todas las pantallas Android.
 * OkHttp soporta PATCH de forma nativa; HttpURLConnection no lo admite.
 */
object SafeLabApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private var accessToken: String? = null

    val authenticated: Boolean get() = !accessToken.isNullOrBlank()
    val isMock: Boolean get() = BuildConfig.USE_LOCAL_MOCKS

    suspend fun signIn(identifier: String, password: String): JSONObject {
        val payload = JSONObject().put("identifier", identifier).put("password", password)
        val response = request("POST", "/auth/login", payload, authorized = false)
        accessToken = response.getString("accessToken")
        return response.getJSONObject("user")
    }

    suspend fun signOut() {
        try { if (authenticated) request("POST", "/auth/logout") }
        finally { accessToken = null }
    }

    suspend fun request(method: String, path: String, body: JSONObject? = null, authorized: Boolean = true): JSONObject =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder().url(BuildConfig.API_BASE_URL.trimEnd('/') + path)
                .header("Accept", "application/json")
            if (authorized) {
                val token = accessToken ?: throw IllegalStateException("Inicia sesión para usar SafeLab API")
                builder.header("Authorization", "Bearer $token")
            }
            val requestBody = body?.toString()?.toRequestBody(jsonType)
                ?: if (method == "POST" || method == "PUT" || method == "PATCH") "{}".toRequestBody(jsonType) else null
            builder.method(method, requestBody)
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = try { JSONObject(text).optString("message") } catch (_: Exception) { "" }
                    throw SafeLabHttpException(response.code, message.ifBlank { "HTTP ${response.code} al ejecutar $path" })
                }
                if (text.isBlank()) JSONObject() else JSONObject(text)
            }
        }

    suspend fun bootstrap(): JSONObject = request("GET", "/integration/bootstrap")
}

class SafeLabHttpException(val status: Int, override val message: String) : Exception(message)
