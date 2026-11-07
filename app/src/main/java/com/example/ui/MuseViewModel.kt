package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccountDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountJobState
import com.example.data.model.CookieValidation
import com.example.network.HttpEngine
import com.example.network.TempMailService
import com.example.service.BatchManager
import com.example.service.BatchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.security.SecureRandom

class MuseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AccountDatabase.getDatabase(application)
    private val dao = db.accountDao()
    private val prefs = application.getSharedPreferences("MusePrefs", Context.MODE_PRIVATE)

    // Multi-Cookie List (dynamic item addition with +)
    private val _cookieList = MutableStateFlow<List<String>>(loadInitialCookies())
    val cookieList: StateFlow<List<String>> = _cookieList.asStateFlow()

    // Backward compatibility single string accessor
    val cookieInput: StateFlow<String> = MutableStateFlow("")

    private val _passwordInput = MutableStateFlow(prefs.getString("password", "@Suhan1234") ?: "@Suhan1234")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _batchCount = MutableStateFlow(1)
    val batchCount: StateFlow<Int> = _batchCount.asStateFlow()

    private val _mailBaseUrl = MutableStateFlow(
        prefs.getString("mail_base", TempMailService.DEFAULT_MAIL_BASE) ?: TempMailService.DEFAULT_MAIL_BASE
    )
    val mailBaseUrl: StateFlow<String> = _mailBaseUrl.asStateFlow()

    // Access Key on Settings page
    private val _mailApiToken = MutableStateFlow(
        prefs.getString("mail_api_token", TempMailService.DEFAULT_API_TOKEN) ?: TempMailService.DEFAULT_API_TOKEN
    )
    val mailApiToken: StateFlow<String> = _mailApiToken.asStateFlow()

    // Multi-Proxy List for Settings page (one-by-one addition)
    private val _proxyList = MutableStateFlow<List<String>>(loadInitialProxies())
    val proxyList: StateFlow<List<String>> = _proxyList.asStateFlow()

    // Test results map per proxy index
    private val _proxyTestResults = MutableStateFlow<Map<Int, String>>(emptyMap())
    val proxyTestResults: StateFlow<Map<Int, String>> = _proxyTestResults.asStateFlow()

    private val _testingProxyIndex = MutableStateFlow<Int?>(null)
    val testingProxyIndex: StateFlow<Int?> = _testingProxyIndex.asStateFlow()

    // Navigation and Language
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(prefs.getString("lang", "bn") ?: "bn")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Batch Status connected directly to BatchManager
    val isBatchRunning: StateFlow<Boolean> = BatchManager.isBatchRunning
    val cooldownSeconds: StateFlow<Int> = BatchManager.cooldownSeconds
    val activeJobs: StateFlow<List<AccountJobState>> = BatchManager.activeJobs
    val snackbarMessage: SharedFlow<String> = BatchManager.snackbarMessage

    // Accounts History
    val allAccounts: StateFlow<List<AccountEntity>> = dao.getAllAccounts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val DEFAULT_USER_COOKIE = "datr=6Z_uaojrZUM5J9AhngyRAyg1; wd=980x1828; dpr=2.549999952316284; hatch_native_auth_csrf=2n-9p_LWb2S-y_1kCdYQBQmrNIrEQDz-kxzMTbWp8Uk.20AqOfszF4OT8qP7Wq8Ndn2oZpjyGwPnFlMY0ndU4-8; hatch_native_auth_temp=v1%3AaqE08AOaUJDP3fr5_IodY_qTCZUmZocdTc64UYdHaZyN37lZFuliq1PEIVDtUNKRRnKZ-mnJCHz6v7INesLW7VNrJ36xpqwH4Teb12LFM_ibKJdekx7Clzubeefw0k1ttZdmMojWDDj3dFBBtnT2tQti2v9bUeULabjOaOd-S6HJ5ay-wNHetjGUZzAPiLUjAJIPwS0MwbgJ8XA9LFMXtLGIEXbwvDHB4Yu2rvibWvJC9FTYqXUZBd7b6SBPPSvBr38NTstB-6XUvSieUytGzPLz9sxO0lVRuDOG88YwSCgm8FNP57BulDe4JqjzvY2YF0AF61dFAsA%3AdzC4upsBpCPYS99N%3Az3g-ZwJ-Ph30fpZG_LFNqQ; hatch_native_auth_device=v1%3A-gaPLwABYtevmRuBuaut574lzV3MRsW7QPsugt44yGDiOvcmFlD4Z2H3Gw%3AoA-eP3x1I2Xfv9qe%3AglYCoz30-z2F47_p3NHJsQ; hatch_native_auth_state=v1%3Ab9HjZHLuy5xVY7FGV6jIqdPu8Q2huAZyQT-0HytVuoBSCZdASfDoe0NQzCh-HtNzoBokbRHWvqa567rK068Wdd1WVrsePiG2hhIQfoa_C_AiywD54ZDX2ol_jZGWEuQjpEDgoCu9Pa1NrL_dV-dcXmU_BEwrUAnB33bbLT-Si6Z_iDgPA9JDcmFBZbGOdnrJmuGU0sduVlPb5Y3IRn35g5ioZuXzesSczkGbV1n7pPHJwJgWnZMFteu8M3e67mt0kkWnh3y29DneQyVMy_c2_NvzYIwrqouKe0wj_Ew5Qe_hRRtvqFU--lNOCiXJ6_blCif-61sKZpJbx4kAruhHclX3gi1YyqubtlJtjuQ8NuDJvK9iYs2lPNhnrjCGsPhkmktrTaw1PaliYthwdPPgSjTZmsVE6LSG6J9tABUq8PV_YdoAI_-B3TUYE7BAEcBtu-0npp85nTguIkA5ULg%3A8wgxp64tYSlthos4%3AdCift_qRBEV_R-B5nxUOnQ."

    private fun loadInitialCookies(): List<String> {
        val json = prefs.getString("cookie_list_json", null)
        if (!json.isNullOrEmpty()) {
            try {
                val arr = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val c = arr.optString(i, "").trim()
                    if (c.isNotEmpty()) list.add(c)
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        val legacy = prefs.getString("cookie", "") ?: ""
        val lines = legacy.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return if (lines.isNotEmpty()) lines else listOf(DEFAULT_USER_COOKIE)
    }

    private fun loadInitialProxies(): List<String> {
        val json = prefs.getString("proxy_list_json", null)
        if (!json.isNullOrEmpty()) {
            try {
                val arr = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val p = arr.optString(i, "").trim()
                    if (p.isNotEmpty()) list.add(p)
                }
                return list
            } catch (_: Exception) {}
        }
        val legacy = prefs.getString("proxy_input", "") ?: ""
        return legacy.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
        prefs.edit().putString("lang", lang).apply()
    }

    // --- Multi-Cookie Management ---
    fun updateCookieItem(index: Int, value: String) {
        val current = _cookieList.value.toMutableList()
        if (index in current.indices) {
            current[index] = value
            _cookieList.value = current
        }
    }

    fun addCookieItem() {
        val current = _cookieList.value.toMutableList()
        current.add("")
        _cookieList.value = current
        BatchManager.postSnackbar(
            if (_selectedLanguage.value == "bn")
                "নতুন কুকি স্লট যোগ করা হয়েছে (#${current.size})"
            else
                "Added cookie slot #${current.size}"
        )
    }

    fun removeCookieItem(index: Int) {
        val current = _cookieList.value.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            _cookieList.value = current
            saveCookiesToPrefs(current)
            BatchManager.postSnackbar(
                if (_selectedLanguage.value == "bn") "কুকি সরানো হয়েছে" else "Cookie removed"
            )
        } else if (current.size == 1 && index == 0) {
            current[0] = ""
            _cookieList.value = current
            saveCookiesToPrefs(current)
        }
    }

    fun saveCookies() {
        val current = _cookieList.value
        val validCount = current.count { validateSingleCookie(it).isValid }
        if (validCount == 0) {
            BatchManager.postSnackbar(
                if (_selectedLanguage.value == "bn")
                    "কমপক্ষে একটি কার্যকর কুকি প্রদান করুন!"
                else
                    "Please provide at least one valid cookie!"
            )
            return
        }
        saveCookiesToPrefs(current)
        BatchManager.postSnackbar(
            if (_selectedLanguage.value == "bn")
                "$validCount টি কুকি সফলভাবে সংরক্ষিত হয়েছে!"
            else
                "$validCount cookies saved successfully!"
        )
    }

    private fun saveCookiesToPrefs(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        val joined = list.filter { it.isNotBlank() }.joinToString("\n")
        prefs.edit()
            .putString("cookie_list_json", arr.toString())
            .putString("cookie", joined)
            .apply()
    }

    fun validateSingleCookie(cookie: String): CookieValidation {
        val trimmed = cookie.trim()
        val len = trimmed.length
        val hasCsrf = trimmed.contains("csrftoken")
        val hasHatch = trimmed.contains("hatch_native_auth_csrf")
        val isValid = len >= 20 && (hasHatch || hasCsrf || trimmed.contains("datr"))

        val isBn = _selectedLanguage.value == "bn"
        val message = when {
            trimmed.isEmpty() -> if (isBn) "কুকি খালি" else "Empty"
            len < 20 -> if (isBn) "কুকি খুব ছোট" else "Too short"
            !hasHatch && !hasCsrf -> if (isBn) "CSRF টোকেন মিসিং" else "Missing CSRF"
            else -> if (isBn) "সক্রিয় ও বৈধ" else "Valid"
        }

        return CookieValidation(
            isValid = isValid,
            hasCsrfToken = hasCsrf,
            hasHatchCsrf = hasHatch,
            length = len,
            message = message,
            validCookieCount = if (isValid) 1 else 0
        )
    }

    fun validateOverallCookies(): CookieValidation {
        val list = _cookieList.value
        val validList = list.filter { validateSingleCookie(it).isValid }
        val validCount = validList.size
        val isBn = _selectedLanguage.value == "bn"

        return if (validCount > 0) {
            CookieValidation(
                isValid = true,
                hasCsrfToken = true,
                hasHatchCsrf = true,
                length = validList.sumOf { it.length },
                message = if (isBn) "$validCount টি কুকি সক্রিয়" else "$validCount cookies active",
                validCookieCount = validCount
            )
        } else {
            CookieValidation(
                isValid = false,
                hasCsrfToken = false,
                hasHatchCsrf = false,
                length = 0,
                message = if (isBn) "কোনো কার্যকর কুকি নেই" else "No valid cookies",
                validCookieCount = 0
            )
        }
    }

    // --- Access Key Management ---
    fun saveAccessKey(key: String) {
        val trimmed = key.trim()
        _mailApiToken.value = trimmed
        prefs.edit().putString("mail_api_token", trimmed).apply()
        BatchManager.postSnackbar(
            if (_selectedLanguage.value == "bn")
                if (trimmed.isNotEmpty()) "Access Key সফলভাবে সংরক্ষিত হয়েছে!" else "Access Key মুছে ফেলা হয়েছে"
            else
                if (trimmed.isNotEmpty()) "Access Key saved successfully!" else "Access Key cleared"
        )
    }

    // --- Multi-Proxy Management (One-by-One in Settings) ---
    fun addProxy(rawProxy: String): Boolean {
        val trimmed = rawProxy.trim()
        val parsed = HttpEngine.parseProxy(trimmed)
        if (parsed == null) {
            val msg = if (_selectedLanguage.value == "bn")
                "ভুল প্রক্সি ফরম্যাট! উদাহরণ: 185.220.101.5:8080 অথবা host:port:user:pass"
            else
                "Invalid proxy format! Example: 185.220.101.5:8080 or host:port:user:pass"
            BatchManager.postSnackbar(msg)
            return false
        }
        val current = _proxyList.value.toMutableList()
        if (current.contains(trimmed)) {
            val msg = if (_selectedLanguage.value == "bn") "এই প্রক্সিটি ইতিমধ্যে তালিকায় রয়েছে!" else "Proxy already in list!"
            BatchManager.postSnackbar(msg)
            return false
        }
        current.add(trimmed)
        _proxyList.value = current
        saveProxiesToPrefs(current)
        val msg = if (_selectedLanguage.value == "bn")
            "প্রক্সি যোগ করা হয়েছে (${current.size}টি সক্রিয়)"
        else
            "Proxy added (${current.size} active)"
        BatchManager.postSnackbar(msg)
        return true
    }

    fun removeProxy(index: Int) {
        val current = _proxyList.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _proxyList.value = current
            saveProxiesToPrefs(current)
            val updatedMap = _proxyTestResults.value.toMutableMap()
            updatedMap.remove(index)
            _proxyTestResults.value = updatedMap
            val msg = if (_selectedLanguage.value == "bn") "প্রক্সি মুছে ফেলা হয়েছে" else "Proxy removed"
            BatchManager.postSnackbar(msg)
        }
    }

    fun clearAllProxies() {
        _proxyList.value = emptyList()
        _proxyTestResults.value = emptyMap()
        saveProxiesToPrefs(emptyList())
        val msg = if (_selectedLanguage.value == "bn") "সব প্রক্সি মুছে ফেলা হয়েছে" else "All proxies cleared"
        BatchManager.postSnackbar(msg)
    }

    fun testProxyAt(index: Int) {
        val list = _proxyList.value
        val proxyStr = list.getOrNull(index) ?: return
        val parsed = HttpEngine.parseProxy(proxyStr) ?: return
        viewModelScope.launch {
            _testingProxyIndex.value = index
            val (success, msg) = HttpEngine.testProxy(parsed)
            _testingProxyIndex.value = null
            val res = if (success) "✅ $msg" else "❌ $msg"
            val map = _proxyTestResults.value.toMutableMap()
            map[index] = res
            _proxyTestResults.value = map
            BatchManager.postSnackbar(res)
        }
    }

    private fun saveProxiesToPrefs(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        val joined = list.joinToString("\n")
        prefs.edit()
            .putString("proxy_list_json", arr.toString())
            .putString("proxy_input", joined)
            .apply()
    }

    // --- Core Password & Batch Settings ---
    fun updatePassword(password: String) {
        _passwordInput.value = password
        prefs.edit().putString("password", password).apply()
    }

    fun generateRandomPassword() {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$"
        val random = SecureRandom()
        val sb = StringBuilder()
        repeat(12) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        val pwd = sb.toString()
        updatePassword(pwd)
        BatchManager.postSnackbar(
            if (_selectedLanguage.value == "bn") "নতুন পাসওয়ার্ড তৈরি হয়েছে: $pwd" else "Generated new password: $pwd"
        )
    }

    fun setBatchCount(count: Int) {
        _batchCount.value = count.coerceIn(1, 5)
    }

    fun startBatch() {
        // Check Access Key first: user requirement "kuno key thakbe na bosai te hoe pore save deya lagbe"
        val token = _mailApiToken.value.trim()
        if (token.isEmpty()) {
            val msg = if (_selectedLanguage.value == "bn")
                "⚠️ অনুগ্রহ করে সেটিংস পেজে গিয়ে আপনার Access Key বসিয়ে সেভ করুন!"
            else
                "⚠️ Please configure your Access Key in Settings before starting!"
            BatchManager.postSnackbar(msg)
            _selectedTab.value = 3 // Switch to Settings tab!
            return
        }

        // Check Cookies
        val validCookies = _cookieList.value.filter { validateSingleCookie(it).isValid }
        if (validCookies.isEmpty()) {
            val msg = if (_selectedLanguage.value == "bn")
                "⚠️ অনুগ্রহ করে কমপক্ষে একটি সক্রিয় কুকি প্রদান করুন!"
            else
                "⚠️ Please provide at least one valid cookie!"
            BatchManager.postSnackbar(msg)
            return
        }

        if (BatchManager.cooldownSeconds.value > 0) {
            BatchManager.postSnackbar(
                if (_selectedLanguage.value == "bn")
                    "অনুগ্রহ করে কুলডাউন শেষ হওয়া পর্যন্ত অপেক্ষা করুন (${BatchManager.cooldownSeconds.value}s)"
                else
                    "Please wait for cooldown to finish (${BatchManager.cooldownSeconds.value}s)"
            )
            return
        }

        if (BatchManager.isBatchRunning.value) return

        val count = _batchCount.value
        val password = _passwordInput.value.ifBlank { "@Suhan1234" }
        val mailBase = _mailBaseUrl.value.ifBlank { TempMailService.DEFAULT_MAIL_BASE }
        val lang = _selectedLanguage.value

        val combinedCookies = validCookies.joinToString("\n")
        val combinedProxies = _proxyList.value.joinToString("\n")

        _selectedTab.value = 1 // Switch to Live Batch tab

        // Start Foreground Service with WakeLock
        BatchService.startBatch(
            context = getApplication(),
            cookie = combinedCookies,
            password = password,
            batchCount = count,
            mailBase = mailBase,
            mailToken = token,
            language = lang,
            proxies = combinedProxies
        )
    }

    fun cancelBatch() {
        BatchService.stopBatch(getApplication())
        BatchManager.postSnackbar(
            if (_selectedLanguage.value == "bn") "ব্যাচ বন্ধ করা হয়েছে" else "Batch stopped by user"
        )
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            dao.deleteAccount(account)
            BatchManager.postSnackbar(
                if (_selectedLanguage.value == "bn") "অ্যাকাউন্ট মুছে ফেলা হয়েছে" else "Account deleted"
            )
        }
    }

    fun clearAllAccounts() {
        viewModelScope.launch {
            dao.clearAll()
            BatchManager.postSnackbar(
                if (_selectedLanguage.value == "bn") "সব অ্যাকাউন্ট মুছে ফেলা হয়েছে" else "All accounts cleared"
            )
        }
    }

    fun getExportText(): String {
        val list = allAccounts.value
        val sb = StringBuilder()
        for (acc in list) {
            val nameSuffix = if (acc.name.isNotEmpty()) ":${acc.name}" else ""
            val dobSuffix = if (acc.dob.isNotEmpty()) ":${acc.dob}" else ""
            sb.append("${acc.email}:${acc.password}:${acc.uid}$nameSuffix$dobSuffix\n")
        }
        return sb.toString()
    }
}
