package com.intersec.androidapp.presentation.viewmodel

import com.intersec.androidapp.core.storage.SecuritySettingsManager
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.core.location.LocationTracker
import com.intersec.androidapp.core.auth.TierManager
import com.intersec.androidapp.core.neural.NeuralCoreEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    private val repository: CoreAnalysisRepository = mock()
    private val securitySettings: SecuritySettingsManager = mock()
    private val locationTracker: LocationTracker = mock()
    private val tierManager: TierManager = mock()
    private val neuralEngine: NeuralCoreEngine = mock()
    private lateinit var viewModel: AnalysisViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        com.intersec.androidapp.core.network.ThreatIntelManager.isTestEnvironment = true
        
        whenever(securitySettings.smartShieldActive).thenReturn(flowOf(true))
        whenever(securitySettings.killSwitchActive).thenReturn(flowOf(false))
        whenever(securitySettings.securityLevel).thenReturn(flowOf(1))
        whenever(securitySettings.userTier).thenReturn(flowOf(0))
        whenever(securitySettings.themeType).thenReturn(flowOf(0))
        whenever(securitySettings.isDarkMode).thenReturn(flowOf(true))
        whenever(securitySettings.firewallRules).thenReturn(flowOf(emptySet()))
        
        val locationFlow = MutableStateFlow<android.location.Location?>(null)
        whenever(locationTracker.currentLocation).thenReturn(locationFlow)
        
        val neuralFlow = MutableStateFlow<List<com.intersec.androidapp.presentation.state.NeuralLink3D>>(emptyList())
        whenever(neuralEngine.neuralStream).thenReturn(neuralFlow)

        viewModel = AnalysisViewModel(repository, securitySettings, locationTracker, tierManager, neuralEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `upgradeToPro should call tierManager performUpgrade`() = runTest {
        viewModel.upgradeToPro()
        verify(tierManager).performUpgrade()
    }

    @Test
    fun `persistence check - dark mode toggle`() = runTest {
        viewModel.toggleDarkMode(false)
        verify(securitySettings).setDarkMode(false)
    }
}
