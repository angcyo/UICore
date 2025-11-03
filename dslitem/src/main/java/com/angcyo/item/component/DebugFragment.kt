package com.angcyo.item.component

import android.os.Bundle
import android.widget.TextView
import com.angcyo.base.dslFHelper
import com.angcyo.core.CoreApplication
import com.angcyo.core.R
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.file.appFilePath
import com.angcyo.core.component.fileSelector
import com.angcyo.core.component.fileViewDialog
import com.angcyo.core.component.httpConfigDialog
import com.angcyo.core.dslitem.DslLastDeviceInfoItem
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.bindItem
import com.angcyo.dsladapter.drawBottom
import com.angcyo.item.DslPropertyNumberItem
import com.angcyo.item.DslPropertyStringItem
import com.angcyo.item.DslPropertySwitchItem
import com.angcyo.item.style.itemDes
import com.angcyo.item.style.itemEditText
import com.angcyo.item.style.itemLabel
import com.angcyo.item.style.itemMaxInputLength
import com.angcyo.item.style.itemSwitchChangedAction
import com.angcyo.item.style.itemSwitchChecked
import com.angcyo.library.Library
import com.angcyo.library.ex.file
import com.angcyo.library.ex.find
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkGetBoolean
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isFile
import com.angcyo.library.ex.isFolder
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.libFolderPath
import com.angcyo.library.toast
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.logFileName
import com.angcyo.library.utils.toLogFilePath
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.resetChild

/**
 * 调试界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/14
 */
open class DebugFragment : BaseDslFragment() {

    companion object {

        /**调试入口*/
        val DEBUG_ACTION_LIST = mutableListOf<DebugAction>().apply {
            add(DebugAction("debug ${if (isDebug()) "√" else "×"}", action = { fragment, value ->
                Library.isDebugTypeVal = !Library.isDebugTypeVal
            }))
            add(DebugAction("模拟崩溃", action = { debugFragment, value ->
                throw IllegalStateException("模拟崩溃测试:${nowTimeString()}")
            }))

            add(DebugAction("浏览目录", action = { fragment, value ->
                fragment.dslFHelper {
                    fileSelector({
                        targetPath = libFolderPath("")
                        showFileMd5 = true
                        showFileMenu = true
                        showHideFile = true
                    }) {
                        //no op
                    }
                }
            }))
            add(DebugAction("服务器配置", action = { fragment, value ->
                fragment.fContext().httpConfigDialog()
            }))
            add(DebugAction("接口请求", action = { fragment, value ->
                fragment.dslFHelper {
                    show(HttpRequestTestFragment::class)
                }
            }))

            //单独的日志
            add(DebugAction(LogFile.l, CoreApplication.DEFAULT_FILE_PRINT_PATH))
            add(DebugAction("crash.log", DslCrashHandler.KEY_CRASH_FILE.hawkGet()))
            add(DebugAction("http-date.log", appFilePath(logFileName(), Constant.HTTP_FOLDER_NAME)))
            //log/目录下的日志
            add(DebugAction(LogFile.log, LogFile.log.toLogFilePath(), true))
            add(DebugAction(LogFile.http, LogFile.http.toLogFilePath(), true))
            add(DebugAction(LogFile.error, LogFile.error.toLogFilePath(), true))
            add(DebugAction(LogFile.perf, LogFile.perf.toLogFilePath(), true))
        }

        /**
         * DebugFragment.addDebugAction {
         *     name = "FileServer"
         *     action = {
         *       coreApp().bindFileServer()
         *       //(RBackground.lastActivityRef?.get() as? LifecycleOwner ?: it)
         *   }
         * }
         * */
        fun addDebugAction(action: DebugAction.() -> Unit) {
            DEBUG_ACTION_LIST.add(DebugAction().apply(action))
        }
    }

    init {
        fragmentTitle = "调试界面"
        enableSoftInput = true
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        renderActions()
    }

    /**渲染界面*/
    open fun renderActions() {
        renderDslAdapter {
            bindItem(R.layout.item_debug_flow_layout) { itemHolder, itemPosition, adapterItem, payloads ->
                itemHolder.group(R.id.lib_flow_layout)
                    ?.resetChild(
                        //渲染按钮actions
                        DEBUG_ACTION_LIST.filter { it.key.isNullOrEmpty() },
                        R.layout.lib_button_layout
                    ) { itemView, item, itemIndex ->
                        itemView.find<TextView>(R.id.lib_button)?.apply {
                            text = item.name
                            clickIt {
                                if (item.action == null) {
                                    val logPath = item.logPath
                                    val file = logPath?.file()
                                    if (file.isFile()) {
                                        //日志文件存在, 直接显示日志内容
                                        fContext().fileViewDialog(logPath) {
                                            //1mb 时反向读取文件
                                            readReversed = item.readReversed ?: ((file?.length()
                                                ?: 0) > 1 * 1024 * 1024)
                                        }
                                    } else if (file.isFolder()) {
                                        //文件目录浏览
                                        dslFHelper {
                                            fileSelector({
                                                targetPath = logPath!!
                                                showFileMd5 = true
                                                showFileMenu = true
                                                showHideFile = true
                                            }) {
                                                //no op
                                            }
                                        }
                                    } else {
                                        toast("not support!")
                                    }
                                } else {
                                    item.action?.invoke(this@DebugFragment, item)
                                }
                            }
                        }
                    }
            }

            //item
            DEBUG_ACTION_LIST.forEach { debugAction ->
                //edit 属性 actions
                renderDebugAction(debugAction)
            }

            //last
            DslLastDeviceInfoItem()()
        }
    }

    /**渲染一个[DebugAction]*/
    fun DslAdapter.renderDebugAction(debugAction: DebugAction) {
        if (!debugAction.key.isNullOrEmpty()) {
            //有key
            if (debugAction.type == Boolean::class.java) {
                //开关控制
                DslPropertySwitchItem()() {
                    itemLabel = debugAction.label
                    itemDes = debugAction.des
                    initItem()

                    val defValue = debugAction.defValue
                    itemSwitchChecked = if (defValue is Boolean) {
                        debugAction.key.hawkGetBoolean(defValue)
                    } else {
                        debugAction.key.hawkGetBoolean()
                    }
                    itemSwitchChangedAction = {
                        debugAction.key.hawkPut(it)

                        debugAction.action?.invoke(this@DebugFragment, it)
                    }
                }
            } else if (DslPropertyNumberItem.isNumber(debugAction.type)) {
                //数字输入
                DslPropertyNumberItem()() {
                    itemLabel = debugAction.label
                    itemDes = debugAction.des
                    itemUseNewNumberKeyboardDialog = debugAction.useNewNumberKeyboardDialog
                    initItem()

                    itemPropertyNumber = when {
                        DslPropertyNumberItem.isInt(debugAction.type) -> debugAction.key.hawkGet(
                            (debugAction.defValue as? Int) ?: 0
                        )

                        DslPropertyNumberItem.isLong(debugAction.type) -> debugAction.key.hawkGet(
                            (debugAction.defValue as? Long) ?: 0L
                        )

                        DslPropertyNumberItem.isFloat(debugAction.type) -> debugAction.key.hawkGet(
                            (debugAction.defValue as? Float) ?: 0f
                        )

                        else -> debugAction.defValue as? Number
                    }

                    observeItemChange {
                        debugAction.key.hawkPut(itemPropertyNumber)
                        debugAction.action?.invoke(this@DebugFragment, itemPropertyNumber)
                    }
                }
            } else {
                //文本输入
                DslPropertyStringItem()() {
                    itemLabel = debugAction.label
                    itemDes = debugAction.des
                    initItem()

                    itemEditText = debugAction.key.hawkGet("${debugAction.defValue ?: ""}")
                    itemMaxInputLength = 1000
                    observeItemChange {
                        debugAction.key.hawkPut(itemEditText)
                        debugAction.action?.invoke(this@DebugFragment, itemEditText)
                    }
                }
            }
        }
    }

    fun DslAdapterItem.initItem() {
        drawBottom()
    }
}