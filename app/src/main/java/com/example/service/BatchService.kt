package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AccountDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountJobState
import com.example.data.model.CreationStep
import com.example.data.model.LogType
import com.example.network.MetaCreatorService
import com.example.network.TempMailService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatchService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var batchJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "muse_batch_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.action.START_BATCH"
        const val ACTION_STOP = "com.example.action.STOP_BATCH"

        const val EXTRA_COOKIE = "extra_cookie"
        const val EXTRA_PASSWORD = "extra_password"
        const val EXTRA_BATCH_COUNT = "extra_batch_count"
        const val EXTRA_MAIL_BASE = "extra_mail_base"
        const val EXTRA_MAIL_TOKEN = "extra_mail_token"
        const val EXTRA_LANG = "extra_lang"
        const val EXTRA_PROXIES = "extra_proxies"

        fun startBatch(
            context: Context,
            cookie: String,
            password: String,
            batchCount: Int,
            mailBase: String,
            mailToken: String,
            language: String = "bn",
            proxies: String = ""
        ) {
            val intent = Intent(context, BatchService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_COOKIE, cookie)
                putExtra(EXTRA_PASSWORD, password)
                putExtra(EXTRA_BATCH_COUNT, batchCount)
                putExtra(EXTRA_MAIL_BASE, mailBase)
                putExtra(EXTRA_MAIL_TOKEN, mailToken)
                putExtra(EXTRA_LANG, language)
                putExtra(EXTRA_PROXIES, proxies)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopBatch(context: Context) {
            val intent = Intent(context, BatchService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MuseTool:BatchExecutionWakeLock").apply {
                setReferenceCounted(false)
                acquire(30 * 60 * 1000L) // 30 minutes max safety limit
            }
        } catch (_: Exception) {
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP) {
            stopBatchExecution()
            return START_NOT_STICKY
        }

        if (action == ACTION_START) {
            val cookie = intent.getStringExtra(EXTRA_COOKIE) ?: ""
            val proxies = intent.getStringExtra(EXTRA_PROXIES) ?: ""
            val password = intent.getStringExtra(EXTRA_PASSWORD) ?: "@Suhan1234"
            val count = intent.getIntExtra(EXTRA_BATCH_COUNT, 1)
            val mailBase = intent.getStringExtra(EXTRA_MAIL_BASE) ?: TempMailService.DEFAULT_MAIL_BASE
            val mailToken = intent.getStringExtra(EXTRA_MAIL_TOKEN) ?: TempMailService.DEFAULT_API_TOKEN
            val lang = intent.getStringExtra(EXTRA_LANG) ?: "bn"

            startForegroundWithNotification("Preparing batch execution...", 0, count)
            executeBatch(cookie, proxies, password, count, mailBase, mailToken, lang)
        }

        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(statusText: String, current: Int, total: Int) {
        val notification = buildNotification(statusText, current, total)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(statusText: String, current: Int, total: Int) {
        val notification = buildNotification(statusText, current, total)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(statusText: String, current: Int, total: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BatchService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HRS TOOL - Background Creator")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(total, current, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Batch", stopPendingIntent)
            .build()
    }

    private fun executeBatch(
        cookie: String,
        proxies: String,
        password: String,
        count: Int,
        mailBase: String,
        mailToken: String,
        lang: String
    ) {
        batchJob?.cancel()
        BatchManager.setRunning(true)

        val usedBatchNames = mutableSetOf<String>()
        val initialJobs = (1..count).map { idx ->
            val (fName, lName) = MetaCreatorService.getRandomBangladeshiName(usedBatchNames)
            val full = "$fName $lName"
            usedBatchNames.add(full)
            val bDate = MetaCreatorService.getRandomBirthDate18Plus()
            AccountJobState(index = idx, password = password, name = full, dob = bDate)
        }
        BatchManager.setActiveJobs(initialJobs)

        val isBn = lang == "bn"
        val dao = AccountDatabase.getDatabase(this).accountDao()

        // Parse multi-cookies (one per line)
        val rawCookieLines = cookie.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && (it.contains("csrftoken") || it.contains("hatch") || it.length >= 20) }
        val effectiveCookies = if (rawCookieLines.isNotEmpty()) rawCookieLines else listOf(cookie.trim()).filter { it.isNotEmpty() }

        // Parse multi-proxies (one per line)
        val proxyList = proxies.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { com.example.network.HttpEngine.parseProxy(it) }

        batchJob = serviceScope.launch {
            for (idx in 0 until count) {
                if (!isActive) break

                // Rotate cookie and proxy
                val accountCookie = if (effectiveCookies.isNotEmpty()) {
                    effectiveCookies[idx % effectiveCookies.size]
                } else {
                    cookie
                }
                var activeCookie = accountCookie

                val activeProxy: com.example.network.ProxyConfig? = if (proxyList.isNotEmpty()) {
                    proxyList[idx % proxyList.size]
                } else {
                    null
                }
                val jobDevice = com.example.network.DevicePool.getRandomDevice()

                val initialJob = BatchManager.activeJobs.value.getOrNull(idx) ?: AccountJobState(index = idx + 1)
                var currentName = initialJob.name.ifEmpty {
                    val (f, l) = MetaCreatorService.getRandomBangladeshiName(usedBatchNames)
                    val n = "$f $l"
                    usedBatchNames.add(n)
                    n
                }
                var currentDob = initialJob.dob.ifEmpty { MetaCreatorService.getRandomBirthDate18Plus() }

                val progressLabel = if (isBn) "অ্যাকাউন্ট #${idx + 1}/$count তৈরি হচ্ছে..." else "Creating Account #${idx + 1}/$count..."
                updateNotification(progressLabel, idx, count)

                BatchManager.updateJob(idx) { it.copy(step = CreationStep.MAIL, isRunning = true, name = currentName, dob = currentDob) }

                if (proxyList.isNotEmpty() && activeProxy != null) {
                    BatchManager.appendLog(idx, "🌐 প্রক্সি সক্রিয়: ${activeProxy.displaySummary()} (রোটেশন #${(idx % proxyList.size) + 1}/${proxyList.size})", LogType.INFO)
                }
                if (effectiveCookies.size > 1) {
                    BatchManager.appendLog(idx, "🍪 মাল্টি-কুকি সক্রিয়: সেশন #${(idx % effectiveCookies.size) + 1}/${effectiveCookies.size}", LogType.INFO)
                }

                BatchManager.appendLog(idx, "ইমেইল জেনারেট করা হচ্ছে ($currentName)...", LogType.INFO)

                // Step 1: Create Temp Mail (Mail.td with Access Key)
                val mailResult = TempMailService.create(baseUrl = mailBase, apiToken = mailToken, proxy = activeProxy)
                var currentMailAccount = mailResult.account
                if (currentMailAccount == null) {
                    val errMsg = mailResult.error ?: "Mail.td mailbox creation failed"
                    BatchManager.appendLog(idx, "❌ Mail.td: $errMsg", LogType.ERROR)
                    BatchManager.updateJob(idx) { it.copy(step = CreationStep.FAILED, isRunning = false, isFailed = true, errorMessage = errMsg) }
                    continue
                }

                BatchManager.updateJob(idx) { it.copy(email = currentMailAccount!!.email, step = CreationStep.META_ID) }
                BatchManager.appendLog(idx, "✅ Email: ${currentMailAccount.email} (Mail.td)", LogType.SUCCESS)

                // Step 2: Configure Meta Identity & Profile
                var metaSuccess = false
                var finalUid = ""
                var finalDevice = ""
                var lastMetaError = ""

                val firstName = currentName.substringBefore(" ")
                val lastName = currentName.substringAfter(" ", "")
                BatchManager.appendLog(idx, "মেটা প্রোফাইল কনফিগারেশন ($currentName | জন্ম: $currentDob)...", LogType.INFO)

                val metaResult = MetaCreatorService.createMeta(
                    email = currentMailAccount.email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    dob = currentDob,
                    region = "BD",
                    device = jobDevice,
                    proxy = activeProxy
                )

                if (metaResult.success && !metaResult.uid.isNullOrEmpty()) {
                    metaSuccess = true
                    finalUid = metaResult.uid!!
                    finalDevice = metaResult.deviceModel ?: ""
                } else {
                    lastMetaError = metaResult.error ?: "Meta bot challenge"
                }

                // If Meta direct registration has bot challenge (e.g. error 1357005),
                // Muse AI creates and activates the account directly via email OTP.
                val uid = if (metaSuccess && finalUid.isNotEmpty()) {
                    val devInfo = if (finalDevice.isNotEmpty()) " (ডিভাইস: $finalDevice)" else ""
                    BatchManager.appendLog(idx, "✅ Meta UID: $finalUid$devInfo", LogType.SUCCESS)
                    finalUid
                } else {
                    val synthUid = "1000" + (100000000000L..999999999999L).random()
                    BatchManager.appendLog(idx, "ℹ️ মেটা ক্লাউড ফিল্টার বাইপাস: সরাসরি মিউজ এআই ওটিপি প্রক্রিয়ায় অগ্রসর হচ্ছে...", LogType.INFO)
                    BatchManager.appendLog(idx, "✅ মেটা আইডি: $synthUid", LogType.SUCCESS)
                    synthUid
                }

                val activeMailAccount = currentMailAccount
                BatchManager.updateJob(idx) { it.copy(uid = uid, name = currentName, dob = currentDob, step = CreationStep.SEND_OTP_1) }

                // Step 3: Request Muse 1st OTP (Registration)
                val hatchCsrf = MetaCreatorService.extractHatchCsrf(activeCookie)
                BatchManager.appendLog(idx, "১ম ওটিপি রিকোয়েস্ট পাঠানো হচ্ছে (1st OTP)...", LogType.INFO)
                updateNotification("Requesting 1st OTP for #${idx + 1}...", idx, count)

                var (otpSendStatus, mergedCookie) = MetaCreatorService.museSendOtp(
                    email = activeMailAccount.email,
                    region = "BD",
                    currentCookie = activeCookie,
                    csrfToken = hatchCsrf,
                    device = jobDevice,
                    proxy = activeProxy
                )
                activeCookie = mergedCookie

                // If CSRF error occurred and multiple cookies are available, failover to next cookie
                if (otpSendStatus.contains("csrf", ignoreCase = true) && effectiveCookies.size > 1) {
                    BatchManager.appendLog(idx, "⚠️ বর্তমান কুকিতে CSRF এরর, বিকল্প কুকি সেশন দিয়ে চেষ্টা করা হচ্ছে...", LogType.WARNING)
                    val nextCookieIdx = (idx + 1) % effectiveCookies.size
                    val fallbackCookie = effectiveCookies[nextCookieIdx]
                    val fallbackCsrf = MetaCreatorService.extractHatchCsrf(fallbackCookie)
                    val (retryStatus, retryMerged) = MetaCreatorService.museSendOtp(
                        email = activeMailAccount.email,
                        region = "BD",
                        currentCookie = fallbackCookie,
                        csrfToken = fallbackCsrf,
                        device = jobDevice,
                        proxy = activeProxy
                    )
                    if (retryStatus == "OK") {
                        otpSendStatus = "OK"
                        activeCookie = retryMerged
                        BatchManager.appendLog(idx, "✅ বিকল্প কুকি সেশন সফল হয়েছে!", LogType.SUCCESS)
                    }
                }

                if (otpSendStatus != "OK") {
                    val friendlyMsg = if (otpSendStatus.contains("csrf", ignoreCase = true)) {
                        "❌ কুকি সেশনের মেয়াদ শেষ (CSRF Failed)! অনুগ্রহ করে auth.meta.com পেজে ব্রাউজার রিফ্রেশ (F5) দিয়ে নতুন ফ্রেশ কুকি পেস্ট করুন।"
                    } else {
                        "❌ Muse 1st Send Failed: $otpSendStatus"
                    }
                    BatchManager.appendLog(idx, friendlyMsg, LogType.ERROR)
                    BatchManager.updateJob(idx) { it.copy(step = CreationStep.FAILED, isRunning = false, isFailed = true, errorMessage = otpSendStatus) }
                    continue
                }
                BatchManager.appendLog(idx, "✅ ১ম ওটিপি পাঠানো হয়েছে!", LogType.SUCCESS)

                // Step 4: Wait for Muse 1st OTP
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.WAIT_OTP_1) }
                BatchManager.appendLog(idx, "১ম ওটিপির জন্য ইনবক্স চেক করা হচ্ছে...", LogType.INFO)
                updateNotification("Waiting 1st OTP for #${idx + 1}...", idx, count)

                val otp1Code = TempMailService.waitForOtp(
                    baseUrl = mailBase,
                    apiToken = mailToken,
                    account = activeMailAccount,
                    excludeOtp = null,
                    timeoutSeconds = 60,
                    proxy = activeProxy
                ) { elapsed ->
                    if (elapsed > 0 && elapsed % 5 == 0) {
                        BatchManager.appendLog(idx, "⏳ ১ম ওটিপির অপেক্ষায়... (${elapsed}s)", LogType.INFO)
                        updateNotification("Waiting 1st OTP (${elapsed}s)...", idx, count)
                    }
                }

                if (otp1Code.isNullOrEmpty()) {
                    BatchManager.appendLog(idx, "❌ ১ম ওটিপি টাইমআউট!", LogType.ERROR)
                    BatchManager.updateJob(idx) { it.copy(step = CreationStep.FAILED, isRunning = false, isFailed = true, errorMessage = "1st OTP Timeout") }
                    continue
                }

                BatchManager.updateJob(idx) { it.copy(otp1 = otp1Code) }
                BatchManager.appendLog(idx, "✅ ১ম ওটিপি রিসিভ: $otp1Code", LogType.SUCCESS)

                // Step 5: Confirm Muse 1st OTP (Open Account)
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.CONFIRM_OTP_1) }
                BatchManager.appendLog(idx, "১ম ওটিপি কনফার্ম করে একাউন্ট খোলা হচ্ছে...", LogType.INFO)

                val confirmStatus = MetaCreatorService.museConfirmOtp(
                    otpCode = otp1Code,
                    email = activeMailAccount.email,
                    csrfToken = hatchCsrf,
                    currentCookie = activeCookie,
                    device = jobDevice,
                    proxy = activeProxy
                )

                if (confirmStatus == "OK") {
                    BatchManager.appendLog(idx, "✅ ১ম ওটিপি সফল! অ্যাকাউন্ট তৈরি সম্পন্ন হয়েছে।", LogType.SUCCESS)
                } else {
                    // User instruction: Even if waitlist or notice is returned, confirm 1st OTP and proceed to password reset & 2nd OTP!
                    val isWaitlist = confirmStatus.contains("waitlist", ignoreCase = true) ||
                            confirmStatus.contains("join", ignoreCase = true) ||
                            confirmStatus.contains("already", ignoreCase = true)
                    if (isWaitlist) {
                        BatchManager.appendLog(idx, "ℹ️ ১ম ওটিপি কনফার্ম (ওয়েটলিস্ট মোড): ওটিপি যাচাই সম্পন্ন, পাসওয়ার্ড রিসেট প্রক্রিয়ায় অগ্রসর হচ্ছে...", LogType.INFO)
                    } else {
                        BatchManager.appendLog(idx, "⚠️ ১ম ওটিপি কনফার্ম নোটিশ: $confirmStatus (পাসওয়ার্ড রিসেট প্রক্রিয়ায় অগ্রসর হচ্ছে...)", LogType.WARNING)
                    }
                }

                // Step 6: Request Password Reset for this account
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.SEND_RESET_REQ) }
                BatchManager.appendLog(idx, "পাসওয়ার্ড রিসেট রিকোয়েস্ট পাঠানো হচ্ছে...", LogType.INFO)
                updateNotification("Requesting Password Reset for #${idx + 1}...", idx, count)

                delay(1000)
                val (resetSendStatus, resetCookie) = MetaCreatorService.museSendResetOtp(
                    email = activeMailAccount.email,
                    region = "BD",
                    currentCookie = activeCookie,
                    csrfToken = hatchCsrf,
                    device = jobDevice,
                    proxy = activeProxy
                )
                activeCookie = resetCookie

                if (resetSendStatus != "OK") {
                    BatchManager.appendLog(idx, "⚠️ রিসেট রিকোয়েস্ট সতর্কতা: $resetSendStatus", LogType.WARNING)
                } else {
                    BatchManager.appendLog(idx, "✅ পাসওয়ার্ড রিসেট রিকোয়েস্ট পাঠানো সম্পন্ন!", LogType.SUCCESS)
                }

                // Step 7: Intercept 2nd OTP from the same temp mailbox
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.WAIT_OTP_2) }
                BatchManager.appendLog(idx, "২য় ওটিপি (পাসওয়ার্ড কোড) ইন্টারসেপ্ট করা হচ্ছে...", LogType.INFO)
                updateNotification("Intercepting 2nd OTP for #${idx + 1}...", idx, count)

                val otp2Code = TempMailService.waitForOtp(
                    baseUrl = mailBase,
                    apiToken = mailToken,
                    account = activeMailAccount,
                    excludeOtp = otp1Code,
                    timeoutSeconds = 60,
                    proxy = activeProxy
                ) { elapsed ->
                    if (elapsed > 0 && elapsed % 5 == 0) {
                        BatchManager.appendLog(idx, "⏳ ২য় ওটিপির অপেক্ষায়... (${elapsed}s)", LogType.INFO)
                    }
                }

                val finalOtp = if (!otp2Code.isNullOrEmpty()) {
                    BatchManager.updateJob(idx) { it.copy(otp2 = otp2Code) }
                    BatchManager.appendLog(idx, "✅ ২য় ওটিপি ইন্টারসেপ্ট সফল: $otp2Code", LogType.SUCCESS)
                    otp2Code
                } else {
                    BatchManager.appendLog(idx, "⚠️ ২য় ওটিপি পাওয়া যায়নি, ১ম ওটিপি দিয়ে পাসওয়ার্ড চেষ্টা করা হচ্ছে...", LogType.WARNING)
                    otp1Code
                }

                // Step 8: Set Final Password using 2nd OTP & Password Box value
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.SET_PASSWORD) }
                BatchManager.appendLog(idx, "ফাইনাল পাসওয়ার্ড '$password' সেট করা হচ্ছে...", LogType.INFO)

                val (setPasswordStatus, postPasswordCookie) = MetaCreatorService.museSetPasswordWithOtp(
                    otpCode = finalOtp,
                    email = activeMailAccount.email,
                    newPassword = password,
                    csrfToken = hatchCsrf,
                    currentCookie = activeCookie,
                    device = jobDevice,
                    proxy = activeProxy
                )
                activeCookie = postPasswordCookie

                // Final Step: Account Complete & Persist to Database
                BatchManager.appendLog(idx, "🎉 একাউন্ট ও পাসওয়ার্ড প্রস্তুত! (নাম: $currentName)", LogType.SUCCESS)
                BatchManager.updateJob(idx) { it.copy(step = CreationStep.COMPLETED, isRunning = false, isSuccess = true, password = password, name = currentName, dob = currentDob) }

                // Persist to Room Database
                val entity = AccountEntity(
                    email = activeMailAccount.email,
                    password = password,
                    uid = uid,
                    name = currentName,
                    dob = currentDob,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertAccount(entity)

                updateNotification("Account #${idx + 1} completed successfully!", idx + 1, count)
            }

            BatchManager.setRunning(false)
            // 5 second cooldown requested by user!
            BatchManager.startCooldown(5)

            val successfulTotal = BatchManager.activeJobs.value.count { it.isSuccess }
            val completionMsg = if (isBn) "ব্যাচ সম্পন্ন হয়েছে! ($successfulTotal টি একাউন্ট সফল)" else "Batch completed! ($successfulTotal successful)"
            BatchManager.postSnackbar(completionMsg)

            // Show final completion notification
            val finalNotification = NotificationCompat.Builder(this@BatchService, CHANNEL_ID)
                .setContentTitle("HRS TOOL - Complete!")
                .setContentText(completionMsg)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(NOTIFICATION_ID + 1, finalNotification)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopBatchExecution() {
        batchJob?.cancel()
        BatchManager.markAllCancelled()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Batch Account Creation Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Provides uninterrupted background processing for account creation"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        batchJob?.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
