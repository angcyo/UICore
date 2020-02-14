package com.angcyo.item

import android.os.StatFs
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.*
import com.angcyo.library.toast
import com.angcyo.library.utils.Device
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
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
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //设备信息
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

        //SD空间信息
        val statFs = StatFs(
            itemHolder.content.getExternalFilesDir("")?.absolutePath
                ?: itemHolder.content.filesDir.absolutePath
        )
        val usedBytes = statFs.totalBytes - statFs.availableBytes
        val progress = (usedBytes * 1f / statFs.totalBytes * 100).toInt()
        itemHolder.v<DslProgressBar>(R.id.lib_progress_bar)
            ?.setProgress(progress)
        itemHolder.tv(R.id.lib_tip_view)?.text = span {
            append(usedBytes.fileSizeString())
            append("/")
            append(statFs.totalBytes.fileSizeString())
            append(" ")
            append("$progress")
            append("%")

            //内存信息
            append(" (")
            append(Device.getAvailableMemory().fileSizeString())
            append(" /")
            append(Device.getTotalMemory().fileSizeString())
            append(")")
        }

        itemHolder.clickItem {
            itemData?.toString()?.run {
                copy()
                toast("信息已复制")
            }
        }
    }
}