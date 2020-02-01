package com.angcyo.widget.pager

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.angcyo.library.utils.getMember
import com.angcyo.widget.base.getCharLength
import com.angcyo.widget.base.getCharLengthFilter
import com.angcyo.widget.base.hasCharLengthFilter
import com.angcyo.widget.span.span

/**
 * 类的描述：1/6 这样的ViewPager 指示器
 * 创建人员：Robi
 * 创建时间：2016/12/17 10:58 ~
 */
class TextIndicator : AppCompatTextView, OnPageChangeListener {

    //上限
    private var maxCount = 0
    private var currentCount = 0

    var autoHide = true

    //兼容控件
    private var editText: EditText? = null
    private var viewPager: ViewPager? = null

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            if (editText != null) {
                if (editText!!.hasCharLengthFilter()) {
                    showIndicator(editText!!.getCharLength() / 2, maxCount)
                } else {
                    setCurrentCount(s.length)
                }
            } else {
                setCurrentCount(s.length)
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode && TextUtils.isEmpty(text)) {
            showIndicator(1, 10)
        }
    }

    /**安装在[ViewPager]*/
    fun setupViewPager(viewPager: ViewPager?) {
        this.viewPager?.removeOnPageChangeListener(this)
        this.viewPager = viewPager
        this.viewPager?.addOnPageChangeListener(this)

        showInViewPager()
    }

    /**安装在[EditText], [maxLength]设置最大上限*/
    fun setupEditText(editText: EditText?, maxLength: Int = -1) {
        if (editText == null) {
            if (autoHide) {
                visibility = View.INVISIBLE
            }
            return
        }

        if (autoHide) {
            visibility = View.VISIBLE
        }

        var max = 0

        if (maxLength > 0) {
            max = maxLength
        } else {
            //自动智能获取上限
            val charLengthFilter = editText.getCharLengthFilter()
            if (charLengthFilter != null) {
                max = charLengthFilter.maxLen
            } else {
                val filters = editText.filters
                for (i in filters.indices) {
                    val filter = filters[i]
                    if (filter is LengthFilter) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            max = filter.max
                        } else {
                            val mMax: Any? = filter.getMember(LengthFilter::class.java, "mMax")
                            if (mMax != null) {
                                max = mMax as Int
                            }
                        }
                        break
                    }
                }
            }
        }
        showIndicator(max, editText)
    }

    fun setCurrentCount(currentCount: Int): TextIndicator {
        this.currentCount = currentCount
        updateText()
        return this
    }

    /**更新显示文本*/
    private fun updateText() {
        text = span {
            append("$currentCount") {
                if (currentCount > maxCount) {

                    foregroundColor = Color.RED
                }
            }
            append("/$maxCount")
        }
    }

    fun setMaxCount(maxCount: Int): TextIndicator {
        this.maxCount = maxCount
        updateText()
        return this
    }

    /**设置当前显示状态*/
    fun showIndicator(currentCount: Int, maxCount: Int): TextIndicator {
        this.maxCount = maxCount
        this.currentCount = currentCount
        updateText()
        return this
    }

    fun showIndicator(maxCount: Int, editText: EditText): TextIndicator {
        showIndicator(if (TextUtils.isEmpty(editText.text)) 0 else editText.length(), maxCount)
        editText.removeTextChangedListener(textWatcher)
        editText.addTextChangedListener(textWatcher)
        return this
    }

    private fun showInViewPager() {
        val adapter = viewPager?.adapter
        if (adapter == null) {
            if (autoHide) {
                visibility = View.INVISIBLE
            }
        } else {
            visibility = View.VISIBLE

            showIndicator((viewPager?.currentItem ?: 0) + 1, adapter.count)
        }
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
    }

    override fun onPageSelected(position: Int) {
        showInViewPager()
    }

    override fun onPageScrollStateChanged(state: Int) {}
}