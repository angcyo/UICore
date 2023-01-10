package com.angcyo.core.dslitem

import android.app.ActivityManager
import android.content.Context
import android.os.StatFs
import androidx.fragment.app.Fragment
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.component.ComplianceCheck
import com.angcyo.core.component.HttpConfigDialog
import com.angcyo.core.component.fileSelector
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isItemLastInAdapter
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.library.app
import com.angcyo.library.component.RBackground
import com.angcyo.library.component.work.Trackers
import com.angcyo.library.ex.*
import com.angcyo.library.libFolderPath
import com.angcyo.library.toast
import com.angcyo.library.utils.*
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

        /**分隔符*/
        const val SPLIT = "/"

        /**保存设备信息到日志
         * [com.angcyo.library.utils.LogFile.device]*/
        fun saveDeviceInfo(
            context: Context = app(),
            isCompliance: Boolean = ComplianceCheck.isCompliance() && !RBackground.isBackground(),
            config: DslSpan.() -> Unit = {}
        ) {
            deviceInfo(context, isCompliance) {
                config()
                appendln()
                append("<-${nowTimeString()}")
            }.toString().writeTo(LogFile.device.toLogFilePath(), false)
        }

        /**[isCompliance] 是否合规了*/
        fun deviceInfo(
            context: Context = app(),
            isCompliance: Boolean = true,
            config: DslSpan.() -> Unit = {}
        ) = span {
            val api = HttpConfigDialog.appBaseUrl //getAppString("base_api")
            if (api.isNotEmpty()) {
                appendLine(api)
            }
            if (isCompliance) {
                append(getWifiIP()).append(SPLIT).append(getMobileIP())
            } else {
                append(getWifiIP())
            }

            //gmt
            appendln()
            append(LanguageModel.getTimeZoneDes())
            append("/")
            append(LanguageModel.getCurrentLanguage())
            append("/")
            append(LanguageModel.getCurrentLanguageTag())

            //vpn 代理
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
            //id
            if (isCompliance) {
                append("${Device.androidId}/${Device.serial}") {
                    foregroundColor = getColor(R.color.colorPrimary)
                }
                appendln()
            }
            append(Device.deviceId) {
                foregroundColor = getColor(R.color.colorPrimaryDark)
            }
            appendln()
            append(ID.id) {
                foregroundColor = getColor(R.color.colorPrimary)
            }
            appendln()

            //本地APK编译信息
            Device.buildString(this._builder)
            //屏幕信息
            Device.screenInfo(context, this._builder)

            //机型信息
            appendln()
            Device.deviceInfoLess(this._builder)

            //网络信息
            appendln()
            append(Trackers.getInstance().networkStateTracker.activeNetworkState.toString())

            //sd
            appendln()
            appendln()
            _statFsInfo(this, context)

            config(this)
        }

        /**SD空间信息
         * @return 剩余空间比例[0~100]*/
        fun _statFsInfo(appendable: Appendable, context: Context = app()): Int {
            //SD空间信息
            val statFs = StatFs(
                context.getExternalFilesDir("")?.absolutePath ?: context.filesDir.absolutePath
            )
            val usedBytes = statFs.totalBytes - statFs.availableBytes
            val progress = (usedBytes * 1f / statFs.totalBytes * 100).toInt()

            appendable.apply {
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

                //内存信息2
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                append(" (${manager.memoryClass}MB")
                append("/${manager.largeMemoryClass}MB)")

                //内存信息3
                append(" (${Runtime.getRuntime().freeMemory().fileSizeString()}")
                append(" /${Runtime.getRuntime().totalMemory().fileSizeString()}")
                append(" /${Runtime.getRuntime().maxMemory().fileSizeString()})")
            }

            return progress
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

        itemHolder.tv(R.id.lib_tip_view)?.text = span {
            itemHolder.v<DslProgressBar>(R.id.lib_progress_bar)?.setProgress(_statFsInfo(this))
        }

        itemHolder.clickItem {
            itemData?.toString()?.run {
                copy()
                toast("信息已复制")
            }
            if (itemClick == null) {
                itemFragment?.dslFHelper {
                    fileSelector({
                        showFileMd5 = true
                        showFileMenu = true
                        showHideFile = true
                        targetPath = libFolderPath()
                    }) {
                        //no op
                    }
                }
            }
            _clickListener?.onClick(it)

            //刷新自身
            updateAdapterItem()
        }
    }
}