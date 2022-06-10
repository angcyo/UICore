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
            configTabLayoutConfig {
                //拦截设置
                onSelectItemView = { itemView, index, select, fromUser ->
                    tabLayoutItemConfig.onSelectItemView(itemView, index, select, fromUser)
                }
                //选中回调
                onSelectIndexChange = { fromIndex, selectList, reselect, fromUser ->
                    tabLayoutItemConfig.itemCurrentIndex = selectList.firstOrNull() ?: -1
                    tabLayoutItemConfig.onSelectIndexChange(
                        fromIndex,
                        selectList,
                        reselect,
                        fromUser
                    )
                }
            }
            setCurrentItem(tabLayoutItemConfig.itemCurrentIndex, false, false)
        }
    }

    /**config*/
    fun configTabLayoutItem(action: TabLayoutItemConfig.() -> Unit) {
        tabLayoutItemConfig.action()
    }
}

/**当前的索引*/
var ITabLayoutItem.itemCurrentIndex: Int
    get() = tabLayoutItemConfig.itemCurrentIndex
    set(value) {
        tabLayoutItemConfig.itemCurrentIndex = value
    }

/**选中回调*/
var ITabLayoutItem.itemSelectIndexChangeAction: (fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean) -> Unit
    get() = tabLayoutItemConfig.onSelectIndexChange
    set(value) {
        tabLayoutItemConfig.onSelectIndexChange = value
    }

class TabLayoutItemConfig : IDslItemConfig {

    /**[R.id.lib_tab_layout]*/
    var itemTabLayoutViewId: Int = R.id.lib_tab_layout

    /**当前选中项*/
    var itemCurrentIndex: Int = -1

    /**[com.angcyo.tablayout.DslSelectorConfig.onSelectItemView]*/
    var onSelectItemView: (itemView: View, index: Int, select: Boolean, fromUser: Boolean) -> Boolean =
        { _, _, _, _ ->
            false
        }

    /**
     * [[com.angcyo.tablayout.DslSelectorConfig.onSelectIndexChange]]
     * */
    var onSelectIndexChange: (fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean) -> Unit =
        { fromIndex, selectList, reselect, fromUser ->
            "选择:[$fromIndex]->${selectList} reselect:$reselect fromUser:$fromUser".logi()
        }
}