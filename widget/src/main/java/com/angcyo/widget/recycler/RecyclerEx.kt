package com.angcyo.widget.recycler

import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.OverScroller
import androidx.core.widget.ScrollerCompat
import androidx.recyclerview.widget.*
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslItemDecoration
import com.angcyo.dsladapter.HoverItemDecoration
import com.angcyo.dsladapter.dslSpanSizeLookup
import com.angcyo.library.utils.getMember
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="DslAdapter相关">

/**[DslAdapter]必备的组件*/
fun RecyclerView.initDsl() {
    addItemDecoration(DslItemDecoration())
    HoverItemDecoration().attachToRecyclerView(this)
}

/**快速初始化[DslAdapter]*/
fun RecyclerView.initDslAdapter(action: DslAdapter.() -> Unit) {
    initDsl()
    resetLayoutManager("v")
    adapter = DslAdapter().apply {
        this.action()
    }
}

fun RecyclerView.dslAdapter(
    append: Boolean = false, //当已经是adapter时, 是否追加item. 需要先关闭 new
    new: Boolean = true, //始终创建新的adapter, 为true时, 则append无效
    init: DslAdapter.() -> Unit
): DslAdapter {

    var dslAdapter: DslAdapter? = null

    fun newAdapter() {
        dslAdapter = DslAdapter()
        adapter = dslAdapter

        dslAdapter!!.init()
    }

    if (new) {
        newAdapter()
    } else {
        if (adapter is DslAdapter) {
            dslAdapter = adapter as DslAdapter

            if (!append) {
                dslAdapter!!.clearItems()
            }

            dslAdapter!!.init()
        } else {
            newAdapter()
        }
    }

    return dslAdapter!!
}

//</editor-fold desc="DslAdapter相关">

//<editor-fold desc="基础">

/** 通过[V] [H] [GV2] [GH3] [SV2] [SV3] 方式, 设置 [LayoutManager] */
fun RecyclerView.resetLayoutManager(match: String) {
    var layoutManager: RecyclerView.LayoutManager? = null
    var spanCount = 1
    var orientation = LinearLayout.VERTICAL

    if (TextUtils.isEmpty(match) || "V".equals(match, ignoreCase = true)) {
        layoutManager = LinearLayoutManagerWrap(context, LinearLayoutManager.VERTICAL, false)
    } else {
        //线性布局管理器
        if ("H".equals(match, ignoreCase = true)) {
            layoutManager =
                LinearLayoutManagerWrap(context, LinearLayoutManager.HORIZONTAL, false)
        } else { //读取其他配置信息(数量和方向)
            val type = match.substring(0, 1)
            if (match.length >= 3) {
                try {
                    spanCount = Integer.valueOf(match.substring(2)) //数量
                } catch (e: Exception) {
                }
            }
            if (match.length >= 2) {
                if ("H".equals(match.substring(1, 2), ignoreCase = true)) {
                    orientation = StaggeredGridLayoutManager.HORIZONTAL //方向
                }
            }
            //交错布局管理器
            if ("S".equals(type, ignoreCase = true)) {
                layoutManager =
                    StaggeredGridLayoutManagerWrap(
                        spanCount,
                        orientation
                    )
            } else if ("G".equals(type, ignoreCase = true)) {
                layoutManager =
                    GridLayoutManagerWrap(
                        context,
                        spanCount,
                        orientation,
                        false
                    )
            }
        }
    }

    if (layoutManager is GridLayoutManager) {
        val gridLayoutManager = layoutManager
        gridLayoutManager.dslSpanSizeLookup(this)
    } else if (layoutManager is LinearLayoutManager) {
        layoutManager.recycleChildrenOnDetach = true
    }

    this.layoutManager = layoutManager
}

/**
 * 获取[RecyclerView] [Fling] 时的速率
 * */
fun RecyclerView?.getLastVelocity(): Float {
    var currVelocity = 0f
    try {
        val mViewFlinger = this.getMember(RecyclerView::class.java, "mViewFlinger")
        var mScroller = mViewFlinger.getMember("mScroller")
        if (mScroller == null) {
            mScroller = mViewFlinger.getMember("mOverScroller")
        }
        when (mScroller) {
            is OverScroller -> {
                currVelocity = mScroller.currVelocity
            }
            is ScrollerCompat -> {
                currVelocity = mScroller.currVelocity
            }
            else -> {
                //throw new IllegalArgumentException("未兼容的mScroller类型:" + mScroller.getClass().getSimpleName());
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return currVelocity
}


/**
 * 取消RecyclerView的默认动画
 * */
public fun RecyclerView.noItemAnim() {
    itemAnimator = null
}

/**
 * 取消默认的change动画
 * */
public fun RecyclerView.noItemChangeAnim() {
    if (itemAnimator == null) {
        itemAnimator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
    } else if (itemAnimator is SimpleItemAnimator) {
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
    }
}

//</editor-fold desc="基础">

//<editor-fold desc="ViewHolder相关">

/**获取[RecyclerView]界面上存在指定位置[index]的[DslViewHolder], 负数表示倒数开始的index*/
operator fun RecyclerView.get(index: Int): DslViewHolder? {
    val child: View?
    child = if (index >= 0) {
        //正向取child
        if (index < childCount) {
            getChildAt(index)
        } else {
            null
        }
    } else {
        //反向取child
        val i = childCount + index
        if (i in 0 until childCount) {
            getChildAt(i)
        } else {
            null
        }
    }
    return child?.run { getChildViewHolder(this) as? DslViewHolder }
}

/**获取[RecyclerView]界面上存在的所有[DslViewHolder]*/
fun RecyclerView.allViewHolder(): List<DslViewHolder> {
    val result = mutableListOf<DslViewHolder>()
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        (getChildViewHolder(child) as? DslViewHolder)?.run { result.add(this) }
    }
    return result
}

//</editor-fold desc="ViewHolder相关">