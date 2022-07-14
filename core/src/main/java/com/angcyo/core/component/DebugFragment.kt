package com.angcyo.core.component

import android.os.Bundle
import android.widget.TextView
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.bindItem
import com.angcyo.library.ex.find
import com.angcyo.library.ex.isFileExist
import com.angcyo.library.libFolderPath
import com.angcyo.library.toast
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
                                    if (item.log.isFileExist()) {
                                        //日志文件存在, 直接显示日志内容

                                    } else {
                                        toast("not support!")
                                    }
                                } else {
                                    item.action.invoke(this@DebugFragment)
                                }
                            }
                        }
                    }
            }
        }
    }
}

/**调试入口点*/
data class DebugAction(
    /**按钮的名字*/
    val name: String,
    /**日志的路径, 如果设置了则会直接显示对应的日志内容
     * 设置了[action]会覆盖默认的点击行为*/
    val log: String? = null,
    /**按钮的点击回调*/
    val action: ((DebugFragment) -> Unit)? = null
)