package com.angcyo.widget.edit

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import com.angcyo.widget.R


/**
 * 支持模板样式的输入框
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/22
 */
open class PatternEditDelegate(editText: EditText) : BaseEditDelegate(editText) {

    var patternEnable = false
        set(value) {
            field = value
            checkPattern()
        }

    /** 分割模版 */
    var patternString: String? = "###,####,####"
        set(value) {
            field = value
            initPattern()
        }

    /** 切割模版的分割字符, 支持多个字符 */
    val splitList = mutableListOf(' ', ',')

    /** 需要插入的分割字符 */
    var separatorChar = ' '
        set(value) {
            field = value
            checkPattern()
        }

    val _delayCheck = Runnable { checkPattern() }

    // 需要分割的位置
    val _separatorPosition = mutableListOf<Int>()

    // ##,### 之后按照每###添加一个分隔符
    // ##,###,#### 之后按照每####添加一个分隔符
    var _patternIncrement: Int = 0

    override fun initAttribute(context: Context, attrs: AttributeSet?) {
        super.initAttribute(context, attrs)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PatternEditDelegate)
        patternEnable =
            typedArray.getBoolean(R.styleable.PatternEditDelegate_r_pattern_enable, patternEnable)
        patternString = typedArray.getString(R.styleable.PatternEditDelegate_r_pattern_string)
        typedArray.recycle()

        initPattern()
    }

    /**初始化*/
    fun initPattern() {
        _separatorPosition.clear()
        _patternIncrement = 0
        val pattern = patternString
        if (!pattern.isNullOrEmpty()) {
            for (i in pattern.indices) {
                _patternIncrement++
                if (splitList.contains(pattern[i])) {
                    _patternIncrement = 0
                    _separatorPosition.add(i)
                }
            }
        }
        checkPattern()
    }

    /**入口1*/
    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        checkPattern()
    }

    /**入口2*/
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (patternEnable) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //解决长按删除键无法删除的BUG
                editText.removeCallbacks(_delayCheck)
                editText.postDelayed(
                    _delayCheck,
                    editText.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                )
            } else {
                checkPattern()
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    /**检查字符模板, 核心方法*/
    open fun checkPattern() {
        if (!patternEnable) {
            return
        }
        val oldText = editText.text.toString()
        var isChanged = false
        val rawText = StringBuffer(editText.text)
        var selectionStart = editText.selectionStart
        var i = 0
        _lastKeyPosition = -1
        while (i < rawText.length) {
            val charAt = rawText[i]
            if (charAt == separatorChar) {
                rawText.delete(i, i + 1)
                isChanged = true
                if (i < selectionStart) {
                    selectionStart--
                }
                i--
            } else if (isKeyPosition(i)) {
                rawText.insert(i, separatorChar)
                isChanged = true
                if (i < selectionStart) {
                    selectionStart++
                }
            }
            i++
        }
        if (oldText != rawText.toString()) {
            editText.setText(rawText)
        }
        if (isChanged) {
            editText.setSelection(kotlin.math.min(rawText.length, selectionStart))
        }
    }

    //上一次的关键位置
    var _lastKeyPosition = 0

    /**是否是模板的关键位置*/
    open fun isKeyPosition(position: Int): Boolean {
        var result = false
        if (_separatorPosition.contains(position)) {
            result = true
        }

        if (_lastKeyPosition >= 0 &&
            _patternIncrement > 0 &&
            position - _lastKeyPosition == _patternIncrement + 1
        ) {
            result = true
        }

        if (result) {
            _lastKeyPosition = position
        }

        return result
    }

}