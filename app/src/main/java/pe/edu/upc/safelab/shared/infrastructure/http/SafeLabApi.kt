package pe.edu.upc.safelab.shared.infrastructure.http

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import pe.edu.upc.safelab.BuildConfig
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Cliente HTTP centralizado: errores distinguibles, cancelación real de llamadas OkHttp
 * y reintentos acotados únicamente para GET (nunca para escrituras o login).
 */
object SafeLabApi {
    // Render puede tardar en responder tras periodos de inactividad.
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(75, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    @Volatile private var accessToken: String? = null
    @Volatile var sessionRevision: Long = 0
        private set

    val authenticated: Boolean get() = !accessToken.isNullOrBlank()
    val isMock: Boolean get() = BuildConfig.USE_LOCAL_MOCKS

    suspend fun signIn(identifier: String, password: String): JSONObject {
        val payload = JSONObject().put("identifier", identifier).put("password", password)
        // No reintentar automáticamente POST /auth/login: un timeout no demuestra
        // que el servidor no haya procesado la operación.
        val response = request("POST", "/auth/login", payload, authorized = false)
        val token = response.getString("accessToken").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("La respuesta de autenticación no contiene un token válido")
        val user = response.getJSONObject("user")
        synchronized(this) { accessToken = token; sessionRevision++ }
        return user
    }

    /** Invalida el token de forma inmediata, sin depender de Internet. */
    @Synchronized
    fun clearLocalSession(): String? {
        val previousToken = accessToken
        accessToken = null
        sessionRevision++
        return previousToken
    }

    /** Revocación best-effort del token anterior; nunca afecta una sesión nueva. */
    suspend fun revokeSession(previousToken: String) {
        request("POST", "/auth/logout", authorized = false, tokenOverride = previousToken)
    }

    /** API mantenida para consumidores anteriores: la sesión local se cierra primero. */
    suspend fun signOut() {
        val previousToken = clearLocalSession() ?: return
        try { revokeSession(previousToken) } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* El cierre local no depende del backend */ }
    }

    suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        authorized: Boolean = true,
        tokenOverride: String? = null
    ): JSONObject {
        val methodUpper = method.uppercase()
        val token = tokenOverride ?: if (authorized) {
            accessToken ?: throw IllegalStateException("Inicia sesión para usar SafeLab API")
        } else null
        val builder = Request.Builder()
            .url(BuildConfig.API_BASE_URL.trimEnd('/') + path)
            .header("Accept", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        val requestBody = body?.toString()?.toRequestBody(jsonType)
            ?: if (methodUpper == "POST" || methodUpper == "PUT" || methodUpper == "PATCH")
                "{}".toRequestBody(jsonType) else null
        val request = builder.method(methodUpper, requestBody).build()

        // Solo una repetición adicional para GET, con pausa corta. No se reintentan
        // POST/PUT/PATCH/DELETE, evitando duplicar acciones de negocio.
        val maxAttempts = if (methodUpper == "GET") 2 else 1
        for (attempt in 1..maxAttempts) {
            try {
                return execute(request)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (http: SafeLabHttpException) {
                if (attempt == maxAttempts || http.status !in listOf(502, 503, 504)) throw http
            } catch (network: IOException) {
                if (attempt == maxAttempts || !isTemporaryNetworkFailure(network)) {
                    throw network.asSafeLabNetworkException()
                }
            }
            delay(1_500L) // Pausa antes del único reintento permitido.
        }
        error("No se completó la solicitud")
    }

    /** OkHttp asíncrono y cancelable: al abandonar la pantalla se cancela la llamada. */
    private suspend fun execute(request: Request): JSONObject = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        val text = it.body?.string().orEmpty()
                        if (!it.isSuccessful) {
                            val message = try { JSONObject(text).optString("message") }
                                catch (_: Exception) { "" }
                            throw SafeLabHttpException(
                                it.code,
                                message.ifBlank { "HTTP ${it.code} al ejecutar ${request.url.encodedPath}" }
                            )
                        }
                        val json = if (text.isBlank()) JSONObject() else JSONObject(text)
                        if (continuation.isActive) continuation.resume(json)
                    } catch (error: Exception) {
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
                }
            }
        })
    }

    suspend fun bootstrap(): JSONObject = request("GET", "/integration/bootstrap")
}

class SafeLabHttpException(val status: Int, override val message: String) : Exception(message)

enum class SafeLabNetworkProblem { DNS, TIMEOUT, CONNECTION }

class SafeLabNetworkException(val problem: SafeLabNetworkProblem, cause: IOException) : IOException(
    when (problem) {
        SafeLabNetworkProblem.DNS -> "No se pudo localizar el servidor. Comprueba la conexión a Internet y reintenta."
        SafeLabNetworkProblem.TIMEOUT -> "El servidor tardó demasiado en responder. Espera un momento y vuelve a intentar."
        SafeLabNetworkProblem.CONNECTION -> "No se pudo conectar con SafeLab. Comprueba la red y reintenta."
    }, cause
)

private fun isTemporaryNetworkFailure(error: IOException): Boolean =
    error is UnknownHostException || error is SocketTimeoutException ||
        error is ConnectException || error is SocketException || error is InterruptedIOException

private fun IOException.asSafeLabNetworkException(): SafeLabNetworkException = SafeLabNetworkException(
    when (this) {
        is UnknownHostException -> SafeLabNetworkProblem.DNS
        is SocketTimeoutException, is InterruptedIOException -> SafeLabNetworkProblem.TIMEOUT
        else -> SafeLabNetworkProblem.CONNECTION
    }, this
)

/** Mostrar mensajes comprensibles; nunca exponer la excepción DNS ni tokens al usuario. */
fun safeLabErrorMessage(error: Throwable, language: String): String {
    val spanish = language.equals("ES", ignoreCase = true)
    return when (error) {
        is SafeLabNetworkException -> when (error.problem) {
            SafeLabNetworkProblem.DNS -> if (spanish)
                "No se pudo localizar el servidor. Comprueba tu conexión a Internet y reintenta."
            else "Could not find the server. Check your Internet connection and try again."
            SafeLabNetworkProblem.TIMEOUT -> if (spanish)
                "El servidor está tardando en responder. Espera un momento y reintenta."
            else "The server is taking too long to respond. Please try again shortly."
            SafeLabNetworkProblem.CONNECTION -> if (spanish)
                "No se pudo conectar con SafeLab. Revisa tu red e inténtalo de nuevo."
            else "Could not connect to SafeLab. Check your connection and try again."
        }
        is SafeLabHttpException -> when (error.status) {
            401 -> if (spanish) "Tu sesión no es válida o ha expirado. Vuelve a iniciar sesión."
                   else "Your session is invalid or has expired. Please sign in again."
            403 -> if (spanish) "No tienes permisos para esta operación."
                   else "You do not have permission to perform this action."
            502, 503, 504 -> if (spanish) "El servidor está temporalmente no disponible. Inténtalo de nuevo."
                             else "The server is temporarily unavailable. Please try again."
            else -> if (spanish) "No se pudo completar la solicitud (HTTP ${error.status})."
                    else "The request could not be completed (HTTP ${error.status})."
        }
        else -> if (spanish) "No se pudo completar la operación. Vuelve a intentarlo."
                else "Could not complete the operation. Please try again."
    }
}
