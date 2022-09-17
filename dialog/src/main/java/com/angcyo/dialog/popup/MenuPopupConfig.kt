package com.angcyo.dialog.popup

import android.content.Context
import android.view.View
import com.angcyo.dialog.PopupConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.dismissWindow
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.clipBoundsAnimator
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.have
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._rv
import com.angcyo.widget.recycler.DslRecyclerView
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * 垂直弹出的菜单Popup, 带圆角/带阴影的弹窗
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/17
 */
open class MenuPopupConfig : PopupConfig() {

    companion object {

        /**点击item的时候, 自动关闭[TargetWindow]*/
        const val FLAG_ITEM_DISMISS = 0x0001
    }

    /**列表*/
    var _recyclerView: DslRecyclerView? = null

    /**渲染内容*/
    var renderAdapterAction: DslAdapter.() -> Unit = {}

    init {
        animationStyle = R.style.LibActionPopupAnimation
        popupLayoutId = R.layout.lib_popup_menu_layout
        autoOffset = true
        autoOffsetCenterInAnchor = true

        minHorizontalOffset = 40 * dpi

        //onDismiss
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        //init
        viewHolder._rv(R.id.lib_recycler_view)?.apply {
            _recyclerView = this
            renderDslAdapter {
                initRecyclerView(window, viewHolder, this@apply, this)

                //托管itemClick
                wrapItemClick(window)
            }
        }
        super.initLayout(window, viewHolder)

        viewHolder.itemView.clipBoundsAnimator {
            //no op
        }
    }

    /**初始化列表*/
    open fun initRecyclerView(
        window: TargetWindow,
        viewHolder: DslViewHolder,
        recyclerView: DslRecyclerView,
        adapter: DslAdapter
    ) {
        renderAdapterAction(adapter)
    }

    /**自动销毁[window]*/
    fun DslAdapter.wrapItemClick(window: TargetWindow) {
        getDataList(false).forEach { item ->
            if (item.itemClick != null && item.itemFlag.have(FLAG_ITEM_DISMISS)) {
                val oldClick = item.itemClick
                item.itemClick = {
                    window.dismissWindow()
                    oldClick?.invoke(it)
                }
            }
        }
    }

}

/**Dsl*/
@DSL
fun Context.menuPopupWindow(anchor: View?, config: MenuPopupConfig.() -> Unit): TargetWindow {
    val popupConfig = MenuPopupConfig()
    popupConfig.anchor = anchor

    /*popupConfig.renderAdapterAction = {
        //设置内容
    }*/

    popupConfig.config()
    return popupConfig.show(this)
}