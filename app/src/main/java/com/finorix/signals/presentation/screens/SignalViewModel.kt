package com.finorix.signals.presentation.screens

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.finorix.signals.domain.engine.SignalEngine
import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.model.Signal
import com.finorix.signals.domain.repository.CandleRepository
import com.finorix.signals.service.SignalWatcherService
import com.finorix.signals.service.SignalWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SignalViewModel @Inject constructor(
    private val repository: CandleRepository,
    private val signalEngine: SignalEngine,
    private val aiRepository: com.finorix.signals.domain.repository.AiRepository,
    private val signalDao: com.finorix.signals.data.local.dao.SignalDao,
    private val gson: com.google.gson.Gson,
    private val prefs: com.finorix.signals.domain.repository.UserPreferencesRepository
) : ViewModel() {

    val soundEnabled: StateFlow<Boolean> = prefs.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val vibrationEnabled: StateFlow<Boolean> = prefs.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _selectedPair = MutableStateFlow("EUR/USD")
    val selectedPair: StateFlow<String> = _selectedPair

    private val _selectedTimeframe = MutableStateFlow("1m")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe

    init {
        viewModelScope.launch {
            _selectedTimeframe.value = prefs.defaultTimeframe.first()
        }
    }

    private val _uiState = MutableStateFlow<SignalUiState>(SignalUiState.Loading)
    val uiState: StateFlow<SignalUiState> = _uiState

    private val _countdown = MutableStateFlow(60)
    val countdown: StateFlow<Int> = _countdown

    private val _aiExplanation = MutableStateFlow("")
    val aiExplanation: StateFlow<String> = _aiExplanation

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    private val _isAutoSignalsEnabled = MutableStateFlow(false)
    val isAutoSignalsEnabled: StateFlow<Boolean> = _isAutoSignalsEnabled

    private var lastSavedSignalTimestamp = 0L
    private var dataJob: Job? = null

    init {
        startDataCollection()
        startTimer()
    }

    private fun saveSignalToDb(signal: Signal, entryPrice: Double) {
        viewModelScope.launch {
            signalDao.insertSignal(
                com.finorix.signals.data.local.entity.SignalEntity(
                    pair = signal.pair,
                    direction = signal.direction,
                    confidence = signal.confidence,
                    timestamp = signal.timestamp,
                    expiryTimestamp = signal.timestamp + (signal.expiresIn * 1000),
                    entryPrice = entryPrice,
                    indicatorJson = gson.toJson(signal.indicators)
                )
            )
        }
    }

    fun explainSignal(signal: Signal, candles: List<Candle>) {
        _aiExplanation.value = ""
        _isAiLoading.value = true
        viewModelScope.launch {
            aiRepository.explainSignal(signal, candles)
                .onCompletion { _isAiLoading.value = false }
                .collect { chunk ->
                    _aiExplanation.value += chunk
                }
        }
    }

    fun toggleAutoSignals(context: android.content.Context) {
        val newState = !_isAutoSignalsEnabled.value
        _isAutoSignalsEnabled.value = newState
        val intent = Intent(context, SignalWatcherService::class.java)
        
        if (newState) {
            context.startForegroundService(intent)
            scheduleBackgroundWork(context)
        } else {
            context.stopService(intent)
            cancelBackgroundWork(context)
        }
    }

    private fun scheduleBackgroundWork(context: android.content.Context) {
        val workRequest = PeriodicWorkRequestBuilder<SignalWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SignalWatcherWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelBackgroundWork(context: android.content.Context) {
        WorkManager.getInstance(context).cancelUniqueWork("SignalWatcherWork")
    }

    fun selectPair(pair: String) {
        _selectedPair.value = pair
        startDataCollection()
    }

    fun selectTimeframe(timeframe: String) {
        _selectedTimeframe.value = timeframe
        startDataCollection()
    }

    private fun startDataCollection() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            repository.getCandles(_selectedPair.value, _selectedTimeframe.value)
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            if (_uiState.value !is SignalUiState.Success) {
                                _uiState.value = SignalUiState.Loading
                            }
                        }
                        is Result.Success -> {
                            val signal = signalEngine.generateSignal(_selectedPair.value, result.data)
                            _uiState.value = SignalUiState.Success(
                                candles = result.data,
                                signal = signal,
                                lastPrice = result.data.lastOrNull()?.close ?: 0.0
                            )
                            
                            // Auto-save high confidence signals to history
                            if (signal != null && signal.timestamp > lastSavedSignalTimestamp) {
                                lastSavedSignalTimestamp = signal.timestamp
                                saveSignalToDb(signal, result.data.last().close)
                            }
                        }
                        is Result.Error -> {
                            _uiState.value = SignalUiState.Error(result.message)
                        }
                    }
                }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_countdown.value > 0) {
                    _countdown.value -= 1
                } else {
                    _countdown.value = 60
                }
            }
        }
    }
}

sealed class SignalUiState {
    object Loading : SignalUiState()
    data class Success(
        val candles: List<Candle>,
        val signal: Signal?,
        val lastPrice: Double
    ) : SignalUiState()
    data class Error(val message: String) : SignalUiState()
}
