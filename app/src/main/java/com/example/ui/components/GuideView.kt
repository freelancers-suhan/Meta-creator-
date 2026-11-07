package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun GuideView(
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("guide_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "HRS TOOL গাইড ও নির্দেশিকা" else "HRS TOOL Guide & Instructions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isBengali) "মোবাইল ফ্রেন্ডলি অটোমেটেড সিস্টেম" else "Mobile-Friendly Automated System",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBengali)
                            "এই অ্যাপটি মেটা (Meta) এবং মিউজ (Muse) একাউন্ট স্বয়ংক্রিয়ভাবে তৈরি করতে ব্যবহৃত হয়। এতে রয়েছে স্বয়ংক্রিয় টেম্প মেইল ও ওটিপি ভেরিফিকেশন ব্যবস্থা।"
                        else
                            "This tool automates Meta and Muse account creation with instant temporary mail allocation and automated OTP retrieval.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // How to Get Cookie Card
        item {
            GuideStepCard(
                stepNumber = "১",
                icon = Icons.Default.Key,
                title = if (isBengali) "মেটা কুকি সংগ্রহ করার নিয়ম" else "How to Obtain Meta Cookie",
                color = EmeraldPrimary,
                content = if (isBengali)
                    "১. যেকোনো ব্রাউজারে (যেমন Kiwi Browser বা Chrome) auth.meta.com এ যান।\n" +
                    "২. ডেভেলপার টুলস (Inspect Element / Network tab) ওপেন করুন।\n" +
                    "৩. যেকোনো রিকোয়েস্টের Request Headers থেকে পূর্ণ Cookie কপি করুন।\n" +
                    "৪. কুকিতে অবশ্যই 'csrftoken' এবং 'hatch_native_auth_csrf' মান দুটি থাকতে হবে।"
                else
                    "1. Visit auth.meta.com on Kiwi Browser or PC Chrome.\n" +
                    "2. Open Developer Tools (Inspect -> Network Tab).\n" +
                    "3. Copy the full 'Cookie' header from the request headers.\n" +
                    "4. Ensure it contains both 'csrftoken' and 'hatch_native_auth_csrf'."
            )
        }

        // Automated Workflow Card
        item {
            GuideStepCard(
                stepNumber = "২",
                icon = Icons.Default.Mail,
                title = if (isBengali) "স্বয়ংক্রিয় কাজের প্রক্রিয়া (২-স্টেপ ওটিপি ও পাসওয়ার্ড সেট)" else "Automated Workflow (2-Step OTP & Password Reset)",
                color = CyanAccent,
                content = if (isBengali)
                    "১. টেম্প মেইল তৈরি: api.mail.td এর মাধ্যমে স্বয়ংক্রিয়ভাবে একটি অস্থায়ী ইনবক্স তৈরি হয়।\n" +
                    "২. মেটা রেজিস্ট্রেশন: মেটা সার্ভিসে একাউন্ট আইডি রেজিস্টার করা হয়।\n" +
                    "৩. ১ম ওটিপি ভেরিফিকেশন: মিউজ এআই থেকে ১ম ওটিপি রিকোয়েস্ট পাঠিয়ে ইনবক্স থেকে ইন্টারসেপ্ট করে একাউন্ট ওপেন সম্পন্ন হয়।\n" +
                    "৪. পাসওয়ার্ড রিসেট রিকোয়েস্ট: অ্যাপটি সরাসরি উক্ত একাউন্টের পাসওয়ার্ড রিসেট রিকোয়েস্ট পাঠায়।\n" +
                    "৫. ২য় ওটিপি ইন্টারসেপ্ট: একই টেম্প মেইলে আসা দ্বিতীয় ওটিপি ইন্টারসেপ্ট করা হয়।\n" +
                    "৬. ফাইনাল পাসওয়ার্ড সেট: অ্যাপের পাসওয়ার্ড বক্সে থাকা পাসওয়ার্ডটি ২য় ওটিপির মাধ্যমে একাউন্টে স্থায়ীভাবে সেট করে ডাটাবেজে সংরক্ষণ করা হয়।"
                else
                    "1. Temp Mail: Instantly creates a temporary mailbox via api.mail.td.\n" +
                    "2. Meta Registration: Registers initial account with Meta.\n" +
                    "3. 1st OTP Verification: Requests 1st verification OTP, intercepts from mailbox, and opens the account.\n" +
                    "4. Password Reset Request: Directly initiates a password reset request for the account.\n" +
                    "5. 2nd OTP Interception: Intercepts the second verification code arriving at the same mailbox.\n" +
                    "6. Final Password Set: Applies the final password from your password box using the 2nd OTP and stores the account in Room database."
            )
        }

        // Multi-Proxy & Multi-Cookie Guide
        item {
            GuideStepCard(
                stepNumber = "৩",
                icon = Icons.Default.Security,
                title = if (isBengali) "মাল্টি-কুকিজ ও মাল্টি-প্রক্সি দিয়ে লিমিট বাইপাস" else "Multi-Cookie & Multi-Proxy Tips",
                color = CyanAccent,
                content = if (isBengali)
                    "• মাল্টি-কুকিজ: কুকি বক্সে প্রতি লাইনে একটি করে একাধিক কুকি পেস্ট করতে পারেন। প্রতি একাউন্টের জন্য আলাদা কুকি সেশন ব্যবহৃত হওয়ায় কোনো একটি কুকি ব্লক হলেও বাকিগুলো সচল থাকবে।\n" +
                    "• মাল্টি-প্রক্সি: প্রক্সি বক্সে প্রতি লাইনে একটি করে প্রক্সি (host:port অথবা host:port:user:pass) দিন। প্রতিটি একাউন্ট ক্রমান্বয়ে আলাদা আইপি দিয়ে তৈরি হবে।\n" +
                    "• সিম ডাটা বা এরোপ্লেন মোড: ১০-১৫টি একাউন্টের পর ৫ সেকেন্ড এরোপ্লেন মোড অন/অফ করলে নতুন আইপি পাওয়া যায়।"
                else
                    "• Multi-Cookies: Paste multiple cookies (one per line). The app rotates sessions across accounts so one expired cookie won't stop the batch.\n" +
                    "• Multi-Proxies: Paste proxies (one per line: host:port or host:port:user:pass). The app automatically rotates IPs per account created.\n" +
                    "• Airplane Mode: Toggling airplane mode for 5s refreshes mobile IP and bypasses Meta rate limits."
            )
        }

        // Common Errors & Troubleshooting Card
        item {
            GuideStepCard(
                stepNumber = "৪",
                icon = Icons.Default.Warning,
                title = if (isBengali) "সমস্যা ও সমাধান (Troubleshooting)" else "Troubleshooting & FAQs",
                color = WarningAmber,
                content = if (isBengali)
                    "• 'Empty Response (FB Blocked)': এর মানে আইপি বা কুকি মেটা দ্বারা সাময়িক ব্লক হয়েছে। একটি নতুন কুকি নিন অথবা প্রক্সি/VPN ব্যবহার করুন।\n" +
                    "• 'Missing CSRF Token': কপি করা কুকিতে csrftoken মিসিং। পুনরায় পূর্ণ হেডার কপি করুন।\n" +
                    "• 'OTP Timeout': সাময়িক মেইল সার্ভার ব্যস্ত থাকলে কিছুটা সময় লাগতে পারে।\n" +
                    "• ৫ সেকেন্ড কুলডাউন: মেটার রেট লিমিট এড়াতে প্রতি ব্যাচের মাঝে ৫ সেকেন্ড বিরতি রাখা হয়েছে।"
                else
                    "• 'Empty Response (FB Blocked)': Meta rate-limited the IP or session. Use fresh cookies or proxy.\n" +
                    "• 'Missing CSRF Token': Ensure you copied the entire Cookie header including csrftoken.\n" +
                    "• 'OTP Timeout': Temporary mail provider delayed, try again.\n" +
                    "• 5s Cooldown: Rapid 5-second interval between batches."
            )
        }
    }
}

@Composable
fun GuideStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    color: Color,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}
