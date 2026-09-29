package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTopBar
import com.example.ui.AutoTradeScreen
import com.example.ui.HomeScreen
import com.example.ui.NewsScreen
import com.example.ui.PortfolioScreen
import com.example.ui.SignalsScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GlobalTraderApp()
            }
        }
    }
}

enum class NavDestination(val label: String) {
    MARKETS("Markets"),
    SIGNALS("Signals"),
    PORTFOLIO("Portfolio"),
    AUTO_TRADE("Auto Trade"),
    NEWS("News")
}

@Composable
fun GlobalTraderApp(
    viewModel: TradingViewModel = viewModel()
) {
    var currentNavIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    // Handle user message snackbars
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissMessage()
        }
    }

    // Android back navigation: if not on Markets tab, go back to Markets
    BackHandler(enabled = currentNavIndex != 0) {
        currentNavIndex = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            AppTopBar()
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, CardBorder)),
                containerColor = SurfaceDark,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentNavIndex == 0,
                    onClick = { currentNavIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Markets",
                            modifier = Modifier.testTag("nav_markets")
                        )
                    },
                    label = {
                        Text(
                            text = "Markets",
                            fontWeight = if (currentNavIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BackgroundDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentNavIndex == 1,
                    onClick = { currentNavIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Signals",
                            modifier = Modifier.testTag("nav_signals")
                        )
                    },
                    label = {
                        Text(
                            text = "Signals",
                            fontWeight = if (currentNavIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BackgroundDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentNavIndex == 2,
                    onClick = { currentNavIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Portfolio",
                            modifier = Modifier.testTag("nav_portfolio")
                        )
                    },
                    label = {
                        Text(
                            text = "Portfolio",
                            fontWeight = if (currentNavIndex == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BackgroundDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentNavIndex == 3,
                    onClick = { currentNavIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Auto Trade",
                            modifier = Modifier.testTag("nav_auto_trade")
                        )
                    },
                    label = {
                        Text(
                            text = "Auto",
                            fontWeight = if (currentNavIndex == 3) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BackgroundDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentNavIndex == 4,
                    onClick = { currentNavIndex = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "News",
                            modifier = Modifier.testTag("nav_news")
                        )
                    },
                    label = {
                        Text(
                            text = "News",
                            fontWeight = if (currentNavIndex == 4) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BackgroundDark,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 60.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavIndex) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSignals = { currentNavIndex = 1 },
                    onNavigateToAutoTrade = { currentNavIndex = 3 }
                )
                1 -> SignalsScreen(viewModel = viewModel)
                2 -> PortfolioScreen(viewModel = viewModel)
                3 -> AutoTradeScreen(viewModel = viewModel)
                4 -> NewsScreen(viewModel = viewModel)
            }
        }
    }
}
