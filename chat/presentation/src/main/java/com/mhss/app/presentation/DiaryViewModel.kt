package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.*
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.inTheLast30Days
import com.mhss.app.util.date.inTheLastYear
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class DiaryViewModel(
    private val getAlEntries: GetAllEntriesUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getEntriesForChart: GetDiaryForChartUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private var getEntriesJob: Job? = null

    init {
        viewModelScope.launch {
            getPreference(
                intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                Order.DateModified(OrderType.ASC).toInt()
            ).collect {
                getEntries(it.toOrder())
            }
        }
    }

    fun onEvent(event: DiaryEvent) {
        when (event) {
            is DiaryEvent.SearchEntries -> viewModelScope.launch {
                val entries = searchEntries(event.query)
                uiState = uiState.copy(
                    searchEntries = entries
                )
            }
            is DiaryEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.DIARY_ORDER_KEY),
                    event.order.toInt()
                )
            }
            is DiaryEvent.ChangeChartEntriesRange -> viewModelScope.launch {
                uiState = uiState.copy(chartEntries = getEntriesForChart {
                    if (event.monthly) it.createdDate.inTheLast30Days()
                    else it.createdDate.inTheLastYear()
                })
            }
        }
    }

    data class UiState(
        val entries: Map<String, List<DiaryEntry>> = emptyMap(),
        val entriesOrder: Order = Order.DateModified(OrderType.ASC),
        val searchEntries: List<DiaryEntry> = emptyList(),
        val chartEntries : List<DiaryEntry> = emptyList()
    )

    private fun getEntries(order: Order) {
        getEntriesJob?.cancel()
        getEntriesJob = getAlEntries(order)
            .onEach { entries ->
                uiState = uiState.copy(
                    entries = entries.groupBy {
                        it.createdDate.formatDateForMapping()
                    },
                    entriesOrder = order
                )
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

}