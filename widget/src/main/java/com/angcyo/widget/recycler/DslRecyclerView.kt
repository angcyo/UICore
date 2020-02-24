package com.angcyo.widget.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslRecyclerView : RecyclerView {

    /** 通过[V] [H] [GV2] [GH3] [SV2] [SV3] 方式, 设置 [LayoutManager] */
    var layout: String? = null
        set(value) {
            field = value
            value?.run { resetLayoutManager(this) }
        }

    val scrollHelper = ScrollHelper()

    constructor(context: Context) : super(context) {
        initAttribute(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslRecyclerView)
        typedArray.getString(R.styleable.DslRecyclerView_r_layout_manager)?.let {
            layout = it
        }
        typedArray.recycle()

        scrollHelper.attach(this)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (layoutManager == null) {
            //layout属性的支持
            layout?.run { layout = this }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    /**
     * 锁定滚动到目标位置
     * [position] 目标位置, 负数表示倒数第几个
     * [duration] 锁定多少毫秒
     * [config] 自定义配置
     * */
    fun lockScroll(
        position: Int = NO_POSITION,
        duration: Long = -1,
        config: ScrollHelper.LockDrawListener .() -> Unit = {}
    ) {
        scrollHelper.lockPositionByDraw {
            lockPosition = position
            lockDuration = duration
            config()
        }
    }
}