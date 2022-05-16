package com.angcyo.dialog.popup

import androidx.annotation.LayoutRes
import com.angcyo.dialog.R
import com.angcyo.dialog.TargetWindow
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.replace
import com.angcyo.widget.DslViewHolder

/**
 * 带阴影的[AnchorPopupConfig]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/16
 */
open class ShadowAnchorPopupConfig : AnchorPopupConfig() {

    /**内容布局id*/
    @LayoutRes
    var contentLayoutId: Int = -1

    init {
        layoutId = R.layout.lib_popup_shadow_anchor_layout
        triangleMinMargin = 22 * dpi
        minHorizontalOffset = 0
        yoff = 2 * dpi
    }

    override fun initLayout(window: TargetWindow, viewHolder: DslViewHolder) {
        viewHolder.group(R.id.lib_triangle_content_layout)?.replace(contentLayoutId)
        initContentLayout(window, viewHolder)
        super.initLayout(window, viewHolder)
    }

    /**重写此方法, 初始化内容布局*/
    open fun initContentLayout(window: TargetWindow, viewHolder: DslViewHolder) {

    }

}