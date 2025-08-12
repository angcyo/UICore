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
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isItemLastInAdapter
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.library.app
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.component.work.Trackers
import com.angcyo.library.ex.copy
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.getMobileIP
import com.angcyo.library.ex.getWifiIP
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.libFolderPath
import com.angcyo.library.toast
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.ID
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.toLogFilePath
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

        /**分隔符*/
        const val SPLIT = "/"

        /**最前面, 额外的设备信息获取回调*/
        var beforeAdditionalInfoAction: (() -> String?)? = null

        /**额外的设备信息获取回调*/
        var additionalInfoAction: (() -> String?)? = null

        /**额外的设备信息获取回调2*/
        var additionalInfoAction2: (() -> String?)? = null

        /**额外的设备信息获取回调3*/
        var additionalInfoAction3: (() -> String?)? = null

        /**保存设备信息到日志
         * [com.angcyo.library.utils.LogFile.device]*/
        fun saveDeviceInfo(
            context: Context = app(),
            isCompliance: Boolean = ComplianceCheck.isCompliance() /*&& !RBackground.isBackground()*/,
            config: DslSpan.() -> Unit = {}
        ): String {
            val deviceInfo = deviceInfo(context, isCompliance, {
                beforeAdditionalInfoAction?.invoke()?.let { append(it) }
            }) {
                config()
                appendln()
                append("<-${nowTimeString()}")

                //额外的信息
                additionalInfoAction?.invoke()?.let { append(it) }
                additionalInfoAction2?.invoke()?.let { append(it) }
                additionalInfoAction3?.invoke()?.let { append(it) }
            }.toString()
            deviceInfo.writeTo(LogFile.device.toLogFilePath(), false)
            return deviceInfo
        }

        /**[isCompliance] 是否合规了*/
        fun deviceInfo(
            context: Context = app(),
            isCompliance: Boolean = LibHawkKeys.isCompliance,
            beforeConfig: DslSpan.() -> Unit = {
                beforeAdditionalInfoAction?.invoke()?.let { append(it) }
            },
            config: DslSpan.() -> Unit = {},
        ) = span {
            beforeConfig(this)

            val api = HttpConfigDialog.appBaseUrl //getAppString("base_api")
            if (api.isNotEmpty()) {
                appendLine(api)
            }

            if (isCompliance) {
                append(getWifiIP()).append(SPLIT).append(getMobileIP())
            }

            //gmt 语言
            appendln()
            append(LanguageModel.getTimeZoneDes())
            appendln()
            append(LanguageModel.getCurrentLanguageDisplayName())
            append("/")
            append(LanguageModel.getCurrentLanguage())
            append("/")
            append(LanguageModel.getCurrentLanguageTag())

            //vpn 代理
            if (isCompliance) {
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
            }

            appendln()
            //id
            val isDarkMode = vmApp<NightModel>().isDarkMode
            if (isCompliance) {
                append("${Device.androidId}/${Device.serial}") {
                    foregroundColor =
                        if (isDarkMode) getColor(R.color.text_primary_color) else getColor(R.color.colorPrimary)
                }
                appendln()
            }
            append(Device.deviceId) {
                foregroundColor =
                    if (isDarkMode) getColor(R.color.text_general_color) else getColor(R.color.colorPrimaryDark)
            }
            appendln()
            append(ID.id) {
                foregroundColor =
                    if (isDarkMode) getColor(R.color.text_primary_color) else getColor(R.color.colorPrimary)
            }
            appendln()

            //本地APK编译信息
            Device.buildString(this._builder)
            //屏幕信息
            Device.screenInfo(context, this._builder)

            //机型信息
            if (isCompliance) {
                appendln()
                Device.deviceInfoLess(this._builder)
            }

            //网络信息
            if (isCompliance) {
                appendln()
                append(Trackers.getInstance().networkStateTracker.activeNetworkState.toString())
            }

            //sd memory
            appendln()
            appendln()
            _statFsInfo(this, context)

            config(this)
        }

        /**SD空间信息
         * @return 剩余空间比例[0~100]*/
        fun _statFsInfo(appendable: Appendable, context: Context = app()): Float {
            //SD空间信息
            val statFs = StatFs(
                context.getExternalFilesDir("")?.absolutePath ?: context.filesDir.absolutePath
            )
            val usedBytes = statFs.totalBytes - statFs.availableBytes
            val progress = usedBytes * 1f / statFs.totalBytes * 100

            appendable.apply {
                append(usedBytes.fileSizeString())
                append("/")
                append(statFs.totalBytes.fileSizeString())
                append(" ")
                append("$progress")
                append("%")

                //内存信息
                append(" (${Device.getMemoryUseInfo()})")

                //内存信息2
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                append(" (${manager.memoryClass}MB") //app分配的内存
                append("/${manager.largeMemoryClass}MB)") //app分配的内存2

                //内存信息3
                append(" (${Runtime.getRuntime().freeMemory().fileSizeString()}")
                append("/${Runtime.getRuntime().totalMemory().fileSizeString()}")
                append("/${Runtime.getRuntime().maxMemory().fileSizeString()})")
            }

            return progress
        }
    }

    override var itemFragment: Fragment? = null

    /**额外的配置信息回调*/
    var onConfigDeviceInfo: (DslSpan) -> Unit = {}

    init {
        itemLayoutId = R.layout.lib_item_last_device_info

        itemClick = {
            itemFragment?.dslFHelper {
                fileSelector({
                    showFileMd5 = true
                    showFileMenu = true
                    showHideFile = true
                    targetPath =
                        FileUtils.appRootExternalFolder().absolutePath
                            ?: storageDirectory
                })
            }
        }
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
                toast("设备调试信息已复制")
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