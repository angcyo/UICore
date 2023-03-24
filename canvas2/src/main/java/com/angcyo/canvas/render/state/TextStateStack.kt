package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.data.TextProperty
import com.angcyo.canvas.render.element.TextElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.element

/**
 * 文本状态存储
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
open class TextStateStack : PropertyStateStack(), IStateStack {

    /**保存的文本属性数据*/
    var textProperty: TextProperty? = null

    override fun saveState(renderer: BaseRenderer, delegate: CanvasRenderDelegate?) {
        super.saveState(renderer, delegate)
        textProperty = renderer.element<TextElement>()?.textProperty?.copy()
    }

    override fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        textProperty?.let {
            renderer.element<TextElement>()?.textProperty = it
        }
        super.restoreState(renderer, reason, strategy, delegate)
        renderer.requestUpdateDrawableAndPropertyFlag(reason, delegate)
    }
}