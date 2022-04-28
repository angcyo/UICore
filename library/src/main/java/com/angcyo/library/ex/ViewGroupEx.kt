package com.angcyo.library.ex

import android.graphics.Rect
import android.view.*
import android.widget.RadioGroup
import androidx.annotation.LayoutRes
import kotlin.math.min

/**
 * Kotlin ViewGroup的扩展
 * Created by angcyo on 2017-07-26.
 */

/**
 * 计算child在parent中的位置坐标, 请确保child在parent中.
 * */
fun ViewGroup.getLocationInParent(child: View, location: Rect) {
    var x = 0
    var y = 0

    var view = child
    while (view.parent != this) {
        x += view.left
        y += view.top
        view = view.parent as View
    }

    x += view.left
    y += view.top

    location.set(x, y, x + child.measuredWidth, y + child.measuredHeight)
}

fun ViewGroup.findView(
    targetView: View /*判断需要结束的View*/,
    touchRawX: Float,
    touchRawY: Float,
    offsetTop: Int = 0,
    /*是否需要拦截View, 拦截后 立马返回. 通常用来拦截ViewGroup, 防止枚举目标ViewGroup*/
    intercept: (View, Rect) -> Boolean = { _, _ -> false },
    /*找到目标后, 是否需要跳过目标继续搜索*/
    jumpTarget: (View, Rect) -> Boolean = { _, _ -> false }
): View? {
    /**键盘的高度*/
    var touchView: View? = targetView
    val rect = Rect()

    for (i in childCount - 1 downTo 0) {
        val childAt = getChildAt(i)

        if (childAt.visibility != View.VISIBLE) {
            continue
        }

//        childAt.getWindowVisibleDisplayFrame(rect)
//        L.e("${this}:1 ->$i $rect")
        childAt.getGlobalVisibleRect(rect)
//        L.e("${this}:2 ->$i $rect")
//        L.e("call: ------------------end -> ")
        rect.offset(0, -offsetTop)

        //检查当前view, 是否在 touch坐标中
        fun check(view: View): View? {
            if (view.visibility == View.VISIBLE &&
                view.measuredHeight != 0 &&
                view.measuredWidth != 0 &&
                (view.left != view.right) &&
                (view.top != view.bottom) &&
                rect.contains(touchRawX.toInt(), touchRawY.toInt())
            ) {
                return view
            }
            return null
        }

        val checkView = check(childAt)

        //拦截处理
        if (checkView != null && intercept.invoke(childAt, rect)) {
            touchView = childAt
            break
        }

        if (childAt is ViewGroup && childAt.childCount > 0) {
            val resultView =
                childAt.findView(targetView, touchRawX, touchRawY, offsetTop, intercept, jumpTarget)
            if (resultView != null && resultView != targetView) {
                if (jumpTarget.invoke(resultView, rect)) {

                } else {
                    touchView = resultView
                    break
                }
            } else {
                if (checkView != null) {
                    if (jumpTarget.invoke(checkView, rect)) {

                    } else {
                        touchView = checkView
                        break
                    }
                }
            }
        } else {
            if (checkView != null) {
                if (jumpTarget.invoke(checkView, rect)) {

                } else {
                    touchView = checkView
                    break
                }
            }
        }
    }
    return touchView
}

//<editor-fold desc="child操作">

/**枚举所有child view
 * [recursively] 递归所有子view*/
fun ViewGroup.eachChild(recursively: Boolean = false, map: (index: Int, child: View) -> Unit) {
    for (index in 0 until childCount) {
        val childAt = getChildAt(index)
        map.invoke(index, childAt)
        if (recursively && childAt is ViewGroup) {
            childAt.eachChild(recursively, map)
        }
    }
}

fun ViewGroup.forEach(recursively: Boolean = false, map: (index: Int, child: View) -> Unit) {
    eachChild(recursively, map)
}

fun ViewGroup.each(recursively: Boolean = false, map: (child: View) -> Unit) {
    eachChild(recursively) { _, child ->
        map.invoke(child)
    }
}

/**获取指定位置[index]的[child], 如果有.*/
fun ViewGroup.getChildOrNull(index: Int): View? {
    return if (index in 0 until childCount) {
        getChildAt(index)
    } else {
        null
    }
}

fun ViewGroup.eachChildVisibility(map: (index: Int, child: View) -> Unit) {
    for (index in 0 until childCount) {
        val childAt = getChildAt(index)
        if (childAt.visibility != View.GONE) {
            map.invoke(index, childAt)
        }
    }
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = true): View {
    if (layoutId == -1) {
        return this
    }
    val rootView = LayoutInflater.from(context).inflate(layoutId, this, false)
    if (attachToRoot) {
        addView(rootView)
    }
    return rootView
}

fun ViewGroup.append(@LayoutRes layoutId: Int, attachToRoot: Boolean = true): View {
    if (layoutId == -1) {
        return this
    }
    val rootView = LayoutInflater.from(context).inflate(layoutId, this, false)
    if (attachToRoot) {
        addView(rootView)
    }
    return rootView
}

fun ViewGroup.append(@LayoutRes layoutId: Int, action: View.() -> Unit = {}): View {
    val view = inflate(layoutId, false)
    return append(view, action)
}

fun <T : View> ViewGroup.append(view: T?, action: T.() -> Unit = {}): View {
    if (view != null) {
        addView(view)
        view.action()
    }
    return view ?: this
}

/**清空之前所有视图, 使用[layoutId]重新渲染*/
fun ViewGroup.replace(@LayoutRes layoutId: Int, attachToRoot: Boolean = true): View {
    if (childCount > 0 && layoutId != -1) {
        removeAllViews()
    }
    return inflate(layoutId, attachToRoot)
}

/**将之前所有视图, 添加到新的[viewGroup]*/
fun ViewGroup.replace(viewGroup: ViewGroup): ViewGroup {
    val childList = mutableListOf<View>()

    for (i in 0 until childCount) {
        childList.add(getChildAt(i))
    }

    if (childCount > 0) {
        removeAllViews()
    }

    childList.forEach {
        viewGroup.addView(it)
    }

    addView(viewGroup, viewGroup.layoutParams ?: ViewGroup.LayoutParams(-1, -1))
    return viewGroup
}

/**将子View的数量, 重置到指定的数量*/
fun ViewGroup.resetChildCount(
    newSize: Int,
    createOrInitView: (childIndex: Int, childView: View?) -> View
) {
    val oldSize = childCount
    val count = newSize - oldSize
    if (count > 0) {
        //需要补充子View
        for (i in 0 until count) {
            addView(createOrInitView.invoke(oldSize + i, null))
        }
    } else if (count < 0) {
        //需要移除子View
        for (i in 0 until count.abs()) {
            removeViewAt(oldSize - 1 - i)
        }
    }

    //初始化
    for (i in 0 until min(oldSize, newSize)) {
        createOrInitView.invoke(i, getChildAt(i))
    }
}

/**调整child的顺序
 * 按照传进来的顺序重新排列*/
fun ViewGroup.adjustOrder(vararg childOrder: View?) {
    childOrder.forEachIndexed { index, view ->
        val child = getChildOrNull(index)
        if (child != view) {
            //当前位置的child, 需要被替换
            removeView(view)
            addView(view, index)
        }
    }
}

fun ViewGroup.adjustOrder(vararg id: Int) {
    val array = Array<View?>(id.size) { null }
    id.forEachIndexed { index, i ->
        array[index] = findViewById(i)
    }
    adjustOrder(*array)
}

//</editor-fold desc="child操作">

/**选中的[View]*/
fun <T : View> RadioGroup.checkView(): T? {
    return findViewById(checkedRadioButtonId)
}

