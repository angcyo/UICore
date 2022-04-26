package com.angcyo.widget.base

import android.app.Activity
import android.graphics.Rect
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.isVisible
import com.angcyo.library.ex.size
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.layout.RSoftInputLayout
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

/**返回当软键盘弹出时, 布局向上偏移了多少距离*/
fun View.getLayoutOffsetTopWidthSoftInput(): Int {
    val rect = Rect()
    var offsetTop = 0

    try {
        val activity = this.context as Activity
        val softInputMode = activity.window.attributes.softInputMode
        if (softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
            val keyboardHeight = RSoftInputLayout.getSoftKeyboardHeight(this)

            /**在ADJUST_PAN模式下, 键盘弹出时, 坐标需要进行偏移*/
            if (keyboardHeight > 0) {
                //return targetView
                val findFocus = this.findFocus()
                if (findFocus is EditText) {
                    findFocus.getWindowVisibleDisplayFrame(rect)
                    offsetTop = findFocus.bottom - rect.bottom
                }
            }
        }

    } catch (e: Exception) {
    }
    return offsetTop
}


/**获取touch坐标对应的RecyclerView, 如果没有则null*/
fun ViewGroup.getTouchOnRecyclerView(
    touchRawX: Float,
    touchRawY: Float
): androidx.recyclerview.widget.RecyclerView? {
    return findRecyclerView(touchRawX, touchRawY)
}

fun ViewGroup.getTouchOnRecyclerView(event: MotionEvent): androidx.recyclerview.widget.RecyclerView? {
    return findRecyclerView(event.rawX, event.rawY)
}

/**
 * 根据touch坐标, 返回touch的View
 */
fun ViewGroup.findView(
    event: MotionEvent,
    intercept: (View, Rect) -> Boolean = { _, _ -> false },
    jumpTarget: (View, Rect) -> Boolean = { _, _ -> false }
): View? {
    return findView(
        this,
        event.rawX,
        event.rawY,
        getLayoutOffsetTopWidthSoftInput(),
        intercept,
        jumpTarget
    )
}

fun ViewGroup.findView(
    touchRawX: Float,
    touchRawY: Float,
    intercept: (View, Rect) -> Boolean = { _, _ -> false },
    jumpTarget: (View, Rect) -> Boolean = { _, _ -> false }
): View? {
    return findView(
        this,
        touchRawX,
        touchRawY,
        getLayoutOffsetTopWidthSoftInput(),
        intercept,
        jumpTarget
    )
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

fun ViewGroup.findRecyclerView(
    touchRawX: Float,
    touchRawY: Float
): androidx.recyclerview.widget.RecyclerView? {
    /**键盘的高度*/
    var touchView: androidx.recyclerview.widget.RecyclerView? = null

    val findView = findView(touchRawX, touchRawY,
        { view, _ ->
            view is androidx.recyclerview.widget.RecyclerView
        }, { view, _ ->
            view !is androidx.recyclerview.widget.RecyclerView
        })

    if (findView is androidx.recyclerview.widget.RecyclerView) {
        touchView = findView
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

fun <T> ViewGroup.resetChild(
    list: List<T>?,
    layoutId: Int,
    init: (itemView: View, item: T, itemIndex: Int) -> Unit = { _, _, _ -> }
) {
    resetChild(list.size(), layoutId) { itemView, itemIndex ->
        val item = list!!.get(itemIndex)
        init(itemView, item, itemIndex)
    }
}

fun ViewGroup.resetChild(
    size: Int,
    layoutId: Int,
    init: (itemView: View, itemIndex: Int) -> Unit = { _, _ -> }
) {
    //如果布局id不一样, 说明child不一样, 需要remove
    for (index in childCount - 1 downTo 0) {
        val tag = getChildAt(index).getTag(R.id.tag)
        if (tag is Int) {
            if (tag != layoutId) {
                removeViewAt(index)
            }
        }
    }

    resetChildCount(size) { childIndex, childView ->
        if (childView == null) {
            val itemView = LayoutInflater.from(context).inflate(layoutId, this, false)
            itemView.setTag(R.id.tag, layoutId)
            itemView
        } else {
            childView
        }
    }

    for (i in 0 until size) {
        init(getChildAt(i), i)
    }
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

//<editor-fold desc="Dsl吸附">

/**从[View]中, 获取挂载的[DslViewHolder].如果没有, 则使用本身创建一个, 并设置给tag*/
fun View.dslViewHolder(): DslViewHolder {
    return this.run {
        var _tag = getTag(R.id.lib_tag_dsl_view_holder)
        if (_tag is DslViewHolder) {
            _tag
        } else {
            _tag = tag
            if (_tag is DslViewHolder) {
                _tag
            } else {
                DslViewHolder(this).apply {
                    setDslViewHolder(this)
                }
            }
        }
    }
}

/**获取挂载的[DslViewHolder]*/
fun View?.tagDslViewHolder(): DslViewHolder? {
    return this?.run {
        var _tag = getTag(R.id.lib_tag_dsl_view_holder)
        if (_tag is DslViewHolder) {
            _tag
        } else {
            _tag = tag
            if (_tag is DslViewHolder) {
                _tag
            } else {
                null
            }
        }
    }
}

/**获取挂载的[DslAdapterItem]*/
fun View?.tagDslAdapterItem(): DslAdapterItem? {
    return this?.run {
        val tag = getTag(R.id.lib_tag_dsl_adapter_item)
        if (tag is DslAdapterItem) {
            tag
        } else {
            null
        }
    }
}

/**设置挂载[DslViewHolder]*/
fun View?.setDslViewHolder(dslViewHolder: DslViewHolder?) {
    this?.setTag(R.id.lib_tag_dsl_view_holder, dslViewHolder)
}

/**设置挂载[DslAdapterItem]*/
fun View?.setDslAdapterItem(dslAdapterItem: DslAdapterItem?) {
    this?.setTag(R.id.lib_tag_dsl_adapter_item, dslAdapterItem)
}

fun View?.setDslAdapterItemDecoration(dslAdapterItem: DslAdapterItem?) {
    this?.setTag(
        R.id.lib_tag_dsl_item_decoration,
        "${dslAdapterItem?.itemTag ?: dslAdapterItem?.hashCode()}"
    )
}

//</editor-fold desc="Dsl吸附">

//</editor-fold desc="DslAdapterItem操作">

fun ViewGroup.appendDslItem(
    items: List<DslAdapterItem>,
    index: Int = -1,
    payloads: List<Any> = emptyList()
) {
    var newIndex = index
    items.forEach {
        appendDslItem(it, newIndex, payloads)
        if (newIndex >= 0) {
            newIndex++
        }
    }
}

fun ViewGroup.appendDslItem(
    dslAdapterItem: DslAdapterItem,
    index: Int = -1,
    payloads: List<Any> = emptyList()
): DslViewHolder {
    return addDslItem(dslAdapterItem, index, payloads)
}

fun ViewGroup.addDslItem(
    dslAdapterItem: DslAdapterItem,
    index: Int = -1,
    payloads: List<Any> = emptyList()
): DslViewHolder {
    setOnHierarchyChangeListener(DslHierarchyChangeListenerWrap())
    val visible = !dslAdapterItem.itemHidden

    val itemView = inflate(dslAdapterItem.itemLayoutId, false)
    val dslViewHolder = DslViewHolder(itemView)
    itemView.tag = dslViewHolder

    itemView.setDslViewHolder(dslViewHolder)
    itemView.setDslAdapterItem(dslAdapterItem)

    var itemIndex = if (index < 0) childCount else index

    itemView.visible(visible)
    dslAdapterItem.itemBind(dslViewHolder, itemIndex, dslAdapterItem, payloads)

    //头分割线的支持
    if (this is LinearLayout) {
        if (this.orientation == LinearLayout.VERTICAL) {
            if (dslAdapterItem.itemTopInsert > 0) {
                addView(
                    View(context).apply {
                        setDslAdapterItemDecoration(dslAdapterItem)
                        visible(visible)
                        setBackgroundColor(dslAdapterItem.itemDecorationColor)
                    },
                    itemIndex++,
                    LinearLayout.LayoutParams(-1, dslAdapterItem.itemTopInsert).apply {
                        leftMargin = dslAdapterItem.itemLeftOffset
                        rightMargin = dslAdapterItem.itemRightOffset
                    })
            }
        } else {
            if (dslAdapterItem.itemLeftInsert > 0) {
                addView(
                    View(context).apply {
                        setDslAdapterItemDecoration(dslAdapterItem)
                        visible(visible)
                        setBackgroundColor(dslAdapterItem.itemDecorationColor)
                    },
                    itemIndex++,
                    LinearLayout.LayoutParams(dslAdapterItem.itemTopInsert, -1).apply {
                        topMargin = dslAdapterItem.itemTopOffset
                        bottomMargin = dslAdapterItem.itemBottomOffset
                    })
            }
        }
    }
    addView(itemView, itemIndex++)
    //尾分割线的支持
    if (this is LinearLayout) {
        if (this.orientation == LinearLayout.VERTICAL) {
            if (dslAdapterItem.itemBottomInsert > 0) {
                addView(
                    View(context).apply {
                        setDslAdapterItemDecoration(dslAdapterItem)
                        visible(visible)
                        setBackgroundColor(dslAdapterItem.itemDecorationColor)
                    },
                    itemIndex,
                    LinearLayout.LayoutParams(-1, dslAdapterItem.itemBottomInsert).apply {
                        leftMargin = dslAdapterItem.itemLeftOffset
                        rightMargin = dslAdapterItem.itemRightOffset
                    })
            }
        } else {
            if (dslAdapterItem.itemRightInsert > 0) {
                addView(
                    View(context).apply {
                        setDslAdapterItemDecoration(dslAdapterItem)
                        visible(visible)
                        setBackgroundColor(dslAdapterItem.itemDecorationColor)
                    },
                    itemIndex,
                    LinearLayout.LayoutParams(dslAdapterItem.itemRightInsert, -1).apply {
                        topMargin = dslAdapterItem.itemTopOffset
                        bottomMargin = dslAdapterItem.itemBottomOffset
                    })
            }
        }
    }
    return dslViewHolder
}

fun ViewGroup.resetDslItem(item: DslAdapterItem) {
    resetDslItem(listOf(item))
}

fun ViewGroup.resetDslItem(items: List<DslAdapterItem>) {
    val childSize = childCount
    val itemSize = items.size

    //需要替换的child索引
    val replaceIndexList = mutableListOf<Int>()

    //更新已存在的Item
    for (i in 0 until min(childSize, itemSize)) {
        val childView = getChildAt(i)
        val dslItem = items[i]

        val tag = childView.getTag(R.id.tag)
        if (tag is Int && tag == dslItem.itemLayoutId) {
            //相同布局, 则使用缓存
            val dslViewHolder = childView.dslViewHolder()
            dslItem.itemBind(dslViewHolder, i, dslItem, emptyList())
        } else {
            //不同布局, 删除原先的view, 替换成新的
            replaceIndexList.add(i)
        }
    }

    //替换不相同的Item
    replaceIndexList.forEach { i ->
        val dslItem = items[i]

        removeViewAt(i)
        addDslItem(dslItem, i)
    }

    //移除多余的item
    for (i in itemSize until childSize) {
        removeViewAt(i)
    }

    //追加新的Item
    for (i in childSize until itemSize) {
        val dslItem = items[i]
        addDslItem(dslItem)
    }
}

/**查找[ViewGroup]中, 包含的[DslAdapterItem]集合*/
fun ViewGroup.findDslItemList(onlyVisible: Boolean = true): List<DslAdapterItem> {
    val result = mutableListOf<DslAdapterItem>()
    forEach { _, child ->
        val dslItem = child.tagDslAdapterItem()
        dslItem?.apply {
            if (onlyVisible) {
                if (child.isVisible()) {
                    result.add(this)
                }
            } else {
                result.add(this)
            }
        }
    }
    return result
}

/**更新所有[DslAdapterItem]在[ViewGroup]中*/
fun ViewGroup.updateAllDslItem(payloads: List<Any> = emptyList()) {
    forEach { index, child ->
        val dslItem = child.tagDslAdapterItem()
        dslItem?.also {
            it.itemBind(child.dslViewHolder(), index, it, payloads)
        }
    }
}

/**更新指定的[DslAdapterItem]在[ViewGroup]中*/
fun ViewGroup.updateDslItem(item: DslAdapterItem, payloads: List<Any> = emptyList()) {
    forEach { index, child ->
        val dslItem = child.tagDslAdapterItem()
        dslItem?.also {
            if (item == it) {
                it.itemBind(child.dslViewHolder(), index, it, payloads)
            }
        }
    }
}

/**更新或者插入指定的[DslAdapterItem]在[ViewGroup]中*/
fun ViewGroup.updateOrInsertDslItem(
    item: DslAdapterItem,
    insertIndex: Int = -1,
    payloads: List<Any> = emptyList()
) {
    var have = false
    forEach { index, child ->
        val dslItem = child.tagDslAdapterItem()
        if (item == dslItem) {
            have = true
            //更新
            item.itemBind(child.dslViewHolder(), index, item, payloads)
        }
    }
    if (!have) {
        //插入
        addDslItem(item, insertIndex, payloads)
    }
}

/**移除指定的[item]*/
fun ViewGroup.removeDslItem(item: DslAdapterItem?) {
    removeAllDslItem { index, dslAdapterItem -> dslAdapterItem == item }
}

/**移除所有符合规则的child*/
fun ViewGroup.removeAllDslItem(predicate: (Int, DslAdapterItem?) -> Boolean = { _, item -> item != null }) {

    val removeIndexList = mutableListOf<Int>()

    forEach { index, child ->
        val dslItem = child.tagDslAdapterItem()

        if (predicate(index, dslItem)) {
            removeIndexList.add(index)
        }
    }

    //移除item
    removeIndexList.reverse()
    removeIndexList.forEach {
        removeViewAt(it)
    }
}

//<editor-fold desc="DslAdapterItem操作">
