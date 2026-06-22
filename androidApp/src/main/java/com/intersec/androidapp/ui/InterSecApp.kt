package com.intersec.androidapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.intersec.androidapp.presentation.navigation.AppNavGraph
import com.intersec.androidapp.presentation.navigation.AppRoutes
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
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "InterSec Core",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider()
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.name) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("InterSec Analyzer") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { analysisViewModel.refreshActiveSession() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                AppNavGraph(navController, analysisViewModel)
            }
        }
    }
}

data class DrawerItem(val name: String, val route: String, val icon: ImageVector)
