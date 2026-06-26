package com.intersec.androidapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterSecApp(analysisViewModel: AnalysisViewModel = viewModel()) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val menuItems = listOf(
        DrawerItem("Dashboard", AppRoutes.CAPTURE, Icons.Default.Dashboard),
        DrawerItem("Pacotes", AppRoutes.PACKETS, Icons.AutoMirrored.Filled.List),
        DrawerItem("Fluxos", AppRoutes.FLOWS, Icons.Default.SwapHoriz),
        DrawerItem("Segurança", AppRoutes.SECURITY_REPORT, Icons.Default.Security),
        DrawerItem("Sessões", AppRoutes.SESSIONS, Icons.Default.Storage),
        DrawerItem("Temas Visuais", AppRoutes.SETTINGS, Icons.Default.Palette),
        DrawerItem("Configurações", AppRoutes.SETTINGS, Icons.Default.Settings),
        DrawerItem("Diagnóstico", AppRoutes.DIAGNOSTIC, Icons.Default.Build)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = Color.White,
            ) {
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_neural_core),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "INTERSEC",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null, tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(item.name.uppercase(), color = if (currentRoute == item.route) Color.White else Color.Gray, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("INTERSEC ANALYZER", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { analysisViewModel.refreshActiveSession() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier.padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavGraph(navController, analysisViewModel)
            }
        }
    }
}

data class DrawerItem(val name: String, val route: String, val icon: ImageVector)
