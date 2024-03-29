package com.angcyo.canvas.render.state

import android.graphics.Bitmap
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.element.BitmapElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.element
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.Strategy

/**
 * 图片状态存储, 用来恢复/重做
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/14
 */
open class BitmapStateStack : PropertyStateStack() {

    var operateBitmap: Bitmap? = null

    var renderBitmap: Bitmap? = null

    override fun saveState(renderer: BaseRenderer, delegate: CanvasRenderDelegate?) {
        super.saveState(renderer, delegate)
        operateBitmap = renderer.element<BitmapElement>()?.originBitmap
        renderBitmap = renderer.element<BitmapElement>()?.renderBitmap
    }

    override fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        renderer.element<BitmapElement>()?.restoreOriginBitmap(operateBitmap)
        renderer.element<BitmapElement>()?.renderBitmap = renderBitmap
        super.restoreState(renderer, reason, strategy, delegate)
        renderer.requestUpdatePropertyFlag(reason, delegate)
    }

}