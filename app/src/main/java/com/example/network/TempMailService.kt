package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.SecureRandom
import java.util.regex.Pattern

data class MailAccount(
    val email: String,
    val accountId: String,
    val token: String = "",
    val password: String = ""
)

data class MailCreationResult(
    val account: MailAccount? = null,
    val error: String? = null
)

object TempMailService {
    const val DEFAULT_MAIL_BASE = "https://api.mail.td"
    const val DEFAULT_API_TOKEN = "td_550f3ca2b9348c5e164faed4356421c495ffcb157cc39ede0127ff0583b4f6c5"

    private val BLOCKED_OTP_CODES = setOf(
        "141823", "141822", "141824", "141821", "141825", "141818", "141819",
        "141820", "141826", "141827", "000000", "111111", "222222",
        "999999", "00000", "0000"
    )

    private val OTP_PATTERN = Pattern.compile("\\b(\\d{6})\\b")

    /**
     * Fetch list of available domains from Mail.td
     */
    suspend fun getAvailableDomains(
        baseUrl: String = DEFAULT_MAIL_BASE,
        proxy: ProxyConfig? = null
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "$baseUrl/api/domains"
            val headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            )
            val resp = HttpEngine.get(endpoint, headers, proxy = proxy)
            if (resp.code == 200 && resp.body.isNotEmpty()) {
                val json = JSONObject(resp.body)
                val domainsArray = json.optJSONArray("domains")
                if (domainsArray != null && domainsArray.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until domainsArray.length()) {
                        val d = domainsArray.getJSONObject(i)
                        val domainStr = d.optString("domain", "").trim()
                        if (domainStr.isNotEmpty()) {
                            list.add(domainStr)
                        }
                    }
                    if (list.isNotEmpty()) {
                        val preferred = listOf("6n9.net", "end.tw", "uuf.me", "qabq.com")
                        val sorted = list.sortedByDescending { preferred.indexOf(it) }
                        return@withContext sorted
                    }
                }
            }
        } catch (_: Exception) {
        }
        listOf("6n9.net", "end.tw", "uuf.me", "qabq.com", "nqmo.com", "sugtbt.com")
    }

    /**
     * Create a new disposable mailbox using Mail.td API with the user's Access Key.
     */
    suspend fun create(
        baseUrl: String = DEFAULT_MAIL_BASE,
        apiToken: String = DEFAULT_API_TOKEN,
        proxy: ProxyConfig? = null
    ): MailCreationResult = withContext(Dispatchers.IO) {
        val trimmedToken = apiToken.trim()
        if (trimmedToken.isEmpty()) {
            return@withContext MailCreationResult(
                error = "Mail.td Access Key প্রয়োজন! অনুগ্রহ করে সেটিংস পেজে গিয়ে আপনার Access Key বসিয়ে সেভ করুন।"
            )
        }

        val domains = getAvailableDomains(baseUrl, proxy = proxy)
        val random = SecureRandom()
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        var lastError: String? = null

        for (domain in domains.take(4)) {
            try {
                val sb = StringBuilder()
                repeat(10) {
                    sb.append(chars[random.nextInt(chars.length)])
                }
                val localPart = sb.toString()
                val fullEmail = "$localPart@$domain"
                val accountPassword = "Passw0rd!" + random.nextInt(10000)

                val endpoint = "$baseUrl/api/accounts"
                val headers = mapOf(
                    "Authorization" to "Bearer $trimmedToken",
                    "Content-Type" to "application/json",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                )

                val bodyJson = JSONObject().apply {
                    put("address", fullEmail)
                    put("password", accountPassword)
                }.toString()

                val resp = HttpEngine.post(url = endpoint, headers = headers, body = bodyJson, proxy = proxy)
                if (resp.code in 200..201 && resp.body.isNotEmpty()) {
                    val json = JSONObject(resp.body)
                    val address = json.optString("address", fullEmail).trim().lowercase()
                    val id = json.optString("id", address).trim()
                    val token = json.optString("token", "").trim()

                    return@withContext MailCreationResult(
                        account = MailAccount(
                            email = address,
                            accountId = id,
                            token = token,
                            password = accountPassword
                        )
                    )
                } else {
                    lastError = "Mail.td HTTP ${resp.code} | " + if (resp.body.length > 80) resp.body.substring(0, 80) else resp.body
                }
            } catch (e: Exception) {
                lastError = "Mail.td Error: " + (e.message ?: "Network error")
            }
            delay(400)
        }

        MailCreationResult(error = lastError ?: "Mail.td mailbox creation failed")
    }

    /**
     * Poll incoming messages and extract 6-digit verification OTP from Mail.td
     */
    suspend fun waitForOtp(
        baseUrl: String = DEFAULT_MAIL_BASE,
        apiToken: String = DEFAULT_API_TOKEN,
        account: MailAccount,
        excludeOtp: String? = null,
        timeoutSeconds: Int = 60,
        proxy: ProxyConfig? = null,
        onPollTick: suspend (elapsedSeconds: Int) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        val mailboxId = account.accountId.ifEmpty { account.email }
        val safeMailboxId = if (mailboxId.contains("@")) {
            java.net.URLEncoder.encode(mailboxId, "UTF-8")
        } else {
            mailboxId
        }
        val messagesEndpoint = "$baseUrl/api/accounts/$safeMailboxId/messages?page=1"
        val headers = mapOf(
            "Authorization" to "Bearer ${apiToken.trim()}",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        )

        val startTime = System.currentTimeMillis()
        val timeoutMs = timeoutSeconds * 1000L

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            onPollTick(elapsed)

            try {
                val resp = HttpEngine.get(messagesEndpoint, headers, proxy = proxy)
                if (resp.code == 200 && resp.body.isNotEmpty()) {
                    val json = JSONObject(resp.body)
                    val messages = json.optJSONArray("messages")
                    if (messages != null && messages.length() > 0) {
                        for (i in 0 until messages.length()) {
                            val msg = messages.getJSONObject(i)
                            val msgId = msg.optString("id", "")
                            val subject = msg.optString("subject", "")
                            val intro = msg.optString("intro", "")
                            val combinedSummary = "$subject $intro"

                            val summaryMatcher = OTP_PATTERN.matcher(combinedSummary)
                            if (summaryMatcher.find()) {
                                val code = summaryMatcher.group(1)
                                if (code != null && !BLOCKED_OTP_CODES.contains(code) && (excludeOtp == null || code != excludeOtp)) {
                                    return@withContext code
                                }
                            }

                            if (msgId.isNotEmpty()) {
                                val detailEndpoint = "$baseUrl/api/accounts/$safeMailboxId/messages/$msgId"
                                val detailResp = HttpEngine.get(detailEndpoint, headers, proxy = proxy)
                                if (detailResp.code == 200 && detailResp.body.isNotEmpty()) {
                                    val detailJson = JSONObject(detailResp.body)
                                    val textBody = detailJson.optString("text_body", "")
                                    val htmlBody = detailJson.optString("html_body", "")
                                    val fullContent = "$subject $textBody $htmlBody"

                                    val fullMatcher = OTP_PATTERN.matcher(fullContent)
                                    while (fullMatcher.find()) {
                                        val candidate = fullMatcher.group(1)
                                        if (candidate != null && !BLOCKED_OTP_CODES.contains(candidate) && (excludeOtp == null || candidate != excludeOtp)) {
                                            return@withContext candidate
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            delay(2500)
        }

        null
    }
}
