/******************************************************************************
 *                                                                            *
 * Copyright (C) 2025 by lingyicute <li@92li.us.kg>             *
 * Copyright (C) 2021 by Max Lv <max.c.lv@gmail.com>                          *
 * Copyright (C) 2021 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.database.preference

import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.preference.EditTextPreference


object EditTextPreferenceModifiers {
    object Monospace : EditTextPreference.OnBindEditTextListener {
        override fun onBindEditText(editText: EditText) {
            editText.typeface = Typeface.MONOSPACE
        }
    }

    object Hosts : EditTextPreference.OnBindEditTextListener {

        override fun onBindEditText(editText: EditText) {
            editText.setHorizontallyScrolling(true)
            editText.setSelection(editText.text.length)
        }
    }

    object Port : EditTextPreference.OnBindEditTextListener {
        private val portLengthFilter = arrayOf(InputFilter.LengthFilter(5))

        override fun onBindEditText(editText: EditText) {
            editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
            editText.filters = portLengthFilter
            editText.setSingleLine()
            editText.setSelection(editText.text.length)
        }
    }

    object Number : EditTextPreference.OnBindEditTextListener {

        override fun onBindEditText(editText: EditText) {
            editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
            editText.setSingleLine()
            editText.setSelection(editText.text.length)
        }
    }

    object Mux : EditTextPreference.OnBindEditTextListener {

        override fun onBindEditText(editText: EditText) {
            editText.inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_SIGNED
            editText.setSingleLine()
            editText.setSelection(editText.text.length)
        }
    }
}
