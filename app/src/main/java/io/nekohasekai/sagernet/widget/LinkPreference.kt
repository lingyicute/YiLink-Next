/******************************************************************************
 *                                                                            *
 * Copyright (C) 2025 by lingyicute <li@92li.us.kg>             *
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

package io.nekohasekai.sagernet.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.takisoft.preferencex.EditTextPreference
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.app
import io.nekohasekai.sagernet.ktx.readableMessage
import libcore.Libcore

class LinkPreference : EditTextPreference {

    var defaultValue: String? = null

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : this(context, attrs, com.takisoft.preferencex.R.attr.editTextPreferenceStyle)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        val a = context.obtainStyledAttributes(
            attrs, androidx.preference.R.styleable.Preference, defStyleAttr, defStyleRes
        )
        if (a.hasValue(androidx.preference.R.styleable.Preference_defaultValue)) {
            defaultValue = onGetDefaultValue(
                a, androidx.preference.R.styleable.Preference_defaultValue
            )?.toString()
        } else if (a.hasValue(androidx.preference.R.styleable.Preference_android_defaultValue)) {
            defaultValue = onGetDefaultValue(
                a, androidx.preference.R.styleable.Preference_android_defaultValue
            )?.toString()
        }
    }

    init {
        dialogLayoutResource = R.layout.layout_link_dialog

        setOnBindEditTextListener {
            val linkLayout = it.rootView.findViewById<TextInputLayout>(R.id.input_layout)
            fun validate() {
                val link = it.text
                if (link.isEmpty()) {
                    linkLayout.isErrorEnabled = false
                    return
                }
                try {
                    if (link.toString().contains("\n")) {
                        error("invalid url")
                    }
                    val uri = Uri.parse(link.toString())

                    if (uri.scheme.isNullOrBlank()) {
                        error("Missing scheme in url")
                    } else if (uri.scheme == "http") {
                        linkLayout.error = app.getString(R.string.cleartext_http_warning)
                        linkLayout.isErrorEnabled = true
                    } else if (uri.scheme != "https") {
                        error("Invalid scheme ${uri.scheme}")
                    } else {
                        linkLayout.isErrorEnabled = false
                    }
                } catch (e: Exception) {
                    linkLayout.error = e.readableMessage
                    linkLayout.isErrorEnabled = true
                }
            }
            validate()
            it.addTextChangedListener {
                validate()
            }
        }

        setOnPreferenceChangeListener { _, newValue ->
            if ((newValue as String).isBlank()) {
                text = defaultValue
                false
            } else try {
                Libcore.parseURL(newValue)
                true
            } catch (ignored: Exception) {
                false
            }
        }
    }

}