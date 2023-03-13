package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.element.TextElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.renderElement

/**
 * 文本状态存储
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
open class TextStateStack(val renderer: BaseRenderer) : PropertyStateStack(), IStateStack {

    protected val element: TextElement?
        get() {
            val element = renderer.renderElement
            if (element is TextElement) {
                return element
            }
            return null
        }

    /**保存的文本属性数据*/
    var textProperty: TextProperty? = null

    init {
        saveState(renderer)
    }

    override fun saveState(renderer: BaseRenderer) {
        super.saveState(renderer)
        textProperty = element?.textProperty?.copy()
    }

    override fun restoreState(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        textProperty?.let {
            element?.textProperty = it
        }
        super.restoreState(reason, strategy, delegate)
        renderer.requestUpdateDrawableAndProperty(reason, delegate)
    }
}