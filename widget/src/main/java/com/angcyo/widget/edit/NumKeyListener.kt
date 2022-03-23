package com.angcyo.widget.edit

import android.text.InputType
import android.text.method.NumberKeyListener

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/22
 */
class NumKeyListener(
    val chars: CharArray = charArrayOf(
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        ' '
    ),
    val type: Int = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
) : NumberKeyListener() {

    override fun getInputType(): Int {
        return type
    }

    override fun getAcceptedChars(): CharArray {
        return chars
    }

}