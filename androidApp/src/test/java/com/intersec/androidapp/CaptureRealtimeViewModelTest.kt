package com.intersec.androidapp

import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel
import com.intersec.androidapp.presentation.state.StatusIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureRealtimeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CoreAnalysisRepository
    private lateinit var viewModel: CaptureRealtimeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        
        runBlocking {
            whenever(repository.snapshotActive()).thenReturn(Result.failure(Exception("None")))
        }
        
        viewModel = CaptureRealtimeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUpdateFilterInput_UpdatesState() {
        val newFilter = "tcp port 80"
        viewModel.updateFilterInput(newFilter)
        
        assertEquals(newFilter, viewModel.uiState.value.filterInput)
    }

    @Test
    fun testPauseCapture_UpdatesStateToPaused() {
        viewModel.pauseCapture()
        
        assertTrue(viewModel.uiState.value.isPaused)
        assertEquals(StatusIndicator.PAUSED, viewModel.uiState.value.statusIndicator)
        assertTrue(viewModel.uiState.value.showSummaryModal)
    }

    @Test
    fun testResumeCapture_UpdatesStateToActive() {
        viewModel.pauseCapture()
        viewModel.resumeCapture()
        
        assertTrue(!viewModel.uiState.value.isPaused)
        assertEquals(StatusIndicator.ACTIVE, viewModel.uiState.value.statusIndicator)
        assertTrue(!viewModel.uiState.value.showSummaryModal)
    }
}
