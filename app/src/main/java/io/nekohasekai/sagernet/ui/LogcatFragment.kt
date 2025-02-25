/******************************************************************************
 * Copyright (C) 2025 by lingyicute <contact-git@sekai.icu>                  *
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

package io.nekohasekai.sagernet.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.graphics.TypefaceCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import cn.hutool.core.util.RuntimeUtil
import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalViewClient
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.databinding.LayoutLogcatBinding
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.utils.CrashHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.math.min

class LogcatFragment : ToolbarFragment(R.layout.layout_logcat),
    TerminalSessionClient,
    TerminalViewClient,
    Toolbar.OnMenuItemClickListener {

    lateinit var binding: LayoutLogcatBinding
    var fontSize = dp2px(8)

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.menu_log)

        toolbar.inflateMenu(R.menu.logcat_menu)
        toolbar.setOnMenuItemClickListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                top = bars.top,
                left = bars.left,
                right = bars.right,
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.layout_terminal)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left + dp2px(8),
                right = bars.right + dp2px(8),
                bottom = bars.bottom + dp2px(8),
            )
            WindowInsetsCompat.CONSUMED
        }

        binding = LayoutLogcatBinding.bind(view)
        val terminalView = binding.terminalView;

        // Make it divisible by 2 since that is the minimal adjustment step:
        if (fontSize % 2 == 1) fontSize--

        terminalView.setTerminalViewClient(this)
        terminalView.setTextSize(fontSize)
        terminalView.setTypeface(
            TypefaceCompat.createFromResourcesFontFile(
                view.context,
                resources,
                R.font.jetbrains_mono,
                "res/font/jetbrains_mono.ttf",
                0,
                Typeface.NORMAL
            )
        )

        reloadSession()

        registerForContextMenu(terminalView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        binding.terminalView.showContextMenu()
    }

    fun reloadSession() {
        val terminalView = binding.terminalView
        terminalView.currentSession?.also {
            it.finishIfRunning()
        }
        val args = mutableListOf("-C")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            args += arrayOf(
                "-v",
                "tag,color",
            )
        }

        args += arrayOf(
            "AndroidRuntime:D",
            "ProxyInstance:D",
            "GuardedProcessPool:D",
            "RawUpdater:D",
            "SIP008Updater:D",
            "OpenOnlineConfigUpdater:D",
            "VpnService:D",
            "libcore:D",
            "v2ray-core:D",
            "su:D",
            "libnaive:D",
            "libbrook:D",
            "libhysteria:D",
            "libhysteria2:D",
            "libmieru:D",
            "libtuic5:D",
            "libtuic:D",
            "libtrojan-go:D",
            "libshadowtls:D",
            "libjuicity:D",
            "*:S",
        )

        val session = TerminalSession(
            "/system/bin/logcat",
            app.cacheDir.absolutePath,
            args.toTypedArray(),
            arrayOf(),
            3000,
            this
        )

        terminalView.attachSession(session)
        terminalView.updateSize()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_logcat -> {
                runOnDefaultDispatcher {
                    try {
                        RuntimeUtil.exec("/system/bin/logcat", "-c").waitFor()
                    } catch (e: Exception) {
                        onMainDispatcher {
                            snackbar(e.readableMessage).show()
                        }
                        return@runOnDefaultDispatcher
                    }
                    onMainDispatcher {
                        reloadSession()
                    }
                }

            }
            R.id.action_send_logcat -> {
                val context = requireContext()

                runOnDefaultDispatcher {
                    val logFile = File.createTempFile("YiLink ",
                        ".log",
                        File(app.cacheDir, "log").also { it.mkdirs() })

                    var report = CrashHandler.buildReportHeader()

                    report += "Logcat: \n\n"

                    logFile.writeText(report)

                    try {
                        Runtime.getRuntime().exec(arrayOf("logcat", "-d")).inputStream.use(
                            FileOutputStream(
                                logFile, true
                            )
                        )
                    } catch (e: IOException) {
                        Logs.w(e)
                        logFile.appendText("Export logcat error: " + CrashHandler.formatThrowable(e))
                    }

                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).setType("text/x-log")
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                .putExtra(
                                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                        context, BuildConfig.APPLICATION_ID + ".cache", logFile
                                    )
                                ), context.getString(androidx.appcompat.R.string.abc_shareactionprovider_share_with)
                        )
                    )
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::binding.isInitialized) {
            binding.terminalView.currentSession?.finishIfRunning()
        }
    }

    override fun onTextChanged(changedSession: TerminalSession) {
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        if (text.isNullOrBlank()) {
            return
        }

        SagerNet.trySetPrimaryClip(text)
        snackbar(R.string.copy_success).show()
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
    }

    override fun onBell(session: TerminalSession) {
    }

    override fun onColorsChanged(session: TerminalSession) {
    }

    override fun onTerminalCursorStateChange(state: Boolean) {
    }

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {
    }

    override val terminalCursorStyle: Int
        get() = 0

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1f
            changeFontSize(increase)
            return 1.0f
        }

        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
    }

    override val shouldBackButtonBeMappedToEscape: Boolean
        get() = false

    override val shouldEnforceCharBasedInput: Boolean
        get() = false

    override val shouldUseCtrlSpaceWorkaround: Boolean
        get() = false

    override val isTerminalViewSelected: Boolean
        get() = true

    override fun copyModeChanged(copyMode: Boolean) {
        val activity = requireActivity() as MainActivity

        activity.binding.drawerLayout.setDrawerLockMode(
            if (copyMode) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        return false
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
        return false
    }

    override fun disableInput(): Boolean {
        return true
    }

    override fun onEmulatorSet() {
        val props = Properties()
        props.load(requireContext().assets.open("terminal.properties"))
        TerminalColors.COLOR_SCHEME.updateWith(props)

        val emulator = binding.terminalView.currentSession!!.emulator
        emulator!!.mColors.reset()
    }

    override fun onScroll(offset: Int) {
        val activity = requireActivity() as MainActivity
        val topRow = binding.terminalView.topRow
        if (offset < 0) {
            activity.binding.stats.apply {
                if (isShown) performHide()
            }
        }

        val screen = binding.terminalView.mEmulator!!.screen

        if (topRow == 0 && screen.activeTranscriptRows > 0) activity.binding.fab.apply {
            if (isShown) hide()
        } else activity.binding.fab.apply {
            if (!isShown) show()
        }
    }

    override fun logError(tag: String?, message: String?) {
    }

    override fun logWarn(tag: String?, message: String?) {
    }

    override fun logInfo(tag: String?, message: String?) {
    }

    override fun logDebug(tag: String?, message: String?) {
    }

    override fun logVerbose(tag: String?, message: String?) {
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
    }

    private fun changeFontSize(increase: Boolean) {
        val terminalView = binding.terminalView
        fontSize += if (increase) 1 else -1
        fontSize = max(MIN_FONTSIZE, min(fontSize, MAX_FONTSIZE))
        terminalView.setTextSize(fontSize)
    }

    companion object {
        private val MIN_FONTSIZE = dp2px(4)
        private val MAX_FONTSIZE = dp2px(12)
    }
}