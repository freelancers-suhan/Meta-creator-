package com.example.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.LinkedHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class HttpResponse(
    val code: Int,
    val body: String,
    val headers: Map<String, List<String>>
)

data class ProxyConfig(
    val host: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null,
    val isSocks: Boolean = false
) {
    fun toJavaProxy(): Proxy {
        val type = if (isSocks) Proxy.Type.SOCKS else Proxy.Type.HTTP
        return Proxy(type, InetSocketAddress(host, port))
    }

    fun displaySummary(): String {
        val prefix = if (isSocks) "socks5://" else "http://"
        val authPart = if (!username.isNullOrEmpty()) "$username:***@" else ""
        return "$prefix$authPart$host:$port"
    }
}

object HttpEngine {

    init {
        setupRelaxedSsl()
    }

    private fun setupRelaxedSsl() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (_: Exception) {
        }
    }

    fun parseProxy(rawLine: String): ProxyConfig? {
        val line = rawLine.trim().removePrefix("http://").removePrefix("https://")
        if (line.isEmpty() || line.startsWith("#")) return null

        val isSocks = rawLine.trim().startsWith("socks5://") || rawLine.trim().startsWith("socks://")
        val cleanLine = line.removePrefix("socks5://").removePrefix("socks://")

        return try {
            // Check for format: user:pass@host:port
            if (cleanLine.contains("@")) {
                val parts = cleanLine.split("@")
                val authParts = parts[0].split(":")
                val hostPort = parts[1].split(":")
                val user = authParts.getOrNull(0)
                val pass = authParts.getOrNull(1)
                val host = hostPort[0]
                val port = hostPort[1].toInt()
                return ProxyConfig(host = host, port = port, username = user, password = pass, isSocks = isSocks)
            }

            // Check for format: host:port:user:pass
            val colonParts = cleanLine.split(":")
            if (colonParts.size == 4) {
                val host = colonParts[0]
                val port = colonParts[1].toInt()
                val user = colonParts[2]
                val pass = colonParts[3]
                return ProxyConfig(host = host, port = port, username = user, password = pass, isSocks = isSocks)
            }

            // Check for standard format: host:port
            if (colonParts.size >= 2) {
                val host = colonParts[0]
                val port = colonParts[1].toInt()
                return ProxyConfig(host = host, port = port, username = null, password = null, isSocks = isSocks)
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun testProxy(proxy: ProxyConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val resp = get("https://api.ipify.org?format=json", proxy = proxy)
            if (resp.code == 200 && resp.body.isNotEmpty()) {
                val json = JSONObject(resp.body)
                val ip = json.optString("ip", "Connected")
                Pair(true, "Proxy Active | IP: $ip")
            } else {
                Pair(false, "Proxy HTTP ${resp.code}")
            }
        } catch (e: Exception) {
            Pair(false, "Connection Failed: " + (e.message ?: "Timeout"))
        }
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        proxy: ProxyConfig? = null
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest(url = url, method = "GET", headers = headers, body = null, proxy = proxy)
    }

    suspend fun post(
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        proxy: ProxyConfig? = null
    ): HttpResponse = withContext(Dispatchers.IO) {
        executeRequest(url = url, method = "POST", headers = headers, body = body, proxy = proxy)
    }

    private fun executeRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        proxy: ProxyConfig? = null
    ): HttpResponse {
        var conn: HttpURLConnection? = null
        try {
            val targetUrl = URL(url)

            if (proxy != null && !proxy.username.isNullOrEmpty() && !proxy.password.isNullOrEmpty()) {
                Authenticator.setDefault(object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(proxy.username, proxy.password.toCharArray())
                    }
                })
            }

            conn = if (proxy != null) {
                targetUrl.openConnection(proxy.toJavaProxy()) as HttpURLConnection
            } else {
                targetUrl.openConnection() as HttpURLConnection
            }

            conn.requestMethod = method
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true

            // Set proxy auth header if applicable
            if (proxy != null && !proxy.username.isNullOrEmpty() && !proxy.password.isNullOrEmpty()) {
                val credentials = "${proxy.username}:${proxy.password}"
                val encodedAuth = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                conn.setRequestProperty("Proxy-Authorization", "Basic $encodedAuth")
            }

            // Set custom headers
            headers.forEach { (k, v) ->
                conn.setRequestProperty(k, v)
            }

            if (method == "POST" && body != null) {
                conn.doOutput = true
                conn.outputStream.use { os ->
                    os.write(body.toByteArray(Charsets.UTF_8))
                    os.flush()
                }
            }

            val code = conn.responseCode
            val inputStream = if (code in 200..399) {
                conn.inputStream
            } else {
                conn.errorStream ?: conn.inputStream
            }

            val bodyString = if (inputStream != null) {
                readStreamDecompressed(inputStream, conn.contentEncoding)
            } else {
                ""
            }

            return HttpResponse(
                code = code,
                body = bodyString,
                headers = conn.headerFields ?: emptyMap()
            )
        } finally {
            conn?.disconnect()
        }
    }

    private fun readStreamDecompressed(inputStream: InputStream, contentEncoding: String?): String {
        try {
            val buffer = ByteArray(4096)
            val baos = ByteArrayOutputStream()
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                baos.write(buffer, 0, bytesRead)
            }
            val rawBytes = baos.toByteArray()

            val encoding = contentEncoding?.lowercase() ?: ""
            val isGzip = encoding.contains("gzip") || (rawBytes.size >= 2 && rawBytes[0] == 0x1f.toByte() && rawBytes[1] == 0x8b.toByte())
            val isDeflate = encoding.contains("deflate")

            if (isGzip) {
                try {
                    val gzipStream = GZIPInputStream(java.io.ByteArrayInputStream(rawBytes))
                    val decompressedBaos = ByteArrayOutputStream()
                    while (gzipStream.read(buffer).also { bytesRead = it } != -1) {
                        decompressedBaos.write(buffer, 0, bytesRead)
                    }
                    gzipStream.close()
                    return decompressedBaos.toString(Charsets.UTF_8.name())
                } catch (_: Exception) {
                }
            } else if (isDeflate) {
                try {
                    val inflaterStream = InflaterInputStream(java.io.ByteArrayInputStream(rawBytes))
                    val decompressedBaos = ByteArrayOutputStream()
                    while (inflaterStream.read(buffer).also { bytesRead = it } != -1) {
                        decompressedBaos.write(buffer, 0, bytesRead)
                    }
                    inflaterStream.close()
                    return decompressedBaos.toString(Charsets.UTF_8.name())
                } catch (_: Exception) {
                }
            }

            return String(rawBytes, Charsets.UTF_8)
        } finally {
            try {
                inputStream.close()
            } catch (_: Exception) {
            }
        }
    }

    fun encodeForm(params: Map<String, String>): String {
        val sb = StringBuilder()
        var first = true
        for ((key, value) in params) {
            if (!first) sb.append("&")
            first = false
            sb.append(URLEncoder.encode(key, "UTF-8"))
            sb.append("=")
            sb.append(URLEncoder.encode(value, "UTF-8"))
        }
        return sb.toString()
    }

    fun collectCookies(headerFields: Map<String, List<String>>): Map<String, String> {
        val cookies = LinkedHashMap<String, String>()
        for ((key, values) in headerFields) {
            if (key != null && key.equals("Set-Cookie", ignoreCase = true)) {
                for (cookie in values) {
                    val parts = cookie.split(";")
                    if (parts.isNotEmpty()) {
                        val cookiePart = parts[0].trim()
                        val eqIdx = cookiePart.indexOf('=')
                        if (eqIdx != -1) {
                            val cKey = cookiePart.substring(0, eqIdx).trim()
                            val cVal = cookiePart.substring(eqIdx + 1).trim()
                            cookies[cKey] = cVal
                        }
                    }
                }
            }
        }
        return cookies
    }

    fun mergeCookies(existingCookie: String, newCookies: Map<String, String>): String {
        if (newCookies.isEmpty()) return existingCookie
        val cookieMap = LinkedHashMap<String, String>()
        if (existingCookie.isNotEmpty()) {
            val parts = existingCookie.split(";")
            for (p in parts) {
                val item = p.trim()
                val eqIdx = item.indexOf('=')
                if (eqIdx != -1) {
                    cookieMap[item.substring(0, eqIdx).trim()] = item.substring(eqIdx + 1).trim()
                }
            }
        }
        cookieMap.putAll(newCookies)

        val sb = StringBuilder()
        var first = true
        for ((k, v) in cookieMap) {
            if (!first) sb.append("; ")
            first = false
            sb.append(k).append("=").append(v)
        }
        return sb.toString()
    }
}
