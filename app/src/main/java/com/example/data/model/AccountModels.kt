package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "created_accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val uid: String,
    val name: String = "",
    val dob: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class LogType {
    INFO,
    SUCCESS,
    ERROR,
    WARNING
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

enum class CreationStep(val labelEn: String, val labelBn: String) {
    INIT("Preparing", "প্রস্তুতি চলছে"),
    MAIL("Creating Temp Mail", "অস্থায়ী ইমেল তৈরি হচ্ছে"),
    SEND_OTP_1("Sending 1st OTP", "১ম ওটিপি পাঠানো হচ্ছে"),
    WAIT_OTP_1("Waiting for 1st OTP", "১ম ওটিপির অপেক্ষায়"),
    CONFIRM_OTP_1("Verifying 1st OTP", "১ম ওটিপি ভেরিফাই সম্পন্ন"),
    PROFILE_SETUP("Setting Bangladeshi Profile", "বাংলাদেশি প্রোফাইল সেট হচ্ছে"),
    META_ID("Registering Meta ID", "মেটা আইডি রেজিস্ট্রেশন"),
    SEND_RESET_REQ("Requesting Password Reset", "পাসওয়ার্ড রিসেট রিকোয়েস্ট"),
    WAIT_OTP_2("Intercepting 2nd OTP", "২য় ওটিপি ইন্টারসেপ্ট হচ্ছে"),
    SET_PASSWORD("Setting Final Password", "পাসওয়ার্ড সেট করা হচ্ছে"),
    COMPLETED("Completed Successfully", "সফলভাবে সম্পন্ন হয়েছে"),
    FAILED("Creation Failed", "ব্যর্থ হয়েছে")
}

data class AccountJobState(
    val index: Int,
    val email: String = "",
    val password: String = "",
    val uid: String = "",
    val name: String = "",
    val dob: String = "",
    val otp1: String = "",
    val otp2: String = "",
    val step: CreationStep = CreationStep.INIT,
    val isRunning: Boolean = true,
    val isSuccess: Boolean = false,
    val isFailed: Boolean = false,
    val errorMessage: String? = null,
    val logs: List<LogEntry> = emptyList()
)

data class CookieValidation(
    val isValid: Boolean = false,
    val hasCsrfToken: Boolean = false,
    val hasHatchCsrf: Boolean = false,
    val length: Int = 0,
    val message: String = "",
    val validCookieCount: Int = 0
)

