package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.layoutDelegate
import com.angcyo.widget.recycler.DslRecyclerView
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * 简单的[androidx.recyclerview.widget.RecyclerView]对话框配置
 *
 * [RecyclerDialogConfig]具有更多的特性, 比如多选/单选
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/04
 */
abstract class BaseRecyclerDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**最大的高度*/
    var dialogMaxHeight: String? = "0.5sh"

    /**渲染回调*/
    var onRenderAdapterAction: (DslAdapter.() -> Unit)? = null

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.group(R.id.lib_dialog_root_layout)?.layoutDelegate {
            rMaxHeight = dialogMaxHeight
        }

        //recycler
        initRecyclerView(dialog, dialogViewHolder, dialogViewHolder.v(R.id.lib_recycler_view))
    }

    /**初始化*/
    open fun initRecyclerView(
        dialog: Dialog,
        dialogViewHolder: DslViewHolder,
        recyclerView: DslRecyclerView?
    ) {
        //recycler
        recyclerView?.renderDslAdapter {
            onRenderAdapterAction?.invoke(this)
        }
    }

    /**重新渲染界面*/
    fun refreshDslAdapter() {
        _dialogViewHolder?.rv(R.id.lib_recycler_view)?.apply {
            renderDslAdapter {
                onRenderAdapterAction?.invoke(this)
            }
        }
    }

}