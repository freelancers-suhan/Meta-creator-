package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountJobState
import com.example.data.model.CreationStep
import com.example.data.model.LogType
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CyberBg
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
fun LiveBatchDashboard(
    jobs: List<AccountJobState>,
    isRunning: Boolean,
    cooldownSeconds: Int,
    isBengali: Boolean,
    onCancelBatch: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = jobs.count { it.isSuccess }
    val failedCount = jobs.count { it.isFailed }
    val total = jobs.size.coerceAtLeast(1)
    val progress = (completedCount + failedCount).toFloat() / total.toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_batch_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Overview Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_status_overview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(
                    1.dp,
                    if (isRunning) CyanAccent.copy(alpha = 0.6f) else BorderColor
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isRunning) {
                                    if (isBengali) "ব্যাচ চলমান রয়েছে..." else "Batch In Progress..."
                                } else if (cooldownSeconds > 0) {
                                    if (isBengali) "কুলডাউন চলছে (${cooldownSeconds}s)" else "Cooling down (${cooldownSeconds}s)"
                                } else if (jobs.isNotEmpty()) {
                                    if (isBengali) "ব্যাচ সম্পন্ন" else "Batch Finished"
                                } else {
                                    if (isBengali) "কোনো ব্যাচ সক্রিয় নেই" else "No Active Batch"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) CyanAccent else if (cooldownSeconds > 0) WarningAmber else TextPrimary
                            )
                            Text(
                                text = if (jobs.isNotEmpty()) {
                                    if (isBengali) "$completedCount টি সফল, $failedCount টি ব্যর্থ (মোট $total টি)"
                                    else "$completedCount succeeded, $failedCount failed (Total $total)"
                                } else {
                                    if (isBengali) "নতুন ব্যাচ শুরু করতে জেনারেটরে যান" else "Start a new batch from Generator"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("batch_running_spinner"),
                                strokeWidth = 2.5.dp,
                                color = CyanAccent
                            )
                        } else if (cooldownSeconds > 0) {
                            Surface(
                                shape = CircleShape,
                                color = WarningAmber.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (jobs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (failedCount > 0 && completedCount == 0) AlertRed else EmeraldPrimary,
                            trackColor = CyberSurfaceVariant
                        )
                    }

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onCancelBatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cancel_batch_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "ব্যাচ বন্ধ করুন (Cancel)" else "Cancel Batch",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // If no jobs yet, show empty state
        if (jobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBengali) "কোনো সক্রিয় ব্যাচ রান হচ্ছে না" else "No batch running currently",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali)
                                "কুকি কনফিগার করে একাউন্ট তৈরি করতে নিচের বাটনে চাপ দিন"
                            else
                                "Configure your cookie and password in the Generator to begin",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToGenerator,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color(0xFF00381B)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "জেনারেটরে যান" else "Go to Generator",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Per Account Job Cards
            items(jobs, key = { it.index }) { job ->
                AccountJobCard(
                    job = job,
                    isBengali = isBengali,
                    onShowSnackbar = onShowSnackbar
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountJobCard(
    job: AccountJobState,
    isBengali: Boolean,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var logsExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("account_job_card_${job.index}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(
            1.dp,
            when {
                job.isSuccess -> SuccessGreen.copy(alpha = 0.7f)
                job.isFailed -> AlertRed.copy(alpha = 0.7f)
                job.isRunning -> CyanAccent.copy(alpha = 0.5f)
                else -> BorderColor
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Account Index & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            job.isSuccess -> SuccessGreen
                            job.isFailed -> AlertRed
                            job.isRunning -> CyanAccent
                            else -> TextMuted
                        },
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBengali) "অ্যাকাউন্ট #${job.index}" else "Account #${job.index}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (job.name.isNotEmpty()) {
                            Text(
                                text = "👤 ${job.name}" + if (job.dob.isNotEmpty()) " (${job.dob})" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        job.isSuccess -> SuccessGreen.copy(alpha = 0.2f)
                        job.isFailed -> AlertRed.copy(alpha = 0.2f)
                        job.isRunning -> CyanAccent.copy(alpha = 0.2f)
                        else -> CyberSurfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            job.isSuccess -> if (isBengali) "সফল (SUCCESS)" else "SUCCESS"
                            job.isFailed -> if (isBengali) "ব্যর্থ (FAILED)" else "FAILED"
                            job.isRunning -> if (isBengali) "চলমান..." else "RUNNING"
                            else -> "QUEUED"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            job.isSuccess -> SuccessGreen
                            job.isFailed -> AlertRed
                            job.isRunning -> CyanAccent
                            else -> TextMuted
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Indicator Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StepChip(
                    label = if (isBengali) "১. ইমেইল" else "1. Email",
                    isDone = job.email.isNotEmpty(),
                    isActive = job.step == CreationStep.MAIL
                )
                StepChip(
                    label = if (isBengali) "২. মেটা আইডি" else "2. Meta ID",
                    isDone = job.uid.isNotEmpty(),
                    isActive = job.step == CreationStep.META_ID
                )
                StepChip(
                    label = if (isBengali) "৩. ১ম ওটিপি" else "3. 1st OTP",
                    isDone = job.otp1.isNotEmpty() || job.step.ordinal >= CreationStep.CONFIRM_OTP_1.ordinal,
                    isActive = job.step == CreationStep.SEND_OTP_1 || job.step == CreationStep.WAIT_OTP_1 || job.step == CreationStep.CONFIRM_OTP_1
                )
                StepChip(
                    label = if (isBengali) "৪. ২য় ওটিপি" else "4. 2nd OTP",
                    isDone = job.otp2.isNotEmpty() || job.step.ordinal >= CreationStep.SET_PASSWORD.ordinal,
                    isActive = job.step == CreationStep.SEND_RESET_REQ || job.step == CreationStep.WAIT_OTP_2
                )
                StepChip(
                    label = if (isBengali) "৫. পাসওয়ার্ড সেট" else "5. Password Set",
                    isDone = job.isSuccess,
                    isActive = job.step == CreationStep.SET_PASSWORD
                )
            }

            // Profile Name Row
            if (job.name.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "প্রোফাইল নাম ও জন্মতারিখ:" else "Profile Name & DOB:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = job.name + if (job.dob.isNotEmpty()) " (${job.dob})" else "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(job.name))
                            onShowSnackbar(if (isBengali) "নাম কপি করা হয়েছে!" else "Name copied!")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_name_btn_${job.index}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Name",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Created Info Row (if email or uid available)
            if (job.email.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ইমেইল:" else "Email:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = job.email,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(job.email))
                            onShowSnackbar(if (isBengali) "ইমেইল কপি করা হয়েছে!" else "Email copied!")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_email_btn_${job.index}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Email",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (job.uid.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "মেটা UID:" else "Meta UID:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = job.uid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(job.uid))
                            onShowSnackbar(if (isBengali) "UID কপি করা হয়েছে!" else "UID copied!")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_uid_btn_${job.index}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UID",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (job.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "পাসওয়ার্ড:" else "Password:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = job.password,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(job.password))
                            onShowSnackbar(if (isBengali) "পাসওয়ার্ড কপি করা হয়েছে!" else "Password copied!")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_pwd_btn_${job.index}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Error display
            if (job.isFailed && !job.errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${job.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlertRed,
                    fontSize = 12.sp
                )
            }

            // Expandable Logs Console
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { logsExpanded = !logsExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "কনসোল লগ (${job.logs.size})" else "Console Logs (${job.logs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = if (logsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = logsExpanded) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberBg,
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (job.logs.isEmpty()) {
                            Text(
                                text = if (isBengali) "কোনো লগ নেই" else "No logs recorded yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        } else {
                            job.logs.forEach { entry ->
                                Text(
                                    text = entry.message,
                                    color = when (entry.type) {
                                        LogType.SUCCESS -> SuccessGreen
                                        LogType.ERROR -> AlertRed
                                        LogType.WARNING -> WarningAmber
                                        LogType.INFO -> TextPrimary
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepChip(label: String, isDone: Boolean, isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = when {
            isDone -> SuccessGreen.copy(alpha = 0.15f)
            isActive -> CyanAccent.copy(alpha = 0.2f)
            else -> CyberSurfaceVariant
        },
        border = BorderStroke(
            1.dp,
            when {
                isDone -> SuccessGreen.copy(alpha = 0.4f)
                isActive -> CyanAccent
                else -> Color.Transparent
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = when {
                    isDone -> SuccessGreen
                    isActive -> CyanAccent
                    else -> TextMuted
                },
                fontWeight = if (isActive || isDone) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
