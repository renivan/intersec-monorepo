package com.intersec.androidapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.intersec.androidapp.R
import com.intersec.androidapp.presentation.navigation.AppNavGraph
import com.intersec.androidapp.presentation.navigation.AppRoutes
import com.intersec.androidapp.presentation.state.PacketColorPalette
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterSecApp() {
    val navController = rememberNavController()
    val analysisViewModel: AnalysisViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val menuItems = listOf(
        DrawerItem("Dashboard", AppRoutes.CAPTURE, Icons.Default.Dashboard),
        DrawerItem("Pacotes", AppRoutes.PACKETS, Icons.Default.List),
        DrawerItem("Fluxos", AppRoutes.FLOWS, Icons.Default.SwapHoriz),
        DrawerItem("Segurança", AppRoutes.SECURITY_REPORT, Icons.Default.Security),
        DrawerItem("Sessões", AppRoutes.SESSIONS, Icons.Default.Storage),
        DrawerItem("Diagnóstico", AppRoutes.DIAGNOSTIC, Icons.Default.Build)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PacketColorPalette.BACKGROUND_DARK,
                drawerContentColor = Color.White
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_neural_core),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "InterSec Core",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null, tint = if (currentRoute == item.route) Color.Cyan else Color.White) },
                        label = { Text(item.name, color = Color.White) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Cyan.copy(alpha = 0.1f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = PacketColorPalette.BACKGROUND_DARK,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("InterSec Analyzer", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PacketColorPalette.BACKGROUND_DARK
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { analysisViewModel.refreshActiveSession() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier.padding(padding),
                color = PacketColorPalette.BACKGROUND_DARK
            ) {
                AppNavGraph(navController, analysisViewModel)
            }
        }
    }
}

data class DrawerItem(val name: String, val route: String, val icon: ImageVector)
