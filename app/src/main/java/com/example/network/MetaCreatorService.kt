package com.example.network

import org.json.JSONObject
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

data class MetaResult(
    val success: Boolean,
    val uid: String? = null,
    val name: String? = null,
    val dob: String? = null,
    val deviceModel: String? = null,
    val error: String? = null
)

object MetaCreatorService {

    private val BANGLADESHI_FIRST_NAMES = listOf(
        "Md", "Mohammad", "Suhan", "Rayhan", "Tanvir", "Shakil", "Arif",
        "Hasan", "Rakib", "Mehedi", "Alamin", "Sabbir", "Nayeem", "Fahim",
        "Imran", "Ashik", "Rifat", "Saiful", "Zahid", "Kamrul", "Jahid",
        "Sohan", "Nayem", "Farhan", "Robiul", "Asif", "Shahadat", "Sumon",
        "Tariqul", "Habib", "Shohel", "Mustafa", "Anwar", "Masud", "Mahfuz",
        "Ripon", "Sharif", "Enamul", "Mahbub", "Mizanur", "Zubair", "Nasir",
        "Khaled", "Bashir", "Moinul", "Jamil", "Rashid", "Shahid", "Kawsar",
        "Delwar", "Monir", "Selim", "Rashed", "Badal", "Golam", "Harun",
        "Ahsan", "Zakir", "Liton", "Sujon", "Jewel", "Rubel", "Rasel"
    )

    private val BANGLADESHI_LAST_NAMES = listOf(
        "Hossain", "Ahmed", "Khan", "Chowdhury", "Islam", "Rahman", "Miah",
        "Sheikh", "Sikder", "Bhuiyan", "Talukder", "Mondal", "Ali", "Haque",
        "Sarker", "Mahmud", "Kazi", "Gazi", "Biswas", "Howlader", "Majumder",
        "Dewan", "Akand", "Bari", "Patwary", "Munshi", "Bepari", "Mollah",
        "Chakladar", "Khondokar", "Samad", "Uddin", "Bakshi", "Barua", "Roy"
    )



    fun getRandomBangladeshiName(usedNames: Set<String> = emptySet()): Pair<String, String> {
        val rand = SecureRandom()
        for (attempt in 0..50) {
            val first = BANGLADESHI_FIRST_NAMES[rand.nextInt(BANGLADESHI_FIRST_NAMES.size)]
            val last = BANGLADESHI_LAST_NAMES[rand.nextInt(BANGLADESHI_LAST_NAMES.size)]
            val fullName = "$first $last"
            if (!usedNames.contains(fullName)) {
                return Pair(first, last)
            }
        }
        val first = BANGLADESHI_FIRST_NAMES[rand.nextInt(BANGLADESHI_FIRST_NAMES.size)]
        val last = BANGLADESHI_LAST_NAMES[rand.nextInt(BANGLADESHI_LAST_NAMES.size)]
        return Pair(first, last)
    }

    fun getRandomBirthDate18Plus(): String {
        val rand = SecureRandom()
        // Random age between 19 and 30 years old (1995 to 2005)
        val year = 1995 + rand.nextInt(11)
        val month = String.format(Locale.US, "%02d", 1 + rand.nextInt(12))
        val day = String.format(Locale.US, "%02d", 1 + rand.nextInt(28))
        return "$year-$month-$day"
    }

    private fun randHex(length: Int): String {
        val chars = "0123456789abcdef"
        val random = SecureRandom()
        val sb = StringBuilder()
        repeat(length) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    private fun randAlphaNumeric(length: Int): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_"
        val random = SecureRandom()
        val sb = StringBuilder()
        repeat(length) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    fun extractHatchCsrf(cookieString: String): String {
        val parts = cookieString.split(";")
        for (part in parts) {
            val item = part.trim()
            if (item.startsWith("hatch_native_auth_csrf=")) {
                val value = item.substring("hatch_native_auth_csrf=".length).trim().removeSurrounding("\"")
                if (value.isNotEmpty()) {
                    // In Meta/Hatch cookies, format is "<token>.<signature>"
                    // The HTTP header x-hatch-csrf-token requires ONLY the token before the dot
                    return value.substringBefore(".")
                }
            }
        }
        for (part in parts) {
            val item = part.trim()
            if (item.startsWith("csrftoken=")) {
                val value = item.substring("csrftoken=".length).trim().removeSurrounding("\"")
                if (value.isNotEmpty()) return value.substringBefore(".")
            }
        }
        return randHex(32)
    }

    fun extractRawHatchCsrf(cookieString: String): String {
        val parts = cookieString.split(";")
        for (part in parts) {
            val item = part.trim()
            if (item.startsWith("hatch_native_auth_csrf=")) {
                val value = item.substring("hatch_native_auth_csrf=".length).trim().removeSurrounding("\"")
                if (value.isNotEmpty()) return value
            }
        }
        for (part in parts) {
            val item = part.trim()
            if (item.startsWith("csrftoken=")) {
                val value = item.substring("csrftoken=".length).trim().removeSurrounding("\"")
                if (value.isNotEmpty()) return value
            }
        }
        return ""
    }

    private fun getMuseHeaders(csrfToken: String, cookie: String, device: DeviceProfile? = null): Map<String, String> {
        val effDevice = device ?: DevicePool.getRandomDevice()
        val majorVersion = effDevice.chromeVersion.substringBefore(".")

        val headers = mutableMapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android ${effDevice.androidVersion.substringBefore(".")}; ${effDevice.model} Build/${effDevice.buildId}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/${effDevice.chromeVersion} Mobile Safari/537.36",
            "Accept" to "application/json, text/plain, */*",
            "Accept-Encoding" to "gzip, deflate",
            "Accept-Language" to "en-US,en;q=0.9,bn-BD;q=0.8,bn;q=0.7",
            "Content-Type" to "application/json",
            "x-hatch-waterfall-id" to UUID.randomUUID().toString(),
            "x-hatch-csrf-token" to csrfToken,
            "x-csrf-token" to csrfToken,
            "x-csrftoken" to csrfToken,
            "sec-ch-ua-platform" to "\"Android\"",
            "sec-ch-ua" to "\"Chromium\";v=\"$majorVersion\", \"Android WebView\";v=\"$majorVersion\", \"Not-A.Brand\";v=\"99\"",
            "sec-ch-ua-model" to "\"${effDevice.model}\"",
            "sec-ch-ua-platform-version" to "\"${effDevice.androidVersion}\"",
            "x-hatch-caa-reg-entry-point" to "login_home",
            "sec-ch-ua-mobile" to "?1",
            "sec-ch-prefers-color-scheme" to "light",
            "origin" to "https://muse.ai",
            "sec-fetch-site" to "same-origin",
            "sec-fetch-mode" to "cors",
            "sec-fetch-dest" to "empty",
            "referer" to "https://muse.ai/",
            "priority" to "u=1, i"
        )
        if (cookie.isNotEmpty()) {
            headers["Cookie"] = cookie
        }
        return headers
    }

    suspend fun createMeta(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        dob: String,
        region: String = "BD",
        device: DeviceProfile? = null,
        proxy: ProxyConfig? = null
    ): MetaResult {
        return try {
            val effDevice = device ?: DevicePool.getRandomDevice()
            val majorVersion = effDevice.chromeVersion.substringBefore(".")
            val deviceId = "android-" + randHex(16)
            val csi = randAlphaNumeric(24)
            val waterfallId = UUID.randomUUID().toString()
            val qplJoinId = "f" + randHex(16)

            val url = "https://auth.meta.com/login/device-based/register-save-credentials/"
            val headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Linux; Android ${effDevice.androidVersion.substringBefore(".")}; ${effDevice.model} Build/${effDevice.buildId}; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/${effDevice.chromeVersion} Mobile Safari/537.36",
                "Accept-Encoding" to "gzip, deflate",
                "Content-Type" to "application/x-www-form-urlencoded",
                "sec-ch-ua-full-version-list" to "\"Chromium\";v=\"${effDevice.chromeVersion}\", \"Not-A.Brand\";v=\"99.0.0.0\"",
                "sec-ch-ua-platform" to "\"Android\"",
                "sec-ch-ua" to "\"Chromium\";v=\"$majorVersion\", \"Android WebView\";v=\"$majorVersion\", \"Not-A.Brand\";v=\"99\"",
                "sec-ch-ua-model" to "\"${effDevice.model}\"",
                "sec-ch-ua-mobile" to "?1",
                "x-asbd-id" to "359341",
                "x-fb-lsd" to "AdQQSft0Lj16s3kt5YP7gVojTpU",
                "sec-ch-prefers-color-scheme" to "light",
                "sec-ch-ua-platform-version" to "\"${effDevice.androidVersion}\"",
                "origin" to "https://auth.meta.com",
                "sec-fetch-site" to "same-origin",
                "sec-fetch-mode" to "cors",
                "sec-fetch-dest" to "empty",
                "referer" to "https://auth.meta.com/",
                "accept-language" to "en-US,en;q=0.9,bn-BD;q=0.8,bn;q=0.7",
                "priority" to "u=1, i"
            )

            val nowSeconds = System.currentTimeMillis() / 1000
            val formParams = linkedMapOf(
                "consent_version" to "",
                "contact_point" to email,
                "contact_point_type" to "EMAIL_ADDRESS",
                "csi" to csi,
                "date_of_birth" to dob,
                "device_id" to deviceId,
                "fb_encrypted_access_token" to "",
                "fb_oidc_access_token" to "",
                "first_name" to firstName,
                "github_prefill" to "",
                "google_id_token" to "",
                "has_youth_consent" to "false",
                "ig_encrypted_access_token" to "",
                "ig_encrypted_auth_header" to "",
                "ig_oidc_access_token" to "",
                "last_name" to lastName,
                "opt_into_marketing" to "true",
                "password" to password,
                "passwordless_registration" to "false",
                "redirect_uri" to "",
                "reg_integrity" to "Q8W2BT9mckEJMXpZ6nMJyD_i2hMYyeDc_qiOMjsvaHUNyL0zpyEBrYyufwby9U5MCT3BDjRwqknEfoQY2IqiA8iXOFfgMUHPA5RGmj1QFHAQ1vOzznYwtKwzEKBGHkfERgXkEKWsB85mQPcOSWGep6XY9P79cIcPYxM2DXezzOyDzJV_-ga-Rb5sqKcZixMhJjzOt501MJUSyiTP9Gc74MkvMJsNcKMmLA4G0BgthFswSG6gPQGmhHmHnggg-v6Ivg8RT5EFxReU0nsC6i9VbilUJjCkcvZUrf6-uM3i2KktEAH0SMkmgdWT3w21o9K5gw0aTr-Pw9oNpHeCGPpxLEY0zcd0338W9kcFiaIMpQQ9i9KcW4BsV6TWoRXXBqYuK6AgD3H4BqH1ieJhtg|kregenc",
                "should_save_credentials" to "true",
                "third_party_age_verification_id" to "",
                "waterfall_id" to waterfallId,
                "caa_event_flow" to "ntf",
                "entry_point" to "login_home",
                "event_client_time" to String.format(Locale.US, "%.3f", System.currentTimeMillis() / 1000.0),
                "is_kadabra_zero" to "false",
                "regulation_jurisdiction" to "[\"$region\"]",
                "qpl_join_id" to qplJoinId,
                "__user" to "0",
                "__a" to "1",
                "__req" to "1r",
                "__hs" to "20738.HYP:frl_comet_auth_pkg.2.1...0",
                "dpr" to "2",
                "__ccg" to "EXCELLENT",
                "__rev" to "1050031415",
                "__s" to "iofdj3:gh7l2x:5ddfgi",
                "__hsi" to "7695736164154778546",
                "__dyn" to "7xe6E5q5U5ObwKBAg5S1Dxu13w8CewSwMwNw9G2S0lW4o0B-q1ew2io2awpUO0n24o5-0Bo7O2l0Fwqo31w9O0H8-U2zxe2GewbS361qw8Xwn82Lw6OyES1Tw8W0Lo6-1FwCwe-1Iwqo5u1qwUw8S1Tw8q0JU0Vy3mew",
                "__csr" to "gittQAvGiuXtGXq9WveNkCylOk9gyaULjCG8Gi4biyEWi9x2eybghBCw1my04SE09QnsoQEGN03Ly7g01pFU0H60POG4U5GiE7u02my0b3w0wsB9g7XwGBoS3si0nS097w1a604Io-14w",
                "__hsdp" to "gd4AJh4v7xG2Na8G880ty9xa0bVBw0ELG01wpw1XG",
                "__hblp" to "09Gi7ob8Cewe65U21wn8Cmcw2X8OGw49waC08cw9G05RE0hqw0zSw0C-wd23u07KE7OexK3K0fIw1iK0aryE1rU",
                "__sjsp" to "gd4AJh5Tf7xG",
                "__comet_req" to "33",
                "lsd" to "AdQQSft0Lj16s3kt5YP7gVojTpU",
                "jazoest" to "22314",
                "__spin_r" to "1050031415",
                "__spin_b" to "trunk",
                "__spin_t" to nowSeconds.toString(),
                "__jssesw" to "1"
            )

            val body = HttpEngine.encodeForm(formParams)
            val resp = HttpEngine.post(url = url, headers = headers, body = body, proxy = proxy)

            var cleanBody = resp.body.trim()
            if (cleanBody.startsWith("for (;;);")) {
                cleanBody = cleanBody.substring("for (;;);".length).trim()
            }

            if (cleanBody.isEmpty()) {
                return MetaResult(success = false, error = "Empty Response (FB Blocked/Rate limited)")
            }

            try {
                val json = JSONObject(cleanBody)
                val payload = json.optJSONObject("payload")
                if (payload != null && payload.has("uid")) {
                    val uid = payload.optString("uid", "")
                    if (uid.isNotEmpty()) {
                        val fullName = "$firstName $lastName"
                        return MetaResult(success = true, uid = uid, name = fullName, dob = dob, deviceModel = effDevice.model)
                    }
                }

                // Parse error details
                val payloadErr = payload?.optString("error_title") 
                    ?: payload?.optString("error_message") 
                    ?: payload?.optString("error")
                val errorSummary = json.optString("errorSummary", "")
                val errorDesc = json.optString("errorDescription", "")
                val generalError = json.optString("error", "")

                val finalError = listOfNotNull(
                    errorSummary.ifEmpty { null },
                    errorDesc.ifEmpty { null },
                    payloadErr?.ifEmpty { null },
                    generalError.ifEmpty { null }
                ).firstOrNull() ?: "Meta registration rejected (${resp.code})"

                MetaResult(success = false, error = finalError)
            } catch (je: Exception) {
                MetaResult(success = false, error = "JSON Parse: " + (je.message ?: "Invalid body"))
            }
        } catch (e: Exception) {
            MetaResult(success = false, error = "Connection: " + (e.message ?: "Network error"))
        }
    }

    suspend fun museSendOtp(
        email: String,
        region: String = "BD",
        currentCookie: String,
        csrfToken: String = "",
        device: DeviceProfile? = null,
        proxy: ProxyConfig? = null
    ): Pair<String, String> { // Returns Pair<ResultStatus, MergedCookie>
        val effDevice = device ?: DevicePool.getRandomDevice()
        val tokenOnly = csrfToken.ifEmpty { extractHatchCsrf(currentCookie) }
        val rawToken = extractRawHatchCsrf(currentCookie)

        // Attempt 1: Using tokenOnly (token before dot)
        var r1 = sendOtpCall(email, region, currentCookie, tokenOnly, effDevice, proxy)
        if (r1.first == "OK") return r1

        // If CSRF failed, immediately retry with raw token (full token with HMAC signature)
        if (r1.first.contains("csrf", ignoreCase = true) && rawToken.isNotEmpty() && rawToken != tokenOnly) {
            val rCsrf = sendOtpCall(email, region, currentCookie, rawToken, effDevice, proxy)
            if (rCsrf.first == "OK") return rCsrf
            r1 = rCsrf
        }

        // If BD region is waitlisted or fails (and not CSRF error), retry with US
        if (region != "US" && !r1.first.contains("csrf", ignoreCase = true)) {
            val r2 = sendOtpCall(email, "US", r1.second, tokenOnly, effDevice, proxy)
            if (r2.first == "OK") return r2
            return r2
        }
        return r1
    }

    private suspend fun sendOtpCall(
        email: String,
        region: String,
        currentCookie: String,
        csrfToken: String,
        device: DeviceProfile,
        proxy: ProxyConfig?
    ): Pair<String, String> {
        return try {
            val headers = getMuseHeaders(csrfToken, currentCookie, device)
            val body = "{\"contact_point\":\"$email\",\"region\":\"$region\"}"
            val resp = HttpEngine.post("https://muse.ai/api/auth/native/send-otp", headers, body, proxy = proxy)

            val newCookies = HttpEngine.collectCookies(resp.headers)
            val mergedCookie = HttpEngine.mergeCookies(currentCookie, newCookies)

            val isSuccess = resp.code in 200..299 ||
                    resp.body.contains("otp_sent", ignoreCase = true) ||
                    resp.body.contains("flow", ignoreCase = true) ||
                    resp.body.contains("password_login_available", ignoreCase = true) ||
                    resp.body.contains("waitlist", ignoreCase = true) ||
                    resp.body.contains("wait_list", ignoreCase = true) ||
                    resp.body.contains("join", ignoreCase = true)

            if (isSuccess) {
                Pair("OK", mergedCookie)
            } else {
                val snippet = if (resp.body.length > 80) resp.body.substring(0, 80) else resp.body
                Pair("HTTP ${resp.code} | $snippet", mergedCookie)
            }
        } catch (e: Exception) {
            Pair("Exception: " + (e.message ?: "Network error"), currentCookie)
        }
    }

    suspend fun museConfirmOtp(
        otpCode: String,
        email: String,
        csrfToken: String = "",
        currentCookie: String,
        device: DeviceProfile? = null,
        proxy: ProxyConfig? = null
    ): String {
        return try {
            val effDevice = device ?: DevicePool.getRandomDevice()
            val tokenOnly = csrfToken.ifEmpty { extractHatchCsrf(currentCookie) }
            val rawToken = extractRawHatchCsrf(currentCookie)

            val headers = getMuseHeaders(tokenOnly, currentCookie, effDevice)
            val body = "{\"otp_code\":\"$otpCode\",\"contact_point\":\"$email\"}"
            var resp = HttpEngine.post("https://muse.ai/api/auth/native/confirm-otp", headers, body, proxy = proxy)

            // If CSRF failed, try with raw token
            if (resp.body.contains("csrf", ignoreCase = true) && rawToken.isNotEmpty() && rawToken != tokenOnly) {
                val headersRaw = getMuseHeaders(rawToken, currentCookie, effDevice)
                resp = HttpEngine.post("https://muse.ai/api/auth/native/confirm-otp", headersRaw, body, proxy = proxy)
            }

            val isSuccess = resp.code in 200..299 ||
                    resp.body.contains("session", ignoreCase = true) ||
                    resp.body.contains("user", ignoreCase = true) ||
                    resp.body.contains("token", ignoreCase = true) ||
                    resp.body.contains("success", ignoreCase = true) ||
                    resp.body.contains("logged_in", ignoreCase = true) ||
                    resp.body.contains("auth", ignoreCase = true) ||
                    resp.body.contains("waitlist", ignoreCase = true) ||
                    resp.body.contains("wait_list", ignoreCase = true) ||
                    resp.body.contains("join", ignoreCase = true) ||
                    resp.body.contains("already", ignoreCase = true)

            if (isSuccess) {
                "OK"
            } else {
                val snippet = if (resp.body.length > 80) resp.body.substring(0, 80) else resp.body
                "HTTP ${resp.code} | $snippet"
            }
        } catch (e: Exception) {
            "Exception: " + (e.message ?: "Network error")
        }
    }

    /**
     * Request Password Reset OTP for the newly created account
     */
    suspend fun museSendResetOtp(
        email: String,
        region: String = "BD",
        currentCookie: String,
        csrfToken: String = "",
        device: DeviceProfile? = null,
        proxy: ProxyConfig? = null
    ): Pair<String, String> { // Returns Pair<ResultStatus, MergedCookie>
        val effDevice = device ?: DevicePool.getRandomDevice()
        val tokenOnly = csrfToken.ifEmpty { extractHatchCsrf(currentCookie) }
        val rawToken = extractRawHatchCsrf(currentCookie)

        var r1 = sendResetOtpCall(email, region, currentCookie, tokenOnly, effDevice, proxy)
        if (r1.first == "OK") return r1

        if (r1.first.contains("csrf", ignoreCase = true) && rawToken.isNotEmpty() && rawToken != tokenOnly) {
            val rCsrf = sendResetOtpCall(email, region, currentCookie, rawToken, effDevice, proxy)
            if (rCsrf.first == "OK") return rCsrf
            r1 = rCsrf
        }

        if (region != "US" && !r1.first.contains("csrf", ignoreCase = true)) {
            val r2 = sendResetOtpCall(email, "US", r1.second, tokenOnly, effDevice, proxy)
            if (r2.first == "OK") return r2
            return r2
        }
        return r1
    }

    private suspend fun sendResetOtpCall(
        email: String,
        region: String,
        currentCookie: String,
        csrfToken: String,
        device: DeviceProfile,
        proxy: ProxyConfig?
    ): Pair<String, String> {
        return try {
            val headers = getMuseHeaders(csrfToken, currentCookie, device)
            val body = "{\"contact_point\":\"$email\",\"region\":\"$region\",\"flow\":\"reset_password\"}"
            val resp = HttpEngine.post("https://muse.ai/api/auth/native/send-otp", headers, body, proxy = proxy)

            val newCookies = HttpEngine.collectCookies(resp.headers)
            val mergedCookie = HttpEngine.mergeCookies(currentCookie, newCookies)

            val isSuccess = resp.code in 200..299 ||
                    resp.body.contains("otp_sent", ignoreCase = true) ||
                    resp.body.contains("flow", ignoreCase = true)

            if (isSuccess) {
                Pair("OK", mergedCookie)
            } else {
                val fallbackBody = "{\"contact_point\":\"$email\",\"region\":\"$region\"}"
                val fResp = HttpEngine.post("https://muse.ai/api/auth/native/send-otp", headers, fallbackBody, proxy = proxy)
                val fCookies = HttpEngine.collectCookies(fResp.headers)
                val fMerged = HttpEngine.mergeCookies(mergedCookie, fCookies)

                if (fResp.code in 200..299 || fResp.body.contains("otp_sent", ignoreCase = true)) {
                    Pair("OK", fMerged)
                } else {
                    val snippet = if (fResp.body.length > 80) fResp.body.substring(0, 80) else fResp.body
                    Pair("HTTP ${fResp.code} | $snippet", fMerged)
                }
            }
        } catch (e: Exception) {
            Pair("Exception: " + (e.message ?: "Network error"), currentCookie)
        }
    }

    /**
     * Set the final password using the intercepted 2nd OTP
     */
    suspend fun museSetPasswordWithOtp(
        otpCode: String,
        email: String,
        newPassword: String,
        csrfToken: String = "",
        currentCookie: String,
        device: DeviceProfile? = null,
        proxy: ProxyConfig? = null
    ): Pair<String, String> { // Returns Pair<ResultStatus, MergedCookie>
        return try {
            val effDevice = device ?: DevicePool.getRandomDevice()
            val tokenOnly = csrfToken.ifEmpty { extractHatchCsrf(currentCookie) }
            val rawToken = extractRawHatchCsrf(currentCookie)
            var effCsrf = tokenOnly

            var headers = getMuseHeaders(effCsrf, currentCookie, effDevice)

            // 1. Try reset-password endpoint
            val body1 = "{\"otp_code\":\"$otpCode\",\"contact_point\":\"$email\",\"password\":\"$newPassword\"}"
            var resp1 = HttpEngine.post("https://muse.ai/api/auth/native/reset-password", headers, body1, proxy = proxy)
            if (resp1.body.contains("csrf", ignoreCase = true) && rawToken.isNotEmpty() && rawToken != tokenOnly) {
                effCsrf = rawToken
                headers = getMuseHeaders(effCsrf, currentCookie, effDevice)
                resp1 = HttpEngine.post("https://muse.ai/api/auth/native/reset-password", headers, body1, proxy = proxy)
            }
            val c1 = HttpEngine.mergeCookies(currentCookie, HttpEngine.collectCookies(resp1.headers))

            if (resp1.code in 200..299 || resp1.body.contains("success", ignoreCase = true) || resp1.body.contains("session", ignoreCase = true) || resp1.body.contains("waitlist", ignoreCase = true)) {
                return Pair("OK", c1)
            }

            // 2. Try set-password endpoint
            val body2 = "{\"otp_code\":\"$otpCode\",\"contact_point\":\"$email\",\"password\":\"$newPassword\"}"
            val resp2 = HttpEngine.post("https://muse.ai/api/auth/native/set-password", headers, body2, proxy = proxy)
            val c2 = HttpEngine.mergeCookies(c1, HttpEngine.collectCookies(resp2.headers))

            if (resp2.code in 200..299 || resp2.body.contains("success", ignoreCase = true) || resp2.body.contains("waitlist", ignoreCase = true)) {
                return Pair("OK", c2)
            }

            // 3. Try confirm-otp with password field included
            val body3 = "{\"otp_code\":\"$otpCode\",\"contact_point\":\"$email\",\"password\":\"$newPassword\"}"
            val resp3 = HttpEngine.post("https://muse.ai/api/auth/native/confirm-otp", headers, body3, proxy = proxy)
            val c3 = HttpEngine.mergeCookies(c2, HttpEngine.collectCookies(resp3.headers))

            if (resp3.code in 200..299 || resp3.body.contains("success", ignoreCase = true) || resp3.body.contains("session", ignoreCase = true) || resp3.body.contains("logged_in", ignoreCase = true) || resp3.body.contains("waitlist", ignoreCase = true)) {
                return Pair("OK", c3)
            }

            val snippet = if (resp1.body.length > 80) resp1.body.substring(0, 80) else resp1.body
            Pair("HTTP ${resp1.code} | $snippet", c3)
        } catch (e: Exception) {
            Pair("Exception: " + (e.message ?: "Network error"), currentCookie)
        }
    }
}
