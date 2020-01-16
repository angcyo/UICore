package com.angcyo.core.dslitem

import com.angcyo.activity.copy
import com.angcyo.core.R
import com.angcyo.core.component.toast
import com.angcyo.core.utils.Device
import com.angcyo.drawable.getColor
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.getMobileIP
import com.angcyo.library.ex.getWifiIP
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.DslSpan
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

class DslLastDeviceInfoItem : DslAdapterItem() {

    /**额外的配置信息回调*/
    var onConfigDeviceInfo: (DslSpan) -> Unit = {}

    init {
        itemLayoutId = R.layout.lib_item_last_device_info
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.tv(R.id.lib_text_view)?.text = span {
            append(getWifiIP()).append("|").append(getMobileIP())
            appendln()
            append(Device.getUniquePsuedoID()) {
                foregroundColor = getColor(R.color.colorPrimaryDark)
            }
            appendln()
            Device.buildString(this._builder)
            Device.screenInfo(itemHolder.content, this._builder)

            itemData = this

            onConfigDeviceInfo(this)
        }

        itemHolder.clickItem {
            itemData?.toString()?.run {
                copy()
                toast("信息已复制")
            }
        }
    }
}