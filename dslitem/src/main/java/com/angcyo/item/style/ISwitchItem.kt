package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.github.SwitchButton
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder

/**
 * [com.angcyo.github.SwitchButton]
 * [com.angcyo.item.DslPropertySwitchItem]
 * [com.angcyo.item.DslSwitchInfoItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/08
 */
interface ISwitchItem : IAutoInitItem {

    /**统一样式配置*/
    var switchItemConfig: SwitchItemConfig

    @ItemInitEntryPoint
    fun initSwitchItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<SwitchButton>(switchItemConfig.itemSwitchViewId)?.apply {
            setOnCheckedChangeListener(null)

            //刷新界面的时候, 不执行动画
            val old = isEnableEffect
            isEnableEffect = false
            isChecked = itemSwitchChecked
            isEnableEffect = old

            setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
                override fun onCheckedChanged(view: SwitchButton, isChecked: Boolean) {
                    if (onSelfItemInterceptSwitchChanged(itemHolder, view, isChecked)) {
                        //拦截了
                        view.post { view.setChecked(itemSwitchChecked, false) }
                        return
                    } else {
                        onSelfItemSwitchChanged(isChecked)
                    }
                }
            })
        }
    }

    /**是否要拦截开关切换操作*/
    fun onSelfItemInterceptSwitchChanged(
        itemHolder: DslViewHolder,
        view: SwitchButton,
        checked: Boolean
    ): Boolean {
        return switchItemConfig.itemInterceptSwitchChangedAction(itemHolder, view, checked)
    }

    /**当自身的开关状态改变时通知*/
    fun onSelfItemSwitchChanged(isChecked: Boolean) {
        val checked = itemSwitchChecked
        itemSwitchChecked = isChecked
        if (checked != itemSwitchChecked) {
            if (this@ISwitchItem is DslAdapterItem) {
                itemChanging = true
            }
            switchItemConfig.itemSwitchChangedAction(itemSwitchChecked)
        }
    }

    /**config*/
    fun configSwitchItem(action: SwitchItemConfig.() -> Unit) {
        switchItemConfig.action()
    }

    /**更新开关的状态
     * [notify] 是否要通知回调
     * [force] 是否强制更新? 忽略相同的状态*/
    fun updateItemSwitchChecked(checked: Boolean, notify: Boolean = false, force: Boolean = false) {
        if (itemSwitchChecked != checked || force) {
            itemSwitchChecked = checked
            if (this is DslAdapterItem) {
                itemChanging = true
                updateAdapterItem()
            }
        }
        if (notify) {
            switchItemConfig.itemSwitchChangedAction(checked)
        }
    }
}

var ISwitchItem.itemSwitchChecked
    get() = switchItemConfig.itemSwitchChecked
    set(value) {
        switchItemConfig.itemSwitchChecked = value
    }

var ISwitchItem.itemSwitchChangedAction
    get() = switchItemConfig.itemSwitchChangedAction
    set(value) {
        switchItemConfig.itemSwitchChangedAction = value
    }

var ISwitchItem.itemInterceptSwitchChangedAction
    get() = switchItemConfig.itemInterceptSwitchChangedAction
    set(value) {
        switchItemConfig.itemInterceptSwitchChangedAction = value
    }

class SwitchItemConfig : IDslItemConfig {

    /**[R.id.lib_switch_view]*/
    var itemSwitchViewId: Int = R.id.lib_switch_view

    /**是否选中*/
    var itemSwitchChecked = false

    /**是否要拦截开关的切换*/
    var itemInterceptSwitchChangedAction: (itemHolder: DslViewHolder, view: SwitchButton, checked: Boolean) -> Boolean =
        { itemHolder, view, checked ->
            false
        }

    /**状态回调, 提供一个可以完全覆盖的方法*/
    var itemSwitchChangedAction: (checked: Boolean) -> Unit = {
    }
}