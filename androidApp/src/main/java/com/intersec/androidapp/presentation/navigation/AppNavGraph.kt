package com.intersec.androidapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.intersec.androidapp.presentation.screens.capture.CaptureRealtimeScreen
import com.intersec.androidapp.presentation.screens.capture.MissionControlScreen
import com.intersec.androidapp.presentation.screens.capture.ImportLogScreen
import com.intersec.androidapp.presentation.screens.diagnostic.DiagnosticScreen
import com.intersec.androidapp.presentation.screens.flow.FlowDetailScreen
import com.intersec.androidapp.presentation.screens.flow.FlowScreen
import com.intersec.androidapp.presentation.screens.history.HistoryScreen
import com.intersec.androidapp.presentation.screens.overview.CaptureOverviewScreen
import com.intersec.androidapp.presentation.screens.overview.GeographicMapScreen
import com.intersec.androidapp.presentation.screens.packet.PacketDetailScreen
import com.intersec.androidapp.presentation.screens.packet.PacketScreen
import com.intersec.androidapp.presentation.screens.security.SecurityReportScreen
import com.intersec.androidapp.presentation.screens.session.SessionScreen
import com.intersec.androidapp.presentation.screens.settings.SettingsScreen
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    analysisViewModel: AnalysisViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.CAPTURE
    ) {
        composable(AppRoutes.CAPTURE) {
            MissionControlScreen(
                viewModel = analysisViewModel,
                onOpenPackets = { navController.navigate(AppRoutes.PACKETS) },
                onOpenFlows = { navController.navigate(AppRoutes.FLOWS) },
                onOpenOverview = { navController.navigate(AppRoutes.OVERVIEW) },
                onOpenSecurity = { navController.navigate(AppRoutes.SECURITY_REPORT) },
                onOpenImportLog = { navController.navigate(AppRoutes.IMPORT_LOG) },
                onOpenCaptureRealtime = { navController.navigate(AppRoutes.CAPTURE_REALTIME) }
            )
        }

        composable(AppRoutes.CAPTURE_REALTIME) {
            CaptureRealtimeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.OVERVIEW) {
            CaptureOverviewScreen(
                onBack = { navController.popBackStack() },
                onOpenPackets = { navController.navigate(AppRoutes.PACKETS) },
                onOpenFlows = { navController.navigate(AppRoutes.FLOWS) },
                onOpenSecurity = { navController.navigate(AppRoutes.SECURITY_REPORT) },
                onOpenGeoMap = { navController.navigate(AppRoutes.GEO_MAP) }
            )
        }

        composable(AppRoutes.GEO_MAP) {
            GeographicMapScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.DIAGNOSTIC) {
            DiagnosticScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PACKETS) {
            PacketScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { 
                    navController.navigate(AppRoutes.PACKET_DETAIL) 
                }
            )
        }

        composable(AppRoutes.PACKET_DETAIL) {
            PacketDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.FLOWS) {
            FlowScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { 
                    navController.navigate(AppRoutes.FLOW_DETAIL) 
                }
            )
        }

        composable(AppRoutes.FLOW_DETAIL) {
            FlowDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.SESSIONS) {
            SessionScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.SECURITY_REPORT) {
            SecurityReportScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.IMPORT_LOG) {
            ImportLogScreen(
                viewModel = analysisViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
