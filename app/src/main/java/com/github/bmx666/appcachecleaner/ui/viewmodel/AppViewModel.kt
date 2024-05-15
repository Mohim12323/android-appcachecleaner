package com.github.bmx666.appcachecleaner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bmx666.appcachecleaner.data.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
) : ViewModel() {

    private val _nightMode = MutableStateFlow<Boolean>(false)
    val nightMode: StateFlow<Boolean> get() = _nightMode.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesManager.userPreferencesFlow.collect { prefs ->
                _nightMode.value = prefs.ui.nightMode
            }
        }
    }
}