package com.angcyo.core.component

import android.os.Bundle
import android.widget.TextView
import com.angcyo.base.dslFHelper
import com.angcyo.core.CoreApplication
import com.angcyo.core.R
import com.angcyo.core.component.file.appFilePath
import com.angcyo.core.dslitem.DslLastDeviceInfoItem
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.bindItem
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
            add(DebugAction("debug ${if (isDebug()) "√" else "×"}", action = {
                Library.isDebugTypeVal = !Library.isDebugTypeVal
            }))

            add(DebugAction("浏览目录", action = {
                it.dslFHelper {
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

            //add(DebugAction("l.log", appFilePath("l.log", Constant.LOG_FOLDER_NAME)))
            add(DebugAction("l.log", CoreApplication.DEFAULT_FILE_PRINT_PATH))
            add(DebugAction("crash.log", DslCrashHandler.KEY_CRASH_FILE.hawkGet()))
            add(DebugAction("http.log", appFilePath(logFileName(), Constant.HTTP_FOLDER_NAME)))
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
                        DEBUG_ACTION_LIST,
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
                                    item.action?.invoke(this@DebugFragment)
                                }
                            }
                        }
                    }
            }

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
}

/**调试入口点*/
data class DebugAction(
    /**按钮的名字*/
    var name: String = "",
    /**日志的路径, 如果设置了则会直接显示对应的日志内容
     * 设置了[action]会覆盖默认的点击行为*/
    var logPath: String? = null,
    /**按钮的点击回调*/
    var action: ((DebugFragment) -> Unit)? = null
)