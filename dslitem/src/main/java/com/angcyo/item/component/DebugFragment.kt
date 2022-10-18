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
import com.angcyo.core.dslitem.DslLastDeviceInfoItem
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.bindItem
import com.angcyo.dsladapter.drawBottom
import com.angcyo.item.DslPropertyNumberItem
import com.angcyo.item.DslPropertySwitchItem
import com.angcyo.item.style.itemDes
import com.angcyo.item.style.itemLabel
import com.angcyo.item.style.itemSwitchChangedAction
import com.angcyo.item.style.itemSwitchChecked
import com.angcyo.library.Library
import com.angcyo.library.ex.*
import com.angcyo.library.libFolderPath
import com.angcyo.library.toast
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.logFileName
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.resetChild

/**
 * 调试界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/14
 */
class DebugFragment : BaseDslFragment() {

    companion object {

        /**调试入口*/
        val DEBUG_ACTION_LIST = mutableListOf<DebugAction>().apply {
            add(DebugAction("debug ${if (isDebug()) "√" else "×"}", action = { fragment, value ->
                Library.isDebugTypeVal = !Library.isDebugTypeVal
            }))
            /*add(DebugAction().apply {
                key = "key_debug_type"
                label = "Debug环境"
                des = "强制开启调试环境?"
                action = { debugFragment, value ->
                    Library.isDebugTypeVal = value as Boolean
                    debugFragment._adapter.updateAllItem()
                }
            })*/

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

            //单独的日志
            add(DebugAction("l.log", CoreApplication.DEFAULT_FILE_PRINT_PATH))
            add(DebugAction("crash.log", DslCrashHandler.KEY_CRASH_FILE.hawkGet()))
            add(DebugAction("http-date.log", appFilePath(logFileName(), Constant.HTTP_FOLDER_NAME)))
            //log/目录下的日志
            add(DebugAction("log.log", appFilePath("log.log", Constant.LOG_FOLDER_NAME)))
            add(DebugAction("http.log", appFilePath("http.log", Constant.LOG_FOLDER_NAME)))
            add(DebugAction("error.log", appFilePath("error.log", Constant.LOG_FOLDER_NAME)))
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
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        renderDslAdapter {
            bindItem(R.layout.item_debug_flow_layout) { itemHolder, itemPosition, adapterItem, payloads ->
                itemHolder.group(R.id.lib_flow_layout)
                    ?.resetChild(
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
                                        fContext().fileViewDialog(logPath)
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
                if (!debugAction.key.isNullOrEmpty()) {
                    //有key
                    if (debugAction.type == Boolean::class.java) {
                        //开关控制
                        DslPropertySwitchItem()() {
                            itemLabel = debugAction.label
                            itemDes = debugAction.des
                            initItem()

                            itemSwitchChecked = debugAction.key.hawkGetBoolean() == true
                            itemSwitchChangedAction = {
                                debugAction.key.hawkPut(it)

                                debugAction.action?.invoke(this@DebugFragment, it)
                            }
                        }
                    } else if (debugAction.type == Int::class.java) {
                        //数字输入
                        DslPropertyNumberItem()() {
                            itemLabel = debugAction.label
                            itemDes = debugAction.des
                            initItem()

                            itemPropertyNumber =
                                debugAction.key.hawkGetInt((debugAction.defValue as? Int) ?: -1)
                            observeItemChange {
                                debugAction.key.hawkPut(itemPropertyNumber)
                                debugAction.action?.invoke(this@DebugFragment, itemPropertyNumber)
                            }
                        }
                    }
                }
            }

            //last
            DslLastDeviceInfoItem()() {
                itemClick = {
                    dslFHelper {
                        fileSelector({
                            showFileMd5 = true
                            showFileMenu = true
                            showHideFile = true
                            targetPath =
                                FileUtils.appRootExternalFolder().absolutePath ?: storageDirectory
                        })
                    }
                }
            }
        }
    }

    fun DslAdapterItem.initItem() {
        drawBottom()
    }
}