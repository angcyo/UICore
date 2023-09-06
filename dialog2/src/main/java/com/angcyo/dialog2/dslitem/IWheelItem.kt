package com.angcyo.dialog2.dslitem

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog2.WheelDialogConfig
import com.angcyo.dialog2.wheelDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.style.IAutoInitItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.itemLabel
import com.angcyo.item.style.itemText
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.size
import com.angcyo.library.ex.string
import com.angcyo.library.extend.IToText

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
interface IWheelItem : IAutoInitItem {

    /**统一配置*/
    var wheelItemConfig: WheelItemConfig

    /**显示dialog*/
    @CallPoint
    fun showItemWheelDialog(context: Context) {
        context.wheelDialog {
            if (wheelItemConfig.itemWheelDialogTitle != null) {
                dialogTitle = wheelItemConfig.itemWheelDialogTitle
            } else if (this is ILabelItem) {
                dialogTitle = itemLabel
            } else if (this is ITextItem) {
                dialogTitle = itemText
            }

            //赋值
            wheelItems = wheelItemConfig.itemWheelList
            wheelItemToStringAction = wheelItemConfig.itemWheelToTextAction
            wheelUnit = wheelItemConfig.itemWheelUnit
            wheelItemSelectorAction = { dialog, index, item ->
                if (wheelItemConfig.itemWheelSelectorAction(dialog, index, item)) {
                    //拦截了
                    true
                } else {
                    onSelfWheelItemSelector(index, item)
                    if (this@IWheelItem is DslAdapterItem) {
                        val old = wheelItemConfig.itemSelectedIndex
                        wheelItemConfig.itemSelectedIndex = index
                        itemChanging = old != index
                    }
                    false
                }
            }

            //默认选中
            wheelSelectedIndex = wheelItemConfig.itemSelectedIndex

            //action
            wheelItemConfig.itemConfigDialogAction(this)
        }
    }

    /**选中后的回调*/
    @CallPoint
    fun onSelfWheelItemSelector(index: Int, item: Any) {

    }
}

var IWheelItem.itemWheelList
    get() = wheelItemConfig.itemWheelList
    set(value) {
        wheelItemConfig.itemWheelList = value
    }

var IWheelItem.itemWheelDialogTitle
    get() = wheelItemConfig.itemWheelDialogTitle
    set(value) {
        wheelItemConfig.itemWheelDialogTitle = value
    }

var IWheelItem.itemWheelUnit
    get() = wheelItemConfig.itemWheelUnit
    set(value) {
        wheelItemConfig.itemWheelUnit = value
    }

var IWheelItem.itemWheelSelectorAction
    get() = wheelItemConfig.itemWheelSelectorAction
    set(value) {
        wheelItemConfig.itemWheelSelectorAction = value
    }

var IWheelItem.itemWheelToTextAction
    get() = wheelItemConfig.itemWheelToTextAction
    set(value) {
        wheelItemConfig.itemWheelToTextAction = value
    }

var IWheelItem.itemSelectedIndex
    get() = wheelItemConfig.itemSelectedIndex
    set(value) {
        wheelItemConfig.itemSelectedIndex = value
        if (this is ITextItem) {
            itemText = itemWheelText()
        }
    }

var IWheelItem.itemConfigDialogAction
    get() = wheelItemConfig.itemConfigDialogAction
    set(value) {
        wheelItemConfig.itemConfigDialogAction = value
    }

class WheelItemConfig : IDslItemConfig {

    /**对话框标题*/
    var itemWheelDialogTitle: CharSequence? = null

    /**数据集合*/
    var itemWheelList: List<Any>? = null

    /**设置选中项, -1不设置*/
    var itemSelectedIndex = -1

    /**wheel dialog 单位设置*/
    var itemWheelUnit: CharSequence? = null

    /**选中回调, 不拦截则默认处理*/
    var itemWheelSelectorAction: (dialog: Dialog, index: Int, item: Any) -> Boolean =
        { dialog, index, item ->
            false
        }

    /**上屏显示转换回调*/
    var itemWheelToTextAction: (item: Any) -> CharSequence? = {
        if (it is IToText) {
            it.toText()
        } else {
            it.string()
        }
    }

    /**配置[WheelDialogConfig]*/
    var itemConfigDialogAction: (WheelDialogConfig) -> Unit = {

    }
}

/**[itemSelectedIndex]*/
fun IWheelItem.updateWheelSelectedIndex(item: Any?) {
    itemWheelList?.indexOf(item)?.let {
        itemSelectedIndex = it
    }
}

/**快速获取对应Item的值*/
fun IWheelItem.itemWheelValue(): Any? {
    return itemWheelList?.getOrNull(itemSelectedIndex)
}

/**快速将选中的item转成字符创*/
fun IWheelItem.itemWheelText(): CharSequence? {
    val item = itemWheelList?.getOrNull(itemSelectedIndex) ?: return null
    return itemWheelToTextAction(item)
}

fun <T> IWheelItem.itemWheelBean(): T? {
    return itemWheelList?.getOrNull(itemSelectedIndex) as T?
}

inline fun <reified DATA> IWheelItem.itemWheelData(): DATA? {
    return itemWheelList?.getOrNull(itemSelectedIndex) as DATA?
}

/**更新默认选中的项, 如果可行
 * [index] 想要选中的索引
 * [item] 想要选中的数据, 如果设置了, 则优先级高
 * [defText] 默认情况下需要显示的文本*/
fun IWheelItem.updateWheelSelected(
    index: Int,
    item: Any? = null,
    defText: CharSequence? = null
) {
    //默认显示的文本
    val text = if (item == null) {
        defText
    } else {
        itemWheelToTextAction(item) ?: defText
    }
    if (this is ITextItem) {
        itemText = text
    } else if (this is ILabelItem) {
        itemLabel = text
    }

    itemSelectedIndex = -1

    val list = itemWheelList
    if (list != null) {
        //查找对应的index

        val _index = if (item == null) {
            index
        } else {
            list.indexOfFirst {
                itemWheelToTextAction(it) == itemWheelToTextAction(item)
            }.run {
                if (this == -1) index else this
            }
        }

        val size = list.size()
        if (_index in 0 until size) {
            //在范围内
            itemSelectedIndex = _index
            return
        }
    }
}

