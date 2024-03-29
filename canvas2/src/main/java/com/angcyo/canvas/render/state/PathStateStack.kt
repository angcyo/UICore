package com.angcyo.canvas.render.state

import android.graphics.Path
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.canvas.core.Reason
import com.angcyo.canvas.render.element.PathElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.element
import com.angcyo.library.component.Strategy

/**
 * 路径的状态存储
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/16
 */
open class PathStateStack : PropertyStateStack() {

    var pathList: List<Path>? = null

    override fun saveState(renderer: BaseRenderer, delegate: CanvasRenderDelegate?) {
        super.saveState(renderer, delegate)
        pathList = renderer.element<PathElement>()?.pathList
    }

    override fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        renderer.element<PathElement>()?.pathList = pathList
        super.restoreState(renderer, reason, strategy, delegate)
        renderer.requestUpdatePropertyFlag(reason, delegate)
    }

}