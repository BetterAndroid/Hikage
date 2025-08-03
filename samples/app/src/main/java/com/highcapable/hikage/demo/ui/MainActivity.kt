/*
 * Hikage - An Android responsive UI building tool.
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.view.toast
import com.highcapable.hikage.demo.R
import com.highcapable.hikage.demo.ui.base.BaseActivity
import com.highcapable.hikage.extension.setContentView
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.coordinatorlayout.widget.CoordinatorLayout
import com.highcapable.hikage.widget.com.google.android.material.appbar.MaterialToolbar
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.card.MaterialCardView
import com.highcapable.hikage.widget.com.google.android.material.chip.ChipGroup
import com.highcapable.hikage.widget.com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputEditText
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputLayout
import com.highcapable.hikage.widget.com.highcapable.hikage.demo.ui.widget.CheckableChip
import android.R as Android_R
import com.google.android.material.R as Material_R

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView {
            var username = ""
            var password = ""

            CoordinatorLayout(
                lparams = LayoutParams(matchParent = true)
            ) {
                MaterialToolbar(
                    lparams = LayoutParams(widthMatchParent = true),
                    init = {
                        title = stringResource(R.string.app_name)
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
                                username = text.toString()
                            }
                        }
                    }
                    TextInputLayout(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 12.dp
                        },
                        init = {
                            hint = stringResource(R.string.text_password)
                            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                        }
                    ) {
                        TextInputEditText(
                            lparams = LayoutParams(widthMatchParent = true)
                        ) {
                            isSingleLine = true
                            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            doOnTextChanged { text, _, _, _ -> 
                                password = text.toString()
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
                        repeat(2) { index ->
                            CheckableChip {
                                text = when (index) {
                                    0 -> stringResource(R.string.text_gender_man)
                                    else -> stringResource(R.string.text_gender_woman)
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
                        }
                    }
                    MaterialButton(
                        lparams = LayoutParams(widthMatchParent = true) {
                            topMargin = 20.dp
                        }
                    ) {
                        text = stringResource(R.string.text_submit)
                        setOnClickListener {
                            if (username.isNotEmpty() && password.isNotEmpty())
                                MaterialAlertDialogBuilder(this@MainActivity)
                                    .setTitle(stringResource(R.string.login_info))
                                    .setMessage(stringResource(R.string.login_info_description, username, password))
                                    .setPositiveButton(stringResource(Android_R.string.ok), null)
                                    .show()
                            else toast(stringResource(R.string.login_info_not_fill_tip))
                        }
                    }
                }
            }
        }
    }
}