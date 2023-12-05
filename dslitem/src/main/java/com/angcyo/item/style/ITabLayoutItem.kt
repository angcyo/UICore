package com.angcyo.item.style

import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.tablayout.logi
import com.angcyo.widget.DslViewHolder

/**
 * [com.angcyo.tablayout.DslTabLayout]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/10
 */

interface ITabLayoutItem : IAutoInitItem {

    /**统一样式配置*/
    var tabLayoutItemConfig: TabLayoutItemConfig

    @ItemInitEntryPoint
    fun initTabLayoutItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<DslTabLayout>(tabLayoutItemConfig.itemTabLayoutViewId)?.apply {
            if (tabLayoutItemConfig.itemTabEquWidthCountRange == null) {
                tabLayoutItemConfig.itemTabEquWidthCountRange = itemEquWidthCountRange?.run {
                    "$first~$last"
                }
            }
            tabLayoutItemConfig.itemTabEquWidthCountRange?.let {
                updateItemEquWidthCountRange(it)
            }

            configTabLayoutConfig {
                //拦截设置
                onSelectItemView = { itemView, index, select, fromUser ->
                    tabLayoutItemConfig.onTabSelectItemView(itemView, index, select, fromUser)
                }
                //选中回调
                onSelectIndexChange = { fromIndex, selectList, reselect, fromUser ->
                    val toIndex = selectList.firstOrNull() ?: -1
                    val oldIndex = tabLayoutItemConfig.itemTabCurrentIndex
                    tabLayoutItemConfig.itemTabCurrentIndex = toIndex
                    tabLayoutItemConfig.onTabSelectIndexChange(
                        fromIndex,
                        selectList,
                        reselect,
                        fromUser
                    )
                    if (oldIndex != toIndex) {
                        adapterItem.itemChanging = true
                    }
                }
            }
            setCurrentItem(tabLayoutItemConfig.itemTabCurrentIndex, false, false)
        }
    }

    /**config*/
    fun configTabLayoutItem(action: TabLayoutItemConfig.() -> Unit) {
        tabLayoutItemConfig.action()
    }
}

/**当前的索引*/
var ITabLayoutItem.itemCurrentIndex: Int
    get() = tabLayoutItemConfig.itemTabCurrentIndex
    set(value) {
        tabLayoutItemConfig.itemTabCurrentIndex = value
    }

/**等宽范围设置*/
var ITabLayoutItem.itemTabEquWidthCountRange: String?
    get() = tabLayoutItemConfig.itemTabEquWidthCountRange
    set(value) {
        tabLayoutItemConfig.itemTabEquWidthCountRange = value
    }

/**选中回调*/
var ITabLayoutItem.itemTabSelectIndexChangeAction: (fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean) -> Unit
    get() = tabLayoutItemConfig.onTabSelectIndexChange
    set(value) {
        tabLayoutItemConfig.onTabSelectIndexChange = value
    }

class TabLayoutItemConfig : IDslItemConfig {

    /**[R.id.lib_tab_layout]*/
    var itemTabLayoutViewId: Int = R.id.lib_tab_layout

    /**当前选中项*/
    var itemTabCurrentIndex: Int = -1

    /**等宽范围*/
    var itemTabEquWidthCountRange: String? = null

    /**[com.angcyo.tablayout.DslSelectorConfig.onSelectItemView]*/
    var onTabSelectItemView: (itemView: View, index: Int, select: Boolean, fromUser: Boolean) -> Boolean =
        { _, _, _, _ ->
            false
        }

    /**
     * [[com.angcyo.tablayout.DslSelectorConfig.onSelectIndexChange]]
     * */
    var onTabSelectIndexChange: (fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean) -> Unit =
        { fromIndex, selectList, reselect, fromUser ->
            "选择:[$fromIndex]->${selectList} reselect:$reselect fromUser:$fromUser".logi()
        }
}