package com.finorix.signals.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finorix.signals.data.local.dao.SignalDao
import com.finorix.signals.data.local.entity.SignalEntity
import com.finorix.signals.data.local.entity.SignalOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    private val signalRepository: com.finorix.signals.domain.repository.SignalRepository
) : ViewModel() {

    private val _filter = MutableStateFlow("All")
    val filter: StateFlow<String> = _filter

    val signals: StateFlow<List<com.finorix.signals.domain.model.Signal>> = combine(
        signalRepository.getUserSignals(50),
        _filter
    ) { list, filter ->
        when (filter) {
            "Wins" -> list.filter { it.outcome == "WIN" }
            "Losses" -> list.filter { it.outcome == "LOSS" }
            "Today" -> {
                val todayStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                list.filter { it.timestamp > todayStart }
            }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val winRate: StateFlow<Int> = signalRepository.getWinRate(7).map { (it * 100).toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(filter: String) {
        _filter.value = filter
    }
}
