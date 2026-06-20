/*
 * Hikage - A Kotlin DSL-based Android real-time UI building framework.
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
 * This file is created by fankes on 2025/2/18.
 */
@file:Suppress("SetTextI18n", "PrivateResource")

package com.highcapable.hikage.demo.ui

import android.os.Bundle
import android.text.InputType
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.view.toast
import com.highcapable.hikage.core.attrs.android
import com.highcapable.hikage.core.attrs.app
import com.highcapable.hikage.core.layout.LayoutParams
import com.highcapable.hikage.demo.R
import com.highcapable.hikage.demo.ui.base.BaseActivity
import com.highcapable.hikage.demo.ui.vm.MainCompatibilityStatus
import com.highcapable.hikage.demo.ui.vm.MainEffect
import com.highcapable.hikage.demo.ui.vm.MainViewModel
import com.highcapable.hikage.extension.setContentView
import com.highcapable.hikage.runtime.lifecycle.collectEffect
import com.highcapable.hikage.runtime.lifecycle.collectState
import com.highcapable.hikage.runtime.lifecycle.setState
import com.highcapable.hikage.widget.android.widget.Button
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.coordinatorlayout.widget.CoordinatorLayout
import com.highcapable.hikage.widget.com.google.android.material.appbar.MaterialToolbar
import com.highcapable.hikage.widget.com.google.android.material.card.MaterialCardView
import com.highcapable.hikage.widget.com.google.android.material.chip.ChipGroup
import com.highcapable.hikage.widget.com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputEditText
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputLayout
import com.highcapable.hikage.widget.com.highcapable.hikage.demo.ui.widget.CheckableChip
import com.google.android.material.R as Material_R

class MainActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectEffect(viewModel.effects, lifecycleOwner = this) { effect ->
            when (effect) {
                MainEffect.FillRequiredToast -> toast(getString(R.string.login_info_not_fill_tip))
                is MainEffect.WelcomeToast -> toast(getString(R.string.runtime_welcome_toast, effect.username))
            }
        }

        setContentView {
            CoordinatorLayout(
                lparams = LayoutParams(matchParent = true)
            ) {
                MaterialToolbar(
                    lparams = LayoutParams(widthMatchParent = true),
                    attrs = {
                        app {
                            set("title", R.string.app_name)
                        }
                    }
                )
                LinearLayout(
                    lparams = LayoutParams(matchParent = true) {
                        topMargin = dimenResource(Material_R.dimen.m3_appbar_size_compact).toInt()
                    },
                    init = {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16.dp)
                    }
                ) {
                    TextInputLayout(
                        lparams = LayoutParams(widthMatchParent = true),
                        init = {
                            hint = stringResource(R.string.text_username)
                        }
                    ) {
                        TextInputEditText(
                            lparams = LayoutParams(widthMatchParent = true)
                        ) {
                            isSingleLine = true
                            doOnTextChanged { text, _, _, _ ->
                                viewModel.updateUsername(text.toString())
                            }
                        }
                    }
                    TextInputLayout(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 12.dp
                        },
                        attrs = {
                            android.set("hint", R.string.text_password)
                            app.set("endIconMode", TextInputLayout.END_ICON_PASSWORD_TOGGLE)
                        }
                    ) {
                        TextInputEditText(
                            lparams = LayoutParams(widthMatchParent = true)
                        ) {
                            isSingleLine = true
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            doOnTextChanged { text, _, _, _ ->
                                viewModel.updatePassword(text.toString())
                            }
                        }
                    }
                    ChipGroup(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 16.dp
                        },
                        init = {
                            isSingleSelection = true
                        }
                    ) {
                        listOf(
                            stringResource(R.string.text_gender_man),
                            stringResource(R.string.text_gender_woman)
                        ).forEach { gender ->
                            CheckableChip {
                                text = gender
                                setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) viewModel.selectGender(gender)
                                    else viewModel.unselectGender(gender)
                                }
                                setState(viewModel.uiState) {
                                    val shouldBeChecked = it.gender == gender
                                    if (isChecked != shouldBeChecked)
                                        isChecked = shouldBeChecked
                                }
                            }
                        }
                    }
                    MaterialSwitch(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 16.dp
                        },
                        init = {
                            text = stringResource(R.string.text_enable_notification)
                            setOnCheckedChangeListener { _, isChecked ->
                                viewModel.setNotificationEnabled(isChecked)
                            }
                            setState(viewModel.uiState) {
                                if (isChecked != it.isNotificationEnabled)
                                    isChecked = it.isNotificationEnabled
                            }
                        }
                    )
                    MaterialCardView(
                        lparams = LayoutParams(matchParent = true) {
                            topMargin = 16.dp
                            weight = 1f
                        }
                    ) {
                        LinearLayout(
                            lparams = LayoutParams(matchParent = true),
                            init = {
                                orientation = LinearLayout.VERTICAL
                                setPadding(16.dp)
                            }
                        ) {
                            TextView {
                                text = stringResource(R.string.text_welcome)
                            }
                            TextView(
                                lparams = LayoutParams {
                                    topMargin = 8.dp
                                }
                            ) {
                                text = stringResource(R.string.text_description)
                            }
                            TextView(
                                lparams = LayoutParams {
                                    topMargin = 16.dp
                                }
                            ) {
                                setState(viewModel.uiState) {
                                    text = stringResource(
                                        R.string.runtime_state_summary,
                                        it.username.ifBlank { "-" },
                                        it.passwordMask.ifBlank { "-" },
                                        it.gender.ifBlank { "-" },
                                        if (it.isNotificationEnabled)
                                            stringResource(R.string.text_enabled)
                                        else stringResource(R.string.text_disabled)
                                    )
                                }
                            }
                            TextView(
                                lparams = LayoutParams {
                                    topMargin = 12.dp
                                }
                            ) {
                                text = stringResource(R.string.runtime_flow_waiting)
                                setState(
                                    flow = viewModel.runtimeTicker,
                                    initialValue = 0
                                ) {
                                    text = stringResource(R.string.runtime_flow_ticker, it)
                                }
                            }
                            TextView(
                                lparams = LayoutParams {
                                    topMargin = 12.dp
                                }
                            ) {
                                text = stringResource(R.string.runtime_collect_waiting)
                                collectState(viewModel.runtimeTicker) {
                                    text = stringResource(
                                        R.string.runtime_collect_state,
                                        stringResource(R.string.runtime_flow_ticker, it)
                                    )
                                }
                            }
                            TextView(
                                lparams = LayoutParams {
                                    topMargin = 12.dp
                                }
                            ) {
                                text = stringResource(R.string.runtime_livedata_waiting)
                                setState(viewModel.compatibilityStatus) {
                                    text = when (it) {
                                        MainCompatibilityStatus.Ready -> stringResource(R.string.runtime_livedata_ready)
                                        MainCompatibilityStatus.NotificationEnabled ->
                                            stringResource(R.string.runtime_livedata_notification_enabled)
                                        MainCompatibilityStatus.NotificationDisabled ->
                                            stringResource(R.string.runtime_livedata_notification_disabled)
                                        is MainCompatibilityStatus.Submitted ->
                                            stringResource(R.string.runtime_livedata_submitted, it.username)
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 20.dp
                        }
                    ) {
                        setState(viewModel.uiState) {
                            isEnabled = it.canSubmit
                            text = stringResource(R.string.text_submit_count, it.submitCount)
                        }
                        setOnClickListener {
                            viewModel.submit()
                        }
                    }
                }
            }
        }
    }
}