package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsView(
    savedAccessKey: String,
    proxyList: List<String>,
    proxyTestResults: Map<Int, String>,
    testingProxyIndex: Int?,
    isBengali: Boolean,
    onSaveAccessKey: (String) -> Unit,
    onAddProxy: (String) -> Boolean,
    onRemoveProxy: (Int) -> Unit,
    onClearAllProxies: () -> Unit,
    onTestProxyAt: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var accessKeyInput by remember(savedAccessKey) { mutableStateOf(savedAccessKey) }
    var keyVisible by remember { mutableStateOf(false) }

    var newProxyInput by remember { mutableStateOf("") }
    var guideExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBengali) "অ্যাপ সেটিংস ও কনফিগারেশন" else "App Settings & Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBengali) "Access Key ও প্রক্সি ম্যানেজমেন্ট" else "Access Key & Proxy Management",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // Section 1: Access Key (Mail.td)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("access_key_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, if (savedAccessKey.isNotEmpty()) EmeraldPrimary.copy(alpha = 0.5f) else BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "Access Key (Mail.td)" else "Access Key (Mail.td)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (savedAccessKey.isNotEmpty()) SuccessGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (savedAccessKey.isNotEmpty()) SuccessGreen.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (savedAccessKey.isNotEmpty())
                                    (if (isBengali) "সেভ করা আছে ✅" else "Saved ✅")
                                else
                                    (if (isBengali) "সেট করা নেই ⚠️" else "Not Set ⚠️"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (savedAccessKey.isNotEmpty()) SuccessGreen else AlertRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali)
                            "ডিসপোজেবল মেইলবক্সের জন্য আপনার Mail.td Access Key এখানে বসিয়ে 'সংরক্ষণ করুন' বাটনে ক্লিক করুন।"
                        else
                            "Enter your Mail.td Access Key below and click 'Save Access Key'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accessKeyInput,
                        onValueChange = { accessKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("access_key_input"),
                        placeholder = {
                            Text(
                                text = if (isBengali) "Access Key এখানে পেস্ট করুন..." else "Paste Access Key here...",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Access Key",
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboard.getText()?.text ?: ""
                                if (clip.isNotBlank()) accessKeyInput = clip.trim()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("paste_access_key_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "পেস্ট করুন" else "Paste",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Button(
                            onClick = {
                                onSaveAccessKey(accessKeyInput)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_access_key_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF00381B)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "সংরক্ষণ করুন" else "Save Key",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Multi-Proxy Management (One-by-one add/delete/test)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proxy_management_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, if (proxyList.isNotEmpty()) CyanAccent.copy(alpha = 0.4f) else BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "প্রক্সি ম্যানেজমেন্ট" else "Proxy Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (proxyList.isNotEmpty()) SuccessGreen.copy(alpha = 0.15f) else CyberSurfaceVariant,
                            border = BorderStroke(1.dp, if (proxyList.isNotEmpty()) SuccessGreen.copy(alpha = 0.4f) else BorderColor)
                        ) {
                            Text(
                                text = if (proxyList.isNotEmpty())
                                    (if (isBengali) "${proxyList.size}টি প্রক্সি সক্রিয়" else "${proxyList.size} Proxies Active")
                                else
                                    (if (isBengali) "ডাইরেক্ট (প্রক্সি নেই)" else "Direct (No Proxy)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (proxyList.isNotEmpty()) SuccessGreen else TextMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali)
                            "এখানে একটা একটা করে প্রক্সি এড করতে পারবেন। অ্যাকাউন্ট তৈরির সময় প্রতিটি প্রক্সি ক্রমান্বয়ে রোটেট হবে।"
                        else
                            "Add proxies one-by-one. Accounts will automatically rotate between added proxies during batch creation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input to Add Single Proxy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newProxyInput,
                            onValueChange = { newProxyInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_proxy_input"),
                            placeholder = {
                                Text(
                                    text = "host:port অথবা host:port:user:pass",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant,
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = {
                                if (newProxyInput.isNotBlank()) {
                                    val added = onAddProxy(newProxyInput)
                                    if (added) newProxyInput = ""
                                }
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("add_proxy_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = Color(0xFF002938)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "এড করুন" else "Add",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Paste shortcut row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboard.getText()?.text ?: ""
                                if (clip.isNotBlank()) newProxyInput = clip.trim()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBengali) "ক্লিপবোর্ড থেকে পেস্ট" else "Paste from clipboard",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }

                        if (proxyList.isNotEmpty()) {
                            TextButton(onClick = onClearAllProxies) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBengali) "সব মুছুন" else "Clear All",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlertRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Added Proxy List Items
        if (proxyList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberSurfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBengali) "কোনো প্রক্সি যোগ করা নেই" else "No proxies added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isBengali) "অ্যাকাউন্ট সরাসরি আপনার নিজস্ব আইপি দিয়ে তৈরি হবে।" else "Accounts will be created directly using current IP.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            itemsIndexed(proxyList) { index, proxyStr ->
                val isTesting = testingProxyIndex == index
                val testResult = proxyTestResults[index]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = proxyStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Test button
                                IconButton(
                                    onClick = { onTestProxyAt(index) },
                                    enabled = !isTesting,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = CyanAccent
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Test Proxy",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Delete button
                                IconButton(
                                    onClick = { onRemoveProxy(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Proxy",
                                        tint = AlertRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (testResult != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = testResult,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (testResult.startsWith("✅")) SuccessGreen else AlertRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Expandable Guide & Tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "নির্দেশিকা ও টিপস (Guide & Tips)" else "Guide & Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(onClick = { guideExpanded = !guideExpanded }) {
                            Icon(
                                imageVector = if (guideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Guide",
                                tint = TextSecondary
                            )
                        }
                    }

                    AnimatedVisibility(visible = guideExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TipItem(
                                title = if (isBengali) "মেটা কুকি সংগ্রহ করার নিয়ম" else "Obtain Meta Cookie",
                                text = if (isBengali)
                                    "auth.meta.com এ গিয়ে Inspect -> Network tab থেকে Cookie হেডার কপি করুন। অবশ্যই hatch_native_auth_csrf ও csrftoken থাকতে হবে।"
                                else
                                    "Visit auth.meta.com and copy Cookie header from Network tab with hatch_native_auth_csrf & csrftoken."
                            )

                            TipItem(
                                title = if (isBengali) "মাল্টি-কুকিজ সুবিধা" else "Multi-Cookie Benefit",
                                text = if (isBengali)
                                    "জেনারেটর পেজে (+) বাটনে ক্লিক করে একাধিক কুকি যোগ করতে পারবেন। ব্যাচ চলার সময় অ্যাকাউন্টগুলো স্বয়ংক্রিয়ভাবে আলাদা কুকি দিয়ে ঘুরিয়ে ফিরিয়ে তৈরি হবে।"
                                else
                                    "Click (+) on the Generator page to add multiple cookies. Accounts will rotate across them."
                            )

                            TipItem(
                                title = if (isBengali) "রেট লিমিট ও এরোপ্লেন মোড" else "Rate Limit & Airplane Mode",
                                text = if (isBengali)
                                    "১০-১৫টি একাউন্টের পর ৫ সেকেন্ড এরোপ্লেন মোড অন/অফ করলে নতুন আইপি পাওয়া যায় অথবা একাধিক প্রক্সি যোগ করুন।"
                                else
                                    "Toggle airplane mode for 5s to refresh mobile IP after 10-15 accounts, or use proxies."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TipItem(title: String, text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CyberSurfaceVariant,
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
