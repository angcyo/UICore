package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.filter.RemoveItemDecorationFilterAfterInterceptor
import com.angcyo.dsladapter.toEmpty
import com.angcyo.dsladapter.updateNow
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.OverridePoint
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.layoutDelegate
import com.angcyo.widget.recycler.initDslAdapter

/**
 * 简单的[androidx.recyclerview.widget.RecyclerView]对话框, 支持标题栏
 *
 * [com.angcyo.dialog.RecyclerDialogConfig]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/26
 */
open class SingleRecyclerDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**最大的高度*/
    var dialogMaxHeight: String? = "0.5sh"

    //适配器
    var _adapter: DslAdapter? = null

    /**移除收尾分割线*/
    var removeFirstLastItemDecoration: Boolean = true

    /**渲染回调*/
    var onRenderAction: ((recyclerView: RecyclerView, adapter: DslAdapter) -> Unit)? = null

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.group(R.id.lib_dialog_root_layout)?.layoutDelegate {
            rMaxHeight = dialogMaxHeight
        }

        initRecycler(dialog, dialogViewHolder)
    }

    //---

    @OverridePoint
    open fun initRecycler(dialog: Dialog, holder: DslViewHolder) {
        holder.rv(R.id.lib_recycler_view)?.let { recyclerView ->
            //初始化DslAdapter
            _adapter = recyclerView.initDslAdapter() {
                if (removeFirstLastItemDecoration) {
                    dslDataFilter?.dataAfterInterceptorList?.add(
                        RemoveItemDecorationFilterAfterInterceptor()
                    )
                }

                //init
                onInitRecyclerAdapter(dialog, holder, recyclerView, this)

                if (onRenderAction == null) {
                    if (isEmpty()) {
                        toEmpty()
                    }
                } else {
                    onRenderAction?.invoke(recyclerView, this)
                }

                updateNow()
            }
        }
    }

    /**初始化*/
    @OverridePoint
    open fun onInitRecyclerAdapter(
        dialog: Dialog,
        holder: DslViewHolder,
        recyclerView: RecyclerView,
        adapter: DslAdapter
    ) {

    }

    /**直接渲染界面, 此方法需要在[com.angcyo.dialog.SingleRecyclerDialogConfig.initRecycler]之前调用*/
    @CallPoint
    fun renderAdapter(action: DslAdapter.() -> Unit) {
        onRenderAction = { recyclerView, adapter ->
            adapter._recyclerView = recyclerView
            adapter.action()
        }
    }

}