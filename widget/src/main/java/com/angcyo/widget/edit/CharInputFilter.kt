package com.angcyo.widget.edit

import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import java.util.*
import kotlin.math.max

/**
 * Created by angcyo on 2018-08-10.
 * Email:angcyo@126.com
 */
class CharInputFilter : InputFilter {

    var callbacks: MutableList<OnFilterCallback>? = null
    /**
     * 默认允许所有输入
     */
    private var filterModel = 0xFF
    /**
     * 限制输入的最大字符数, 小于0不限制
     */
    private var maxInputLength = -1

    constructor()
    constructor(filterModel: Int, allowChar: CharArray?) : this(filterModel, -1, allowChar)

    /**
     * @param allowChar 额外允许输入的 char 字符数组
     */
    constructor(filterModel: Int, maxInputLength: Int = -1, allowChar: CharArray? = null) {
        this.filterModel = filterModel
        this.maxInputLength = maxInputLength
        if (allowChar != null && allowChar.isNotEmpty()) {
            addFilterCallback(object : OnFilterCallback {
                override fun onFilterAllow(
                    source: CharSequence?,
                    c: Char,
                    cIndex: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): Boolean {
                    return Arrays.binarySearch(allowChar, c) >= 0
                }
            })
        }
    }

    fun setFilterModel(filterModel: Int) {
        this.filterModel = filterModel
    }

    fun setMaxInputLength(maxInputLength: Int) {
        this.maxInputLength = maxInputLength
    }

    /**
     * 将 dest 字符串中[dstart, dend] 位置对应的字符串, 替换成 source 字符串中 [start, end] 位置对应的字符串.
     */
    override fun filter(
        source: CharSequence,  //本次需要更新的字符串, (可以理解为输入法输入的字符,比如:我是文本)
        start: Int,  //取 source 字符串的开始位置,通常是0
        end: Int,  //取 source 字符串的结束位置,通常是source.length()
        dest: Spanned,  //原始字符串
        dstart: Int,  //原始字符串开始的位置,
        dend: Int //原始字符串结束的位置, 这种情况会在你已经选中了很多个字符, 然后用输入法输入字符的情况下.
    ): CharSequence? {
        if (start == 0 && end == 0 && dstart != dend) { //删除字符
            return null
        }
        //此次操作后, 原来的字符修改后的新值
        val newDest = StringBuilder()
        newDest.append(dest.subSequence(0, dstart))
        newDest.append(dest.subSequence(dend, dest.length))
        val length = newDest.length
        //此次操作后, 原来的字符数量
        //int length = dest.length() - (dend - dstart);
        if (maxInputLength > 0) {
            if (length == maxInputLength) {
                return ""
            }
        }
        val modification = SpannableStringBuilder()
        for (i in start until end) {
            var c = source[i]
            var append = false
            if (filterModel and MODEL_CHINESE == MODEL_CHINESE) {
                append = isChinese(c) || append
            }
            if (filterModel and MODEL_CHAR_LETTER == MODEL_CHAR_LETTER) {
                append = isCharLetter(c) || append
            }
            if (filterModel and MODEL_NUMBER == MODEL_NUMBER) {
                append = isNumber(c) || append
            }
            if (filterModel and MODEL_ASCII_CHAR == MODEL_ASCII_CHAR) {
                append = isAsciiChar(c) || append
            }
            if (filterModel and MODEL_SPACE == MODEL_SPACE) {
                append = isAsciiSpace(c) || append
            }
            if (filterModel and MODEL_NOT_EMOJI == MODEL_NOT_EMOJI) {
                append = !EmojiTools.isEmojiCharacter(c.toInt()) || append
            }
            if (filterModel and MODEL_ID_CARD == MODEL_ID_CARD) {
                if (length == 14 || length == 17) {
                    val oldString = newDest.toString()
                    //本次输入的是x X 乘号
                    append = !oldString.contains("x") &&
                            !oldString.contains("X") &&
                            (c == 'x' || c == 'X' || c == '×') || append
                    if (c == '×') {
                        c = 'X'
                    }
                }
            }
            if (callbacks != null && filterModel and MODEL_CALLBACK == MODEL_CALLBACK) {
                for (callback in callbacks!!) {
                    append = callback.onFilterAllow(source, c, i, dest, dstart, dend) || append
                }
            }
            if (append) {
                modification.append(c)
            }
        }
        if (maxInputLength > 0) {
            val newLength = length + modification.length
            if (newLength > maxInputLength) { //越界
                modification.delete(
                    max(0, maxInputLength - length),
                    modification.length
                )
            }
        }
        return if (TextUtils.equals(source, modification)) {
            //如果输入的字符, 和过滤后的字符无变化, 交给系统处理.
            // 否则在输入法联想输入的时候会出现错乱输入的BUG
            null
        } else modification
        //返回修改后, 允许输入的字符串. 返回null, 由系统处理.
    }

    fun addFilterCallback(callback: OnFilterCallback) {
        filterModel = filterModel or MODEL_CALLBACK
        if (callbacks == null) {
            callbacks = ArrayList()
        }
        if (!callbacks!!.contains(callback)) {
            callbacks!!.add(callback)
        }
    }

    interface OnFilterCallback {
        /**
         * 是否允许输入字符c
         *
         * @param c 当前需要过滤的char
         * @return 返回true, 过滤.否则允许输入
         * @see InputFilter.filter
         */
        fun onFilterAllow(
            source: CharSequence?,
            c: Char,
            cIndex: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): Boolean
    }

    /**
     * https://github.com/itgoyo/EmojiUtils
     */
    object EmojiTools {
        fun containsEmoji(str: String): Boolean {
            if (TextUtils.isEmpty(str)) {
                return false
            }
            for (i in str.indices) {
                val cp = str.codePointAt(i)
                if (isEmojiCharacter(cp)) {
                    return true
                }
            }
            return false
        }

        /**
        1F30D - 1F567
        1F600 - 1F636
        24C2 - 1F251
        1F680 - 1F6C0
        2702 - 27B0
        1F601 - 1F64F
         */
        fun isEmojiCharacter(first: Int): Boolean {
            return (!(first == 0x0 ||
                    first == 0x9 ||
                    first == 0xA ||
                    first == 0xD ||
                    first in 0x20..0xD7FF ||
                    first in 0xE000..0xFFFD ||
                    first >= 0x10000) ||
                    first == 0xa9 ||
                    first == 0xae ||
                    first == 0x2122 ||
                    first == 0x3030 ||
                    first in 0x25b6..0x27bf ||
                    first == 0x2328 ||
                    first in 0x23e9..0x23fa ||
                    first in 0x1F000..0x1FFFF ||
                    first in 0x2702..0x27B0 ||
                    first in 0x1F601..0x1F64F)
        }

        fun filterEmoji(str: String): String {
            if (!containsEmoji(str)) {
                return str
            } else {
            }
            var buf: StringBuilder? = null
            val len = str.length
            for (i in 0 until len) {
                val codePoint = str[i]
                if (!isEmojiCharacter(codePoint.toInt())) {
                    if (buf == null) {
                        buf = StringBuilder(str.length)
                    }
                    buf.append(codePoint)
                } else {
                }
            }
            return buf?.toString() ?: ""
        }
    }

    companion object {
        /**
         * 允许中文输入
         */
        const val MODEL_CHINESE = 1
        /**
         * 允许输入大小写字母
         */
        const val MODEL_CHAR_LETTER = 2
        /**
         * 允许输入数字
         */
        const val MODEL_NUMBER = 4
        /**
         * 允许输入Ascii码表的[33-126]的字符
         */
        const val MODEL_ASCII_CHAR = 8
        /**
         * callback过滤模式
         */
        const val MODEL_CALLBACK = 16
        /**
         * 身份证号码
         */
        const val MODEL_ID_CARD = 32
        /**
         * 允许输入空格 ASCII 码 32
         */
        const val MODEL_SPACE = 64
        /**
         * 允许非 emoji 字符输入, 即过滤emoji
         */
        const val MODEL_NOT_EMOJI = 128

        /**
         * 是否是中文
         */
        fun isChinese(c: Char): Boolean {
            return c.toInt() in 0x4E00..0x9FA5
            //        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
//        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
//            return true;
//        }
//        return false;
        }

        /**
         * 是否是大小写字母
         */
        fun isCharLetter(c: Char): Boolean { // Allow [a-zA-Z]
            return if (c in 'a'..'z') {
                true
            } else c in 'A'..'Z'
        }

        fun isNumber(c: Char): Boolean {
            return c in '0'..'9'
        }

        fun isAsciiChar(c: Char): Boolean {
            return c.toInt() in 33..126
        }

        fun isAsciiSpace(c: Char): Boolean {
            return 32 == c.toInt()
        }
    }
}