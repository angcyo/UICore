package com.angcyo.canvas.core.renderer

import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.utils.CanvasConstant

/**
 * [com.angcyo.canvas.core.renderer.SelectGroupRenderer]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/09
 */
open class SelectGroupItem : BaseItem() {

    init {
        itemLayerName = "Group"
        dataType = CanvasConstant.DATA_TYPE_GROUP
        dataMode = CanvasConstant.DATA_MODE_DITHERING
    }

}