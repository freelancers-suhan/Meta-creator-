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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CookieCard(
    cookieList: List<String>,
    onUpdateCookieItem: (index: Int, String) -> Unit,
    onAddCookieItem: () -> Unit,
    onRemoveCookieItem: (index: Int) -> Unit,
    onSaveCookies: () -> Unit,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val validCount = cookieList.count { item ->
        val trimmed = item.trim()
        trimmed.length >= 20 && (trimmed.contains("hatch_native_auth_csrf") || trimmed.contains("csrftoken"))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cookie_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(
            1.dp,
            if (validCount > 0) EmeraldPrimary.copy(alpha = 0.6f) else BorderColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Cookie Icon",
                        tint = if (validCount > 0) EmeraldPrimary else Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "মেটা কুকিজ (Multi-Cookie)" else "Meta Cookies (Multi-Cookie)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (validCount > 0) SuccessGreen.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f)
                ) {
                    val badgeText = if (validCount > 0) {
                        if (isBengali) "$validCount টি কুকি সক্রিয়" else "$validCount Cookies Active"
                    } else {
                        if (isBengali) "কুকি প্রয়োজন ⚠️" else "Cookie Needed ⚠️"
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (validCount > 0) SuccessGreen else AlertRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isBengali)
                    "একটি কুকি এড করুন, পরে চাইলে (+) বাটনে চাপ দিয়ে আরেকটি করতে পারবেন। ব্যাচ চলার সময় প্রতিটি অ্যাকাউন্টে ক্রমান্বয়ে আলাদা কুকি ব্যবহৃত হবে।"
                else
                    "Add a cookie, then click (+) to add more. Accounts will automatically rotate between added cookies to bypass session limits.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cookie items list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cookieList.forEachIndexed { index, cookieValue ->
                    val trimmed = cookieValue.trim()
                    val hasHatch = trimmed.contains("hatch_native_auth_csrf")
                    val hasCsrf = trimmed.contains("csrftoken")
                    val isValidItem = trimmed.length >= 20 && (hasHatch || hasCsrf)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(
                            1.dp,
                            if (isValidItem) EmeraldPrimary.copy(alpha = 0.4f) else BorderColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Cookie Item Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isValidItem) EmeraldPrimary.copy(alpha = 0.2f) else CyberSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isValidItem) EmeraldPrimary else TextMuted
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBengali) "কুকি সেশন #${index + 1}" else "Cookie Session #${index + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isValidItem) SuccessGreen.copy(alpha = 0.15f) else CyberSurfaceVariant
                                    ) {
                                        Text(
                                            text = if (isValidItem)
                                                (if (isBengali) "সক্রিয় (${trimmed.length})" else "Active (${trimmed.length})")
                                            else if (trimmed.isEmpty())
                                                (if (isBengali) "খালি" else "Empty")
                                            else
                                                (if (isBengali) "অকার্যকর" else "Invalid"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isValidItem) SuccessGreen else TextMuted,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (cookieList.size > 1) {
                                        IconButton(
                                            onClick = { onRemoveCookieItem(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Cookie",
                                                tint = AlertRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cookieValue,
                                onValueChange = { onUpdateCookieItem(index, it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cookie_input_$index"),
                                placeholder = {
                                    Text(
                                        text = if (isBengali) "কুকি এখানে পেস্ট করুন (hatch_native_auth_csrf সহ)..." else "Paste full cookie header here...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CyberSurfaceVariant,
                                    unfocusedContainerColor = CyberSurfaceVariant,
                                    focusedBorderColor = if (isValidItem) EmeraldPrimary else CyanAccent,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick actions: Paste & Clear for this slot
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (cookieValue.isNotEmpty()) {
                                    TextButton(
                                        onClick = { onUpdateCookieItem(index, "") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBengali) "মুছুন" else "Clear",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val text = clipboard.getText()?.text ?: ""
                                        if (text.isNotBlank()) {
                                            onUpdateCookieItem(index, text.trim())
                                        }
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
                                        text = if (isBengali) "পেস্ট করুন" else "Paste",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: + Add Another Cookie & Save All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddCookieItem,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_cookie_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Cookie",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "+ আরেকটি কুকি" else "+ Add Cookie",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onSaveCookies,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_cookie_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (validCount > 0) EmeraldPrimary else Color(0xFF334155),
                        contentColor = if (validCount > 0) Color(0xFF00381B) else TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Cookies",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "কুকি সেভ করুন" else "Save Cookies",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
