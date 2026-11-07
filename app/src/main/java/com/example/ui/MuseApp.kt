package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AccountsHistoryView
import com.example.ui.components.ConfigCard
import com.example.ui.components.CookieCard
import com.example.ui.components.LiveBatchDashboard
import com.example.ui.components.SettingsView
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuseApp(
    viewModel: MuseViewModel,
    modifier: Modifier = Modifier
) {
    val cookieList by viewModel.cookieList.collectAsStateWithLifecycle()
    val password by viewModel.passwordInput.collectAsStateWithLifecycle()
    val batchCount by viewModel.batchCount.collectAsStateWithLifecycle()
    val mailApiToken by viewModel.mailApiToken.collectAsStateWithLifecycle()
    val proxyList by viewModel.proxyList.collectAsStateWithLifecycle()
    val proxyTestResults by viewModel.proxyTestResults.collectAsStateWithLifecycle()
    val testingProxyIndex by viewModel.testingProxyIndex.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isBengali = selectedLanguage == "bn"

    val isRunning by viewModel.isBatchRunning.collectAsStateWithLifecycle()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsStateWithLifecycle()
    val activeJobs by viewModel.activeJobs.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    val cookieValidation = remember(cookieList, selectedLanguage) {
        viewModel.validateOverallCookies()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CyberSurfaceVariant,
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_muse_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HRS TOOL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isBengali) "মেটা ক্রিয়েটর (v3.2)" else "META CREATOR (v3.2)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberSurfaceVariant,
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.setLanguage(if (isBengali) "en" else "bn")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("language_toggle_button")
                        ) {
                            Text(
                                text = if (isBengali) "বাং" else "EN",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // Generator
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Generator")
                    },
                    label = {
                        Text(text = if (isBengali) "জেনারেটর" else "Generator")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = CyberSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_generator")
                )

                // Live Batch
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CyanAccent
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = "Live Batch")
                        }
                    },
                    label = {
                        Text(text = if (isBengali) "লাইভ ব্যাচ" else "Live Batch")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = CyberSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_live_batch")
                )

                // Accounts History
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Accounts")
                    },
                    label = {
                        Text(text = if (isBengali) "অ্যাকাউন্টস" else "Accounts")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = CyberSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_accounts")
                )

                // Settings (Replaced Guide as requested)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = {
                        Text(text = if (isBengali) "সেটিংস" else "Settings")
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = CyberSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (selectedTab) {
                    0 -> GeneratorScreen(
                        cookieList = cookieList,
                        cookieValidation = cookieValidation,
                        password = password,
                        batchCount = batchCount,
                        isAccessKeySet = mailApiToken.isNotBlank(),
                        proxyCount = proxyList.size,
                        isRunning = isRunning,
                        cooldownSeconds = cooldownSeconds,
                        isBengali = isBengali,
                        onUpdateCookieItem = viewModel::updateCookieItem,
                        onAddCookieItem = viewModel::addCookieItem,
                        onRemoveCookieItem = viewModel::removeCookieItem,
                        onSaveCookies = viewModel::saveCookies,
                        onPasswordChange = viewModel::updatePassword,
                        onGenerateRandomPassword = viewModel::generateRandomPassword,
                        onBatchCountChange = viewModel::setBatchCount,
                        onNavigateToSettings = { viewModel.setTab(3) },
                        onStartBatch = viewModel::startBatch
                    )

                    1 -> LiveBatchDashboard(
                        jobs = activeJobs,
                        isRunning = isRunning,
                        cooldownSeconds = cooldownSeconds,
                        isBengali = isBengali,
                        onCancelBatch = viewModel::cancelBatch,
                        onNavigateToGenerator = { viewModel.setTab(0) },
                        onShowSnackbar = { msg ->
                            kotlinx.coroutines.runBlocking {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )

                    2 -> AccountsHistoryView(
                        accounts = allAccounts,
                        isBengali = isBengali,
                        onDeleteAccount = viewModel::deleteAccount,
                        onClearAll = viewModel::clearAllAccounts,
                        onExportAll = viewModel::getExportText,
                        onShowSnackbar = { msg ->
                            kotlinx.coroutines.runBlocking {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )

                    3 -> SettingsView(
                        savedAccessKey = mailApiToken,
                        proxyList = proxyList,
                        proxyTestResults = proxyTestResults,
                        testingProxyIndex = testingProxyIndex,
                        isBengali = isBengali,
                        onSaveAccessKey = viewModel::saveAccessKey,
                        onAddProxy = viewModel::addProxy,
                        onRemoveProxy = viewModel::removeProxy,
                        onClearAllProxies = viewModel::clearAllProxies,
                        onTestProxyAt = viewModel::testProxyAt
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratorScreen(
    cookieList: List<String>,
    cookieValidation: com.example.data.model.CookieValidation,
    password: String,
    batchCount: Int,
    isAccessKeySet: Boolean,
    proxyCount: Int,
    isRunning: Boolean,
    cooldownSeconds: Int,
    isBengali: Boolean,
    onUpdateCookieItem: (index: Int, String) -> Unit,
    onAddCookieItem: () -> Unit,
    onRemoveCookieItem: (index: Int) -> Unit,
    onSaveCookies: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onGenerateRandomPassword: () -> Unit,
    onBatchCountChange: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onStartBatch: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("generator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Access Key Warning if not set
        if (!isAccessKeySet) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("access_key_warning_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali)
                                "Access Key প্রয়োজন! অনুগ্রহ করে সেটিংস পেজে গিয়ে সেভ করুন।"
                            else
                                "Access Key required! Please configure in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlertRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(onClick = onNavigateToSettings) {
                        Text(
                            text = if (isBengali) "সেটিংস যান →" else "Settings →",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Multi-Cookie Card with (+) Add button
        CookieCard(
            cookieList = cookieList,
            onUpdateCookieItem = onUpdateCookieItem,
            onAddCookieItem = onAddCookieItem,
            onRemoveCookieItem = onRemoveCookieItem,
            onSaveCookies = onSaveCookies,
            isBengali = isBengali
        )

        // Config Card (Password, Batch Size, Status shortcut)
        ConfigCard(
            password = password,
            batchCount = batchCount,
            isAccessKeySet = isAccessKeySet,
            proxyCount = proxyCount,
            isBengali = isBengali,
            onPasswordChange = onPasswordChange,
            onGenerateRandomPassword = onGenerateRandomPassword,
            onBatchCountChange = onBatchCountChange,
            onNavigateToSettings = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Big Glowing Start Action Button
        val isCooldown = cooldownSeconds > 0
        val isButtonEnabled = cookieValidation.isValid && isAccessKeySet && !isRunning && !isCooldown

        Button(
            onClick = onStartBatch,
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_meta_create_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color(0xFF00381B),
                disabledContainerColor = if (isCooldown) Color(0xFF334155) else CyberSurfaceVariant,
                disabledContentColor = if (isCooldown) WarningAmber else TextMuted
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF00381B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "চলছে... (RUNNING)" else "RUNNING...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isCooldown) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "অপেক্ষা করুন (${cooldownSeconds}s Cooldown)" else "WAIT ${cooldownSeconds}s (Cooldown)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!isAccessKeySet) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "সেটিংস থেকে Access Key দিন" else "Set Access Key in Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!cookieValidation.isValid) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "সক্রিয় কুকি প্রদান করুন" else "Provide Valid Cookie",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali)
                            "মেটা তৈরি শুরু করুন ($batchCount টি)"
                        else
                            "START META CREATE ($batchCount)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
