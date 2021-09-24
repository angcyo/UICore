package com.angcyo.item

import android.content.Context
import android.os.StatFs
import androidx.fragment.app.Fragment
import com.angcyo.base.dslFHelper
import com.angcyo.core.component.fileSelector
import com.angcyo.core.dslitem.IFragmentItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isItemLastInAdapter
import com.angcyo.library.app
import com.angcyo.library.component.work.Trackers
import com.angcyo.library.ex.*
import com.angcyo.library.toast
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.logFilePath
import com.angcyo.library.utils.writeTo
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.recycler.RecyclerBottomLayout
import com.angcyo.widget.span.DslSpan
import com.angcyo.widget.span.span

/**
 * 设备信息item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

class DslLastDeviceInfoItem : DslAdapterItem(), IFragmentItem {

    companion object {
        const val SPLIT = "/"

        fun saveDeviceInfo(context: Context = app()) {
            deviceInfo(context).toString()
                .writeTo(Constant.LOG_FOLDER_NAME.logFilePath("device.log"), false)
        }

        fun deviceInfo(context: Context, config: DslSpan.() -> Unit = {}) = span {
            append(getWifiIP()).append(SPLIT).append(getMobileIP())

            val vpn = Device.vpnInfo()
            val proxy = Device.proxyInfo()
            if (!vpn.isNullOrBlank() || !proxy.isNullOrBlank()) {
                vpn?.run {
                    append(SPLIT)
                    append(this)

                }
                proxy?.run {
                    append(SPLIT)
                    append(this)
                }
            }

            appendln()
            append("${Device.androidId}/${Device.serial}") {
                foregroundColor = getColor(R.color.colorPrimary)
            }
            appendln()
            append(Device.deviceId) {
                foregroundColor = getColor(R.color.colorPrimaryDark)
            }
            appendln()
            Device.buildString(this._builder)
            Device.screenInfo(context, this._builder)

            //机型信息
            appendln()
            Device.deviceInfoLess(this._builder)

            //网络信息
            appendln()
            append(Trackers.getInstance().networkStateTracker.activeNetworkState.toString())

            config(this)
        }
    }

    override var itemFragment: Fragment? = null

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

        //save
        saveDeviceInfo()

        itemHolder.v<RecyclerBottomLayout>(R.id.lib_item_root_layout)?.enableLayout =
            isItemLastInAdapter()

        //设备信息
        itemHolder.tv(R.id.lib_text_view)?.text = deviceInfo(itemHolder.context) {
            itemData = this
            onConfigDeviceInfo(this)
        }

        //SD空间信息
        val statFs = StatFs(
            itemHolder.context.getExternalFilesDir("")?.absolutePath
                ?: itemHolder.context.filesDir.absolutePath
        )
        val usedBytes = statFs.totalBytes - statFs.availableBytes
        val progress = (usedBytes * 1f / statFs.totalBytes * 100).toInt()
        itemHolder.v<DslProgressBar>(R.id.lib_progress_bar)?.setProgress(progress)
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
            itemFragment?.dslFHelper {
                fileSelector({
                    showFileMd5 = true
                    showFileMenu = true
                    showHideFile = true
                }) {

                }
            }
            _clickListener?.onClick(it)
        }
    }
}