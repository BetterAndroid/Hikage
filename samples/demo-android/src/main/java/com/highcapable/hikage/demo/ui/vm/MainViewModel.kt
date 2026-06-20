/*
 * Hikage - A real-time Android View runtime powered by Kotlin DSL.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2026/6/21.
 */
package com.highcapable.hikage.demo.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainViewModel : ViewModel() {

    private val mutableEffects = MutableSharedFlow<MainEffect>()
    private val mutableUiState = MutableStateFlow(MainUiState())
    private val mutableCompatibilityStatus = MutableLiveData<MainCompatibilityStatus>(MainCompatibilityStatus.Ready)

    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()
    val effects: SharedFlow<MainEffect> = mutableEffects.asSharedFlow()
    val compatibilityStatus: LiveData<MainCompatibilityStatus> = mutableCompatibilityStatus

    val runtimeTicker = flow {
        var seconds = 0
        while (true) {
            emit(seconds++)
            delay(1_000.milliseconds)
        }
    }

    fun updateUsername(value: String) {
        mutableUiState.update { it.copy(username = value) }
    }

    fun updatePassword(value: String) {
        mutableUiState.update { it.copy(password = value) }
    }

    fun selectGender(value: String) {
        mutableUiState.update { it.copy(gender = value) }
    }

    fun unselectGender(value: String) {
        mutableUiState.update {
            if (it.gender == value) it.copy(gender = "") else it
        }
    }

    fun setNotificationEnabled(value: Boolean) {
        mutableUiState.update { it.copy(isNotificationEnabled = value) }
        mutableCompatibilityStatus.value = if (value)
            MainCompatibilityStatus.NotificationEnabled
        else MainCompatibilityStatus.NotificationDisabled
    }

    fun submit() {
        val state = uiState.value
        if (state.canSubmit) {
            mutableUiState.update { it.copy(submitCount = it.submitCount + 1) }
            mutableCompatibilityStatus.value = MainCompatibilityStatus.Submitted(state.username)
            sendEffect(MainEffect.WelcomeToast(state.username))
        } else sendEffect(MainEffect.FillRequiredToast)
    }

    private fun sendEffect(effect: MainEffect) {
        viewModelScope.launch {
            mutableEffects.emit(effect)
        }
    }
}