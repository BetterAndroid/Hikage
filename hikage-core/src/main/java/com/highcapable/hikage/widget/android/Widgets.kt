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
 * This file is created by fankes on 2025/2/26.
 */
@file:Suppress("unused", "DEPRECATION")
@file:JvmName("WidgetsDeclaration")

package com.highcapable.hikage.widget.android

import android.view.SurfaceView
import android.webkit.WebView
import android.widget.ActionMenuView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.Chronometer
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.GridView
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.MediaController
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.QuickContactBadge
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.Space
import android.widget.Spinner
import android.widget.Switch
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextClock
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.TimePicker
import android.widget.ToggleButton
import android.widget.Toolbar
import android.widget.VideoView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import com.highcapable.hikage.annotation.HikageViewDeclaration

@HikageViewDeclaration(SeekBar::class)
private object SeekBarDeclaration

@HikageViewDeclaration(LinearLayout::class, LinearLayout.LayoutParams::class)
private object LinearLayoutDeclaration

@HikageViewDeclaration(RelativeLayout::class, RelativeLayout.LayoutParams::class)
private object RelativeLayoutDeclaration

@HikageViewDeclaration(FrameLayout::class, FrameLayout.LayoutParams::class)
private object FrameLayoutDeclaration

@HikageViewDeclaration(ScrollView::class, FrameLayout.LayoutParams::class)
private object ScrollViewDeclaration

@HikageViewDeclaration(ProgressBar::class)
private object ProgressBarDeclaration

@HikageViewDeclaration(Chronometer::class)
private object ChronometerDeclaration

@HikageViewDeclaration(Space::class)
private object SpaceDeclaration

@HikageViewDeclaration(CheckedTextView::class)
private object CheckedTextViewDeclaration

@HikageViewDeclaration(ExpandableListView::class, final = true)
private object ExpandableListViewDeclaration

@HikageViewDeclaration(Spinner::class)
private object SpinnerDeclaration

@HikageViewDeclaration(RadioGroup::class, RadioGroup.LayoutParams::class)
private object RadioGroupDeclaration

@HikageViewDeclaration(RadioButton::class)
private object RadioButtonDeclaration

@HikageViewDeclaration(ToggleButton::class)
private object ToggleButtonDeclaration

@HikageViewDeclaration(CheckBox::class)
private object CheckBoxDeclaration

@HikageViewDeclaration(EditText::class)
private object EditTextDeclaration

@HikageViewDeclaration(AutoCompleteTextView::class)
private object AutoCompleteTextViewDeclaration

@HikageViewDeclaration(Button::class)
private object ButtonDeclaration

@HikageViewDeclaration(ImageButton::class)
private object ImageButtonDeclaration

@HikageViewDeclaration(TextView::class)
private object TextViewDeclaration

@HikageViewDeclaration(TextClock::class)
private object TextClockDeclaration

@HikageViewDeclaration(TextSwitcher::class, FrameLayout.LayoutParams::class)
private object TextSwitcherDeclaration

@HikageViewDeclaration(ActionMenuView::class, ActionMenuView.LayoutParams::class)
private object ActionMenuViewDeclaration

@HikageViewDeclaration(CalendarView::class, final = true)
private object CalendarViewDeclaration

@HikageViewDeclaration(DatePicker::class, final = true)
private object DatePickerDeclaration

@HikageViewDeclaration(TimePicker::class, final = true)
private object TimePickerDeclaration

@HikageViewDeclaration(RatingBar::class)
private object RatingBarDeclaration

@HikageViewDeclaration(HorizontalScrollView::class, FrameLayout.LayoutParams::class)
private object HorizontalScrollViewDeclaration

@HikageViewDeclaration(QuickContactBadge::class)
private object QuickContactBadgeDeclaration

@HikageViewDeclaration(ImageSwitcher::class, FrameLayout.LayoutParams::class)
private object ImageSwitcherDeclaration

@HikageViewDeclaration(ViewSwitcher::class, FrameLayout.LayoutParams::class)
private object ViewSwitcherDeclaration

@HikageViewDeclaration(ViewFlipper::class, FrameLayout.LayoutParams::class)
private object ViewFlipperDeclaration

@HikageViewDeclaration(ViewAnimator::class, FrameLayout.LayoutParams::class)
private object ViewAnimatorDeclaration

@HikageViewDeclaration(SurfaceView::class)
private object SurfaceVieweclaration

@HikageViewDeclaration(VideoView::class)
private object VideoViewDeclaration

@HikageViewDeclaration(WebView::class, final = true)
private object WebViewDeclaration

@HikageViewDeclaration(Toolbar::class, final = true)
private object ToolbarDeclaration

@HikageViewDeclaration(GridLayout::class, GridLayout.LayoutParams::class)
private object GridLayoutDeclaration

@HikageViewDeclaration(GridView::class, final = true)
private object GridViewDeclaration

@HikageViewDeclaration(ListView::class, final = true)
private object ListViewDeclaration

@HikageViewDeclaration(ImageView::class)
private object ImageViewDeclaration

@HikageViewDeclaration(MediaController::class, FrameLayout.LayoutParams::class)
private object MediaControllerDeclaration

@HikageViewDeclaration(TableLayout::class, TableLayout.LayoutParams::class)
private object TableLayoutDeclaration

@HikageViewDeclaration(TableRow::class, TableRow.LayoutParams::class)
private object TableRowDeclaration

@HikageViewDeclaration(NumberPicker::class, final = true)
private object NumberPickerDeclaration

@HikageViewDeclaration(SearchView::class, final = true)
private object SearchViewDeclaration

@HikageViewDeclaration(Switch::class)
private object SwitchDeclaration

@HikageViewDeclaration(TabHost::class, FrameLayout.LayoutParams::class)
private object TabHostDeclaration

@HikageViewDeclaration(TabWidget::class, final = true)
private object TabWidgetDeclaration