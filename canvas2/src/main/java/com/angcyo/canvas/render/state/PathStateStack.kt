package com.angcyo.canvas.render.state

import android.graphics.Path
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.element.PathElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.renderElement

/**
 * 路径的状态存储
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/16
 */
open class PathStateStack(val renderer: BaseRenderer) : PropertyStateStack() {

    protected val pathElement: PathElement?
        get() {
            val element = renderer.renderElement
            if (element is PathElement) {
                return element
            }
            return null
        }

    var pathList: List<Path>? = null

    override fun saveState(renderer: BaseRenderer) {
        super.saveState(renderer)
        pathList = pathElement?.pathList
    }

    override fun restoreState(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        pathElement?.pathList = pathList
        super.restoreState(reason, strategy, delegate)
    }

}