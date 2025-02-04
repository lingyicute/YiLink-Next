package com.termux.terminal

import java.util.*

/**
 * Color scheme for a terminal with default colors, which may be overridden (and then reset) from the shell using
 * Operating System Control (OSC) sequences.
 *
 * @see TerminalColors
 */
class TerminalColorScheme {
    @JvmField
    val mDefaultColors = IntArray(TextStyle.NUM_INDEXED_COLORS)

    init {
        reset()
    }

    private fun reset() {
        System.arraycopy(DEFAULT_COLORSCHEME, 0, mDefaultColors, 0, TextStyle.NUM_INDEXED_COLORS)
    }

    fun updateWith(props: Properties) {
        reset()
        var cursorPropExists = false
        for ((key1, value1) in props) {
            val key = key1 as String
            val value = value1 as String
            var colorIndex: Int
            if (key == "foreground") {
                colorIndex = TextStyle.COLOR_INDEX_FOREGROUND
            } else if (key == "background") {
                colorIndex = TextStyle.COLOR_INDEX_BACKGROUND
            } else if (key == "cursor") {
                colorIndex = TextStyle.COLOR_INDEX_CURSOR
                cursorPropExists = true
            } else if (key.startsWith("color")) {
                colorIndex = try {
                    key.substring(5).toInt()
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Invalid property: '$key'")
                }
            } else {
                throw IllegalArgumentException("Invalid property: '$key'")
            }
            val colorValue = TerminalColors.parse(value)
            require(colorValue != 0) { "Property '$key' has invalid color: '$value'" }
            mDefaultColors[colorIndex] = colorValue
        }
        if (!cursorPropExists) setCursorColorForBackground()
    }

    /**
     * If the "cursor" color is not set by user, we need to decide on the appropriate color that will
     * be visible on the current terminal background. White will not be visible on light backgrounds
     * and black won't be visible on dark backgrounds. So we find the perceived brightness of the
     * background color and if its below the threshold (too dark), we use white cursor and if its
     * above (too bright), we use black cursor.
     */
    fun setCursorColorForBackground() {
        val backgroundColor = mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND]
        val brightness = TerminalColors.getPerceivedBrightnessOfColor(backgroundColor)
        if (brightness > 0) {
            if (brightness < 130) mDefaultColors[TextStyle.COLOR_INDEX_CURSOR] = -0x1 else mDefaultColors[TextStyle.COLOR_INDEX_CURSOR] = -0x1000000
        }
    }

    companion object {
        /** http://upload.wikimedia.org/wikipedia/en/1/15/Xterm_256color_chart.svg, but with blue color brighter.  */
        private val DEFAULT_COLORSCHEME = intArrayOf( // 16 original colors. First 8 are dim.
            -0x1000000,  // black
            -0x330000,  // dim red
            -0xff3300,  // dim green
            -0x323300,  // dim yellow
            -0x9b6a13,  // dim blue
            -0x32ff33,  // dim magenta
            -0xff3233,  // dim cyan
            -0x1a1a1b,  // dim white
            // Second 8 are bright:
            -0x808081,  // medium grey
            -0x10000,  // bright red
            -0xff0100,  // bright green
            -0x100,  // bright yellow
            -0xa3a301,  // light blue
            -0xff01,  // bright magenta
            -0xff0001,  // bright cyan
            -0x1,  // bright white
            // 216 color cube, six shades of each color:
            -0x1000000,
            -0xffffa1,
            -0xffff79,
            -0xffff51,
            -0xffff29,
            -0xffff01,
            -0xffa100,
            -0xffa0a1,
            -0xffa079,
            -0xffa051,
            -0xffa029,
            -0xffa001,
            -0xff7900,
            -0xff78a1,
            -0xff7879,
            -0xff7851,
            -0xff7829,
            -0xff7801,
            -0xff5100,
            -0xff50a1,
            -0xff5079,
            -0xff5051,
            -0xff5029,
            -0xff5001,
            -0xff2900,
            -0xff28a1,
            -0xff2879,
            -0xff2851,
            -0xff2829,
            -0xff2801,
            -0xff0100,
            -0xff00a1,
            -0xff0079,
            -0xff0051,
            -0xff0029,
            -0xff0001,
            -0xa10000,
            -0xa0ffa1,
            -0xa0ff79,
            -0xa0ff51,
            -0xa0ff29,
            -0xa0ff01,
            -0xa0a100,
            -0xa0a0a1,
            -0xa0a079,
            -0xa0a051,
            -0xa0a029,
            -0xa0a001,
            -0xa07900,
            -0xa078a1,
            -0xa07879,
            -0xa07851,
            -0xa07829,
            -0xa07801,
            -0xa05100,
            -0xa050a1,
            -0xa05079,
            -0xa05051,
            -0xa05029,
            -0xa05001,
            -0xa02900,
            -0xa028a1,
            -0xa02879,
            -0xa02851,
            -0xa02829,
            -0xa02801,
            -0xa00100,
            -0xa000a1,
            -0xa00079,
            -0xa00051,
            -0xa00029,
            -0xa00001,
            -0x790000,
            -0x78ffa1,
            -0x78ff79,
            -0x78ff51,
            -0x78ff29,
            -0x78ff01,
            -0x78a100,
            -0x78a0a1,
            -0x78a079,
            -0x78a051,
            -0x78a029,
            -0x78a001,
            -0x787900,
            -0x7878a1,
            -0x787879,
            -0x787851,
            -0x787829,
            -0x787801,
            -0x785100,
            -0x7850a1,
            -0x785079,
            -0x785051,
            -0x785029,
            -0x785001,
            -0x782900,
            -0x7828a1,
            -0x782879,
            -0x782851,
            -0x782829,
            -0x782801,
            -0x780100,
            -0x7800a1,
            -0x780079,
            -0x780051,
            -0x780029,
            -0x780001,
            -0x510000,
            -0x50ffa1,
            -0x50ff79,
            -0x50ff51,
            -0x50ff29,
            -0x50ff01,
            -0x50a100,
            -0x50a0a1,
            -0x50a079,
            -0x50a051,
            -0x50a029,
            -0x50a001,
            -0x507900,
            -0x5078a1,
            -0x507879,
            -0x507851,
            -0x507829,
            -0x507801,
            -0x505100,
            -0x5050a1,
            -0x505079,
            -0x505051,
            -0x505029,
            -0x505001,
            -0x502900,
            -0x5028a1,
            -0x502879,
            -0x502851,
            -0x502829,
            -0x502801,
            -0x500100,
            -0x5000a1,
            -0x500079,
            -0x500051,
            -0x500029,
            -0x500001,
            -0x290000,
            -0x28ffa1,
            -0x28ff79,
            -0x28ff51,
            -0x28ff29,
            -0x28ff01,
            -0x28a100,
            -0x28a0a1,
            -0x28a079,
            -0x28a051,
            -0x28a029,
            -0x28a001,
            -0x287900,
            -0x2878a1,
            -0x287879,
            -0x287851,
            -0x287829,
            -0x287801,
            -0x285100,
            -0x2850a1,
            -0x285079,
            -0x285051,
            -0x285029,
            -0x285001,
            -0x282900,
            -0x2828a1,
            -0x282879,
            -0x282851,
            -0x282829,
            -0x282801,
            -0x280100,
            -0x2800a1,
            -0x280079,
            -0x280051,
            -0x280029,
            -0x280001,
            -0x10000,
            -0xffa1,
            -0xff79,
            -0xff51,
            -0xff29,
            -0xff01,
            -0xa100,
            -0xa0a1,
            -0xa079,
            -0xa051,
            -0xa029,
            -0xa001,
            -0x7900,
            -0x78a1,
            -0x7879,
            -0x7851,
            -0x7829,
            -0x7801,
            -0x5100,
            -0x50a1,
            -0x5079,
            -0x5051,
            -0x5029,
            -0x5001,
            -0x2900,
            -0x28a1,
            -0x2879,
            -0x2851,
            -0x2829,
            -0x2801,
            -0x100,
            -0xa1,
            -0x79,
            -0x51,
            -0x29,
            -0x1,  // 24 grey scale ramp:
            -0xf7f7f8,
            -0xededee,
            -0xe3e3e4,
            -0xd9d9da,
            -0xcfcfd0,
            -0xc5c5c6,
            -0xbbbbbc,
            -0xb1b1b2,
            -0xa7a7a8,
            -0x9d9d9e,
            -0x939394,
            -0x89898a,
            -0x7f7f80,
            -0x757576,
            -0x6b6b6c,
            -0x616162,
            -0x575758,
            -0x4d4d4e,
            -0x434344,
            -0x39393a,
            -0x2f2f30,
            -0x252526,
            -0x1b1b1c,
            -0x111112,  // COLOR_INDEX_DEFAULT_FOREGROUND, COLOR_INDEX_DEFAULT_BACKGROUND and COLOR_INDEX_DEFAULT_CURSOR:
            -0x1,
            -0x1000000,
            -0x1
        )
    }
}