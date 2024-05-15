package com.github.bmx666.appcachecleaner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bmx666.appcachecleaner.data.UserPreferencesManager
import com.github.bmx666.appcachecleaner.data.UserPreferencesUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
) : ViewModel() {

    data class PreferencesUI(
        var nightMode: MutableStateFlow<Boolean> = MutableStateFlow(false),
    )

    data class PreferencesFilter(
        var hideDisabledApps: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var hideIgnoredApps: MutableStateFlow<Boolean> = MutableStateFlow(false),
    )

    data class Preferences(
        val ui: PreferencesUI = PreferencesUI(),
        val filter: PreferencesFilter = PreferencesFilter(),
    )

    private val prefs = Preferences()

    val uiNightMode: StateFlow<Boolean> get() =
        prefs.ui.nightMode.asStateFlow()

    val filterHideDisabledApps: StateFlow<Boolean> get() =
        prefs.filter.hideDisabledApps.asStateFlow()
    val filterHideIgnoredApps: StateFlow<Boolean> get() =
        prefs.filter.hideIgnoredApps.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesManager.userPreferencesFlow.collect { userPrefs ->
                prefs.ui.nightMode.value =
                    userPrefs.ui.nightMode

                prefs.filter.hideDisabledApps.value =
                    userPrefs.filter.hideDisabledApps
                prefs.filter.hideIgnoredApps.value =
                    userPrefs.filter.hideIgnoredApps
            }
        }
    }

    fun toggleNightMode() {
        viewModelScope.launch {
            userPreferencesManager.toggleNightMode()
        }
    }

    fun toggleFilterHideDisabledApps() {
        viewModelScope.launch {
            userPreferencesManager.toggleFilterHideDisabledApps()
        }
    }

    fun toggleFilterHideIgnoredApps() {
        viewModelScope.launch {
            userPreferencesManager.toggleFilterHideIgnoredApps()
        }
    }
}