package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.R
import kotlinx.coroutines.delay

private data class FabMenuItem(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
)

private data class FabMenuItemWithActive(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit,
        val isActive: Boolean = false,
        val activeLabel: String = "",
        val enabled: Boolean = true
) {
        fun toFabMenuItem() = FabMenuItem(icon, label, onClick, enabled)
}

/**
 * Separate backdrop composable for FabMenu. This allows parent to control z-ordering so NavBar can
 * appear above the backdrop.
 */
@Composable
fun FabMenuBackdrop(
        expanded: Boolean,
        menuTitle: String? = null,
        menuDescription: String? = null,
        onDismiss: () -> Unit
) {
        val backdropAlpha by
                animateFloatAsState(
                        targetValue = if (expanded) 0.4f else 0f,
                        animationSpec = tween(300),
                        label = "backdropAlpha"
                )

        if (expanded || backdropAlpha > 0f) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = backdropAlpha))
                                        .clickable(
                                                interactionSource =
                                                        remember { MutableInteractionSource() },
                                                indication = null
                                        ) { onDismiss() }
                ) {
                        // Menu title header on top left with fade animation
                        AnimatedContent(
                                targetState = menuTitle,
                                transitionSpec = {
                                        fadeIn(animationSpec = tween(200)) togetherWith
                                                fadeOut(animationSpec = tween(150))
                                },
                                label = "menuTitleTransition",
                                modifier =
                                        Modifier.statusBarsPadding()
                                                .padding(start = 24.dp, top = 16.dp)
                                                .graphicsLayer { alpha = backdropAlpha / 0.4f }
                        ) { title ->
                                if (title != null) {
                                        Column {
                                                Text(
                                                        text = title,
                                                        color = Color.White,
                                                        fontSize = 28.sp,
                                                        fontWeight = FontWeight.Black
                                                )
                                                if (menuDescription != null) {
                                                        Text(
                                                                text = menuDescription,
                                                                color = Color.White.copy(alpha = 0.7f),
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Normal,
                                                                modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun FabMenuOverlay(
        modifier: Modifier = Modifier,
        expanded: Boolean,
        canGoForward: Boolean,
        isLoading: Boolean = false,
        isAtTop: Boolean = false,
        onToggle: () -> Unit,
        onRefresh: () -> Unit,
        onStop: () -> Unit = {},
        onForward: () -> Unit,
        onPingoAI: () -> Unit = {},
        onBookmark: () -> Unit = {},
        onHistory: () -> Unit = {},
        onDownloads: () -> Unit = {},
        // Settings submenu callbacks
        onNavigateToSearch: () -> Unit = {},
        onNavigateToNotifications: () -> Unit = {},
        onNavigateToDownloadSettings: () -> Unit = {},
        onNavigateToHelp: () -> Unit = {},
        onNavigateToAbout: () -> Unit = {},
        // General submenu callbacks
        onSetDefaultBrowser: () -> Unit = {},
        onLanguageSelected: (String) -> Unit = {},
        // Appearance submenu callbacks
        onThemeSelected: (String) -> Unit = {},
        // Start Page submenu callbacks
        startupBehavior: String = "start_page",
        onStartupNewPage: () -> Unit = {},
        onStartupContinue: () -> Unit = {},
        onStartupStartPage: () -> Unit = {},
        // Privacy submenu callbacks
        onAdBlockerToggle: () -> Unit = {},
        adBlockerActive: Boolean = true,
        adBlockerStrength: String = "medium",
        onAdBlockerStatusChanged: (Boolean) -> Unit = {},
        onAdBlockerStrengthChanged: (String) -> Unit = {},
        onSafeBrowsingChanged: (Boolean) -> Unit = {},
        safeBrowsingActive: Boolean = true,
        onPreventPopupsToggle: () -> Unit = {},
        onSiteSettingsClicked: () -> Unit = {},
        onCookiesClicked: () -> Unit = {},
        onDnsClicked: () -> Unit = {},
        onClearOnExitToggle: () -> Unit = {},
        onClearDataClicked: () -> Unit = {},
        // Notifications submenu callbacks
        onPushNotificationClicked: () -> Unit = {},
        // Download Settings submenu callbacks
        onAskBeforeDownloadToggle: () -> Unit = {},
        onDownloadQueueClicked: () -> Unit = {},
        onDownloadFolderClicked: () -> Unit = {},
        // Help submenu callbacks
        onFaqClicked: () -> Unit = {},
        onReportBugClicked: () -> Unit = {},
        onFeedbackClicked: () -> Unit = {},
        // About submenu callbacks
        onVersionClicked: () -> Unit = {},
        onTosClicked: () -> Unit = {},
        onThirdPartyClicked: () -> Unit = {},
        // Menu change callback for header title
        // Menu change callback for header title and description
        onMenuChanged: (String?, String?) -> Unit = { _, _ -> }
) {
        // State for menu navigation - using int for state management
        var currentMenu by remember {
                mutableStateOf(0)
        } // 0=main, 1=settings, 2=general, 3=language, 4=appearance, 5=startpage, 6=privacy,
        // 7=search, 8=notifications, 9=downloads, 10=help, 11=about, 12=startup, 13=adblocker,
        // 14=adblocker_status, 15=adblocker_strength, 16=safe_browsing

        // Reset menu when fab is closed
        LaunchedEffect(expanded) {
                if (!expanded) {
                        currentMenu = 0
                        onMenuChanged(null, null)
                }
        }

        // Localized strings for settings menu
        val settingsLabel = stringResource(R.string.fab_settings)
        val generalLabel = stringResource(R.string.settings_general)
        val appearanceLabel = stringResource(R.string.settings_appearance)
        val startPageLabel = stringResource(R.string.settings_start_page)
        val privacyLabel = stringResource(R.string.settings_privacy)
        val searchLabel = stringResource(R.string.settings_search)
        val notificationsLabel = stringResource(R.string.settings_notifications)
        val downloadsSettingsLabel = stringResource(R.string.settings_downloads)
        val helpLabel = stringResource(R.string.settings_help)
        val aboutLabel = stringResource(R.string.settings_about)
        val languageLabel = stringResource(R.string.settings_language)
        // Startup label (needed before getMenuTitle)
        val startupLabel = stringResource(R.string.startup_on_startup)

        // Privacy menu labels (Moved up for scope)
        val adBlockerLabel = stringResource(R.string.privacy_ad_blocker)
        val safeBrowsingLabel = stringResource(R.string.privacy_safe_browsing)
        val preventPopupsLabel = stringResource(R.string.privacy_prevent_popups)
        val siteSettingsLabel = stringResource(R.string.privacy_site_settings)
        val cookiesLabel = stringResource(R.string.privacy_cookies)
        val dnsLabel = stringResource(R.string.privacy_dns)
        val clearOnExitLabel = stringResource(R.string.privacy_clear_on_exit)
        val clearNextLabel = stringResource(R.string.privacy_clear_on_exit) // Not used?
        val clearDataLabel = stringResource(R.string.privacy_clear_data)

        // Ad Blocker submenu labels
        val adBlockerStatusLabel = stringResource(R.string.adblock_status)
        val adBlockerStrengthLabel = stringResource(R.string.adblock_strength)
        val strengthLightLabel = stringResource(R.string.strength_light)
        val strengthMediumLabel = stringResource(R.string.strength_medium)
        val strengthStrongLabel = stringResource(R.string.strength_strong)
        val activeStatusLabel = stringResource(R.string.status_active)
        val inactiveStatusLabel = stringResource(R.string.status_inactive)

        // Safe Browsing submenu labels
        val safeBrowsingOnLabel = stringResource(R.string.safe_browsing_on)
        val safeBrowsingOffLabel = stringResource(R.string.safe_browsing_off)
        val safeBrowsingDescLabel = stringResource(R.string.safe_browsing_description)

        // Menu titles for header display (localized)
        fun getMenuTitleAndDescription(menu: Int): Pair<String?, String?> {
                return when (menu) {
                        0 -> null to null // Main menu - no header
                        1 -> settingsLabel to null
                        2 -> generalLabel to null
                        3 -> languageLabel to null
                        4 -> appearanceLabel to null
                        5 -> startPageLabel to null
                        6 -> privacyLabel to null
                        7 -> searchLabel to null
                        8 -> notificationsLabel to null
                        9 -> downloadsSettingsLabel to null
                        10 -> helpLabel to null
                        11 -> aboutLabel to null
                        12 -> startupLabel to null
                        13 -> adBlockerLabel to null
                        14 -> adBlockerStatusLabel to null
                        15 -> adBlockerStrengthLabel to null
                        16 -> safeBrowsingLabel to safeBrowsingDescLabel
                        else -> null to null
                }
        }

        // Notify parent when menu changes
        LaunchedEffect(currentMenu) {
                val (title, description) = getMenuTitleAndDescription(currentMenu)
                onMenuChanged(title, description)
        }

        // Additional localized strings
        val stopLabel = stringResource(R.string.fab_stop)
        val refreshLabel = stringResource(R.string.fab_refresh)
        val downloadsLabel = stringResource(R.string.fab_downloads)
        val historyLabel = stringResource(R.string.fab_history)
        val bookmarkLabel = stringResource(R.string.fab_bookmark)
        val forwardLabel = stringResource(R.string.fab_forward)
        val pingoAiLabel = stringResource(R.string.fab_pingo_ai)

        // Settings menu labels
        val backLabel = stringResource(R.string.fab_back)

        // General menu labels
        val defaultBrowserLabel = stringResource(R.string.settings_default_browser)
        val indonesianLabel = stringResource(R.string.language_indonesian)
        val englishLabel = stringResource(R.string.language_english)

        // Appearance menu labels
        val systemThemeLabel = stringResource(R.string.theme_system)
        val lightThemeLabel = stringResource(R.string.theme_light)
        val darkThemeLabel = stringResource(R.string.theme_dark)

        // Start Page menu labels
        val startupNewPageLabel = stringResource(R.string.startup_open_new_page)
        val startupContinueLabel = stringResource(R.string.startup_continue_where_left)
        val startupStartPageLabel = stringResource(R.string.startup_open_start_page)
        val activeLabel = stringResource(R.string.status_aktif)

        // Privacy menu labels

        // Search menu labels
        val searchEngineLabel = stringResource(R.string.search_engine)
        val pingoAiSearchLabel = stringResource(R.string.search_pingo_ai)

        // Notifications menu labels
        val pushNotificationLabel = stringResource(R.string.notifications_push)

        // Download Settings menu labels
        val askBeforeDownloadLabel = stringResource(R.string.downloads_ask_before)
        val downloadQueueLabel = stringResource(R.string.downloads_queue)
        val downloadFolderLabel = stringResource(R.string.downloads_folder)

        // Help menu labels
        val faqLabel = stringResource(R.string.help_faq)
        val reportBugLabel = stringResource(R.string.help_report_bug)
        val feedbackLabel = stringResource(R.string.help_feedback)

        // About menu labels
        val versionLabel = stringResource(R.string.about_version)
        val tosLabel = stringResource(R.string.about_tos)
        val thirdPartyLabel = stringResource(R.string.about_third_party)

        // Refresh/Stop item based on loading state
        val refreshOrStopItem =
                if (isLoading) {
                        FabMenuItem(
                                Icons.Rounded.Close,
                                stopLabel,
                                {
                                        onStop()
                                        onToggle()
                                }
                        )
                } else {
                        FabMenuItem(
                                Icons.Rounded.Refresh,
                                refreshLabel,
                                {
                                        onRefresh()
                                        onToggle()
                                }
                        )
                }

        // Main menu items (menu 0)
        val mainMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.Rounded.Settings,
                                settingsLabel,
                                { currentMenu = 1 } // Switch to settings submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Download,
                                downloadsLabel,
                                {
                                        onDownloads()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.History,
                                historyLabel,
                                {
                                        onHistory()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.BookmarkBorder,
                                bookmarkLabel,
                                {
                                        onBookmark()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                forwardLabel,
                                {
                                        if (canGoForward) {
                                                onForward()
                                                onToggle()
                                        }
                                },
                                canGoForward
                        ),
                        refreshOrStopItem,
                        FabMenuItem(
                                Icons.Rounded.AutoAwesome,
                                pingoAiLabel,
                                {
                                        onPingoAI()
                                        onToggle()
                                }
                        )
                )

        // Settings submenu items (menu 1)
        val settingsMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 0 } // Back to main menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Language,
                                generalLabel,
                                { currentMenu = 2 } // Go to general submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Palette,
                                appearanceLabel,
                                { currentMenu = 4 } // Go to appearance submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Home,
                                startPageLabel,
                                { currentMenu = 5 } // Go to start page submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.PrivacyTip,
                                privacyLabel,
                                { currentMenu = 6 } // Go to privacy submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Search,
                                searchLabel,
                                { currentMenu = 7 } // Go to search submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Notifications,
                                notificationsLabel,
                                { currentMenu = 8 } // Go to notifications submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Download,
                                downloadsSettingsLabel,
                                { currentMenu = 9 } // Go to download settings submenu
                        ),
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.HelpOutline,
                                helpLabel,
                                { currentMenu = 10 } // Go to help submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Info,
                                aboutLabel,
                                { currentMenu = 11 } // Go to about submenu
                        )
                )

        // General submenu items (menu 2)
        val generalMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Language,
                                languageLabel,
                                { currentMenu = 3 } // Go to language submenu
                        ),
                        FabMenuItem(
                                Icons.Rounded.OpenInBrowser,
                                defaultBrowserLabel,
                                {
                                        onSetDefaultBrowser()
                                        onToggle()
                                }
                        )
                )

        // Language submenu items (menu 3)
        val languageMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 2 } // Back to general menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Language,
                                indonesianLabel,
                                {
                                        onLanguageSelected("id")
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Language,
                                englishLabel,
                                {
                                        onLanguageSelected("en")
                                        onToggle()
                                }
                        )
                )

        // Appearance submenu items (menu 4)
        val appearanceMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Smartphone,
                                systemThemeLabel,
                                {
                                        onThemeSelected("system")
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.WbSunny,
                                lightThemeLabel,
                                {
                                        onThemeSelected("light")
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.DarkMode,
                                darkThemeLabel,
                                {
                                        onThemeSelected("dark")
                                        onToggle()
                                }
                        )
                )

        // Start Page submenu items (menu 5)
        val startPageMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.PlayArrow,
                                startupLabel,
                                { currentMenu = 12 } // Go to startup submenu
                        )
                )

        // Privacy submenu items (menu 6)
        val privacyMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.AdsClick,
                                adBlockerLabel,
                                {
                                        currentMenu = 13 // Go to Ad Blocker submenu
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Security,
                                safeBrowsingLabel,
                                {
                                        currentMenu = 16 // Go to Safe Browsing submenu
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Public,
                                preventPopupsLabel,
                                {
                                        onPreventPopupsToggle()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Settings,
                                siteSettingsLabel,
                                {
                                        onSiteSettingsClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Cookie,
                                cookiesLabel,
                                {
                                        onCookiesClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Dns,
                                dnsLabel,
                                {
                                        onDnsClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ExitToApp,
                                clearOnExitLabel,
                                {
                                        onClearOnExitToggle()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.DeleteForever,
                                clearDataLabel,
                                {
                                        onClearDataClicked()
                                        onToggle()
                                }
                        )
                )

        // Search submenu items (menu 7)
        val searchMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Search,
                                searchEngineLabel,
                                {
                                        onNavigateToSearch()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.AutoAwesome,
                                pingoAiSearchLabel,
                                {
                                        onPingoAI()
                                        onToggle()
                                }
                        )
                )

        // Notifications submenu items (menu 8)
        val notificationsMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Notifications,
                                pushNotificationLabel,
                                {
                                        onPushNotificationClicked()
                                        onToggle()
                                }
                        )
                )

        // Download Settings submenu items (menu 9)
        val downloadSettingsMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.HelpOutline,
                                askBeforeDownloadLabel,
                                {
                                        onAskBeforeDownloadToggle()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.Sort,
                                downloadQueueLabel,
                                {
                                        onDownloadQueueClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Folder,
                                downloadFolderLabel,
                                {
                                        onDownloadFolderClicked()
                                        onToggle()
                                }
                        )
                )

        // Help submenu items (menu 10)
        val helpMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.QuestionAnswer,
                                faqLabel,
                                {
                                        onFaqClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.BugReport,
                                reportBugLabel,
                                {
                                        onReportBugClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Feedback,
                                feedbackLabel,
                                {
                                        onFeedbackClicked()
                                        onToggle()
                                }
                        )
                )

        // About submenu items (menu 11)
        val aboutMenuItems =
                listOf(
                        FabMenuItem(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 1 } // Back to settings menu
                        ),
                        FabMenuItem(
                                Icons.Rounded.Info,
                                versionLabel,
                                {
                                        onVersionClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Description,
                                tosLabel,
                                {
                                        onTosClicked()
                                        onToggle()
                                }
                        ),
                        FabMenuItem(
                                Icons.Rounded.Layers,
                                thirdPartyLabel,
                                {
                                        onThirdPartyClicked()
                                        onToggle()
                                }
                        )
                )

        // Startup submenu items (menu 12)
        val startupMenuItems =
                listOf(
                        FabMenuItemWithActive(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 5 }, // Back to start page menu
                                isActive = false,
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Add,
                                startupNewPageLabel,
                                {
                                        onStartupNewPage()
                                        onToggle()
                                },
                                isActive = startupBehavior == "new_page",
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.History,
                                startupContinueLabel,
                                {
                                        onStartupContinue()
                                        onToggle()
                                },
                                isActive = startupBehavior == "continue",
                                activeLabel = activeLabel
                        )
                )

        // Ad Blocker submenu items (menu 13)
        val adBlockerMenuItems =
                listOf(
                        FabMenuItemWithActive(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 6 }, // Back to privacy menu
                                isActive = false
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.AdsClick,
                                adBlockerStatusLabel,
                                { currentMenu = 14 },
                                isActive = false
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Security,
                                adBlockerStrengthLabel,
                                { currentMenu = 15 },
                                isActive = false
                        )
                )

        // Ad Blocker Status submenu items (menu 14)
        val adBlockerStatusMenuItems =
                listOf(
                        FabMenuItemWithActive(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 13 }, // Back to ad blocker menu
                                isActive = false
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.CheckCircle,
                                activeStatusLabel,
                                {
                                        onAdBlockerStatusChanged(true)
                                        // Update toggle fallback if needed or assume parent handles
                                        // it
                                        onToggle()
                                },
                                isActive = adBlockerActive,
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Cancel,
                                inactiveStatusLabel,
                                {
                                        onAdBlockerStatusChanged(false)
                                        onToggle()
                                },
                                isActive = !adBlockerActive,
                                activeLabel = activeLabel
                        )
                )

        // Ad Blocker Strength submenu items (menu 15)
        val adBlockerStrengthMenuItems =
                listOf(
                        FabMenuItemWithActive(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 13 }, // Back to ad blocker menu
                                isActive = false
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Shield, // Use Shield or Security
                                strengthLightLabel,
                                {
                                        onAdBlockerStrengthChanged("ringan")
                                        onToggle()
                                },
                                isActive = adBlockerStrength == "ringan",
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Shield,
                                strengthMediumLabel,
                                {
                                        onAdBlockerStrengthChanged("sedang")
                                        onToggle()
                                },
                                isActive = adBlockerStrength == "sedang",
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Shield,
                                strengthStrongLabel,
                                {
                                        onAdBlockerStrengthChanged("kuat")
                                        onToggle()
                                },
                                isActive = adBlockerStrength == "kuat",
                                activeLabel = activeLabel
                        )
                )

        // Safe Browsing submenu items (menu 16)
        val safeBrowsingMenuItems =
                listOf(
                        FabMenuItemWithActive(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                backLabel,
                                { currentMenu = 6 }, // Back to privacy menu
                                isActive = false
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.CheckCircle,
                                safeBrowsingOnLabel,
                                {
                                        onSafeBrowsingChanged(true)
                                        onToggle()
                                },
                                isActive = safeBrowsingActive,
                                activeLabel = activeLabel
                        ),
                        FabMenuItemWithActive(
                                Icons.Rounded.Cancel,
                                safeBrowsingOffLabel,
                                {
                                        onSafeBrowsingChanged(false)
                                        onToggle()
                                },
                                isActive = !safeBrowsingActive,
                                activeLabel = activeLabel
                        )
                )

        // Get current menu items based on state
        fun getMenuItems(menu: Int): List<FabMenuItem> {
                return when (menu) {
                        0 -> mainMenuItems
                        1 -> settingsMenuItems
                        2 -> generalMenuItems
                        3 -> languageMenuItems
                        4 -> appearanceMenuItems
                        5 -> startPageMenuItems
                        6 -> privacyMenuItems
                        7 -> searchMenuItems
                        8 -> notificationsMenuItems
                        9 -> downloadSettingsMenuItems
                        10 -> helpMenuItems
                        11 -> aboutMenuItems
                        12 -> startupMenuItems.map { it.toFabMenuItem() }
                        13 -> adBlockerMenuItems.map { it.toFabMenuItem() }
                        14 -> adBlockerStatusMenuItems.map { it.toFabMenuItem() }
                        15 -> adBlockerStrengthMenuItems.map { it.toFabMenuItem() }
                        16 -> safeBrowsingMenuItems.map { it.toFabMenuItem() }
                        else -> mainMenuItems
                }
        }

        val currentMenuItems = getMenuItems(currentMenu)
        val menuItems = if (isAtTop) currentMenuItems.reversed() else currentMenuItems

        var visibleItems by remember { mutableStateOf(setOf<Int>()) }
        var isTransitioning by remember { mutableStateOf(false) }
        var displayedMenuState by remember { mutableIntStateOf(0) }

        // Handle menu transition with crossfade
        LaunchedEffect(currentMenu) {
                if (expanded && displayedMenuState != currentMenu) {
                        // Start transition: fade out current menu
                        isTransitioning = true
                        visibleItems = emptySet()
                        delay(200) // Wait for fade out animation

                        // Switch to new menu
                        displayedMenuState = currentMenu

                        // Animate in new menu items with stagger
                        val newMenuItems = getMenuItems(currentMenu)
                        val orderedItems = if (isAtTop) newMenuItems.reversed() else newMenuItems
                        val indices =
                                if (isAtTop) orderedItems.indices
                                else orderedItems.indices.reversed()
                        for (i in indices) {
                                delay(50L)
                                visibleItems = visibleItems + i
                        }
                        isTransitioning = false
                } else if (!expanded) {
                        displayedMenuState = currentMenu
                }
        }

        // Handle initial expand and collapse
        LaunchedEffect(expanded, isAtTop) {
                if (expanded && !isTransitioning) {
                        visibleItems = emptySet()
                        displayedMenuState = currentMenu
                        val indices =
                                if (isAtTop) menuItems.indices else menuItems.indices.reversed()
                        for (i in indices) {
                                delay(50L)
                                visibleItems = visibleItems + i
                        }
                } else if (!expanded) {
                        visibleItems = emptySet()
                }
        }

        // Use displayedMenuState to determine which items to show
        val displayedMenuItems = getMenuItems(displayedMenuState)
        val finalMenuItems = if (isAtTop) displayedMenuItems.reversed() else displayedMenuItems

        // For menu 12, 13, 14, 15 also get the active state items
        val displayedActiveItems =
                when (displayedMenuState) {
                        12 -> if (isAtTop) startupMenuItems.reversed() else startupMenuItems
                        13 -> if (isAtTop) adBlockerMenuItems.reversed() else adBlockerMenuItems
                        14 ->
                                if (isAtTop) adBlockerStatusMenuItems.reversed()
                                else adBlockerStatusMenuItems
                        15 ->
                                if (isAtTop) adBlockerStrengthMenuItems.reversed()
                                else adBlockerStrengthMenuItems
                        else -> emptyList()
                }

        Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = if (isAtTop) Alignment.TopEnd else Alignment.BottomEnd
        ) {
                Column(
                        modifier =
                                Modifier.padding(
                                        end = 24.dp,
                                        top = if (isAtTop) 130.dp else 0.dp,
                                        bottom = if (isAtTop) 0.dp else 100.dp
                                ),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        if (displayedActiveItems.isNotEmpty()) {
                                // Render active menus (12, 13, 14, 15)
                                displayedActiveItems.forEachIndexed { index, item ->
                                        AnimatedVisibility(
                                                visible = expanded && index in visibleItems,
                                                enter =
                                                        fadeIn(animationSpec = tween(200)) +
                                                                slideInVertically(
                                                                        initialOffsetY = {
                                                                                if (isAtTop) -it / 2
                                                                                else it / 2
                                                                        },
                                                                        animationSpec =
                                                                                spring(
                                                                                        dampingRatio =
                                                                                                Spring.DampingRatioMediumBouncy,
                                                                                        stiffness =
                                                                                                Spring.StiffnessMedium
                                                                                )
                                                                ),
                                                exit =
                                                        fadeOut(animationSpec = tween(150)) +
                                                                slideOutVertically(
                                                                        targetOffsetY = {
                                                                                if (isAtTop) -it / 2
                                                                                else it / 2
                                                                        },
                                                                        animationSpec = tween(150)
                                                                )
                                        ) {
                                                MenuPillWithActive(
                                                        icon = item.icon,
                                                        label = item.label,
                                                        onClick = item.onClick,
                                                        enabled = item.enabled,
                                                        isActive = item.isActive,
                                                        activeLabel = item.activeLabel
                                                )
                                        }
                                }
                        } else {
                                // Regular menu rendering
                                finalMenuItems.forEachIndexed { index, item ->
                                        AnimatedVisibility(
                                                visible = expanded && index in visibleItems,
                                                enter =
                                                        fadeIn(animationSpec = tween(200)) +
                                                                slideInVertically(
                                                                        initialOffsetY = {
                                                                                if (isAtTop) -it / 2
                                                                                else it / 2
                                                                        },
                                                                        animationSpec =
                                                                                spring(
                                                                                        dampingRatio =
                                                                                                Spring.DampingRatioMediumBouncy,
                                                                                        stiffness =
                                                                                                Spring.StiffnessMedium
                                                                                )
                                                                ),
                                                exit =
                                                        fadeOut(animationSpec = tween(150)) +
                                                                slideOutVertically(
                                                                        targetOffsetY = {
                                                                                if (isAtTop) -it / 2
                                                                                else it / 2
                                                                        },
                                                                        animationSpec = tween(150)
                                                                )
                                        ) {
                                                MenuPill(
                                                        icon = item.icon,
                                                        label = item.label,
                                                        onClick = item.onClick,
                                                        enabled = item.enabled
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun MenuPill(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean = true
) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by
                animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "pillScale"
                )

        Surface(
                modifier =
                        Modifier.scale(scale)
                                .shadow(3.dp, RoundedCornerShape(25.dp))
                                .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        enabled = enabled,
                                        onClick = onClick
                                ),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint =
                                        if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.4f
                                                ),
                                modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                                text = label,
                                fontSize = 14.sp,
                                color =
                                        if (enabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                }
        }
}

@Composable
private fun MenuPillWithActive(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean = true,
        isActive: Boolean = false,
        activeLabel: String = ""
) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by
                animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "pillScale"
                )

        Surface(
                modifier =
                        Modifier.scale(scale)
                                .shadow(3.dp, RoundedCornerShape(25.dp))
                                .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        enabled = enabled,
                                        onClick = onClick
                                ),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.surface,
                border =
                        BorderStroke(
                                width = if (isActive) 2.dp else 1.dp,
                                color =
                                        if (isActive) com.fadhilmanfa.pingo.ui.theme.Secondary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
        ) {
                Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint =
                                        if (isActive) com.fadhilmanfa.pingo.ui.theme.Secondary
                                        else if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.4f
                                                ),
                                modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                                Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight =
                                                if (isActive) FontWeight.SemiBold
                                                else FontWeight.Normal,
                                        color =
                                                if (isActive)
                                                        com.fadhilmanfa.pingo.ui.theme.Secondary
                                                else if (enabled)
                                                        MaterialTheme.colorScheme.onSurface
                                                else
                                                        MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.4f
                                                        )
                                )
                                if (isActive) {
                                        Text(
                                                text = activeLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = com.fadhilmanfa.pingo.ui.theme.Secondary
                                        )
                                }
                        }
                }
        }
}
