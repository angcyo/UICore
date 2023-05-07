package com.angcyo.canvas.render.core

import android.graphics.Canvas
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.have
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
abstract class BaseRenderDispatch {

    //region---Core---

    private val _tempList = CopyOnWriteArrayList<IRenderer>()

    fun dispatchRender(canvas: Canvas, renderer: IRenderer, params: RenderParams) {
        _tempList.clear()
        _tempList.add(renderer)
        dispatchRender(canvas, _tempList, params)
    }

    /**分发渲染*/
    @CallPoint
    fun dispatchRender(
        canvas: Canvas,
        rendererList: List<IRenderer>,
        params: RenderParams
    ) {
        dispatchRenderBefore(canvas, rendererList, params)
        dispatchRenderOnInside(
            canvas,
            rendererList.filter { it.renderFlags.have(IRenderer.RENDERER_FLAG_ON_INSIDE) },
            params
        )
        dispatchRenderOnOutside(
            canvas,
            rendererList.filter { it.renderFlags.have(IRenderer.RENDERER_FLAG_ON_OUTSIDE) },
            params
        )
        dispatchRenderOnView(
            canvas,
            rendererList.filter { it.renderFlags.have(IRenderer.RENDERER_FLAG_ON_VIEW) },
            params
        )
    }

    /**[com.angcyo.canvas.render.core.IRenderer.renderBefore]*/
    protected fun dispatchRenderBefore(
        canvas: Canvas,
        list: List<IRenderer>,
        params: RenderParams
    ) {
        if (list.isEmpty()) return

        for (renderer in list) {
            renderer.renderBefore(canvas, params)
        }
    }

    /**[com.angcyo.canvas.render.core.IRenderer.renderOnInside]*/
    protected fun dispatchRenderOnInside(
        canvas: Canvas,
        list: List<IRenderer>,
        params: RenderParams
    ) {
        val renderViewBox = params.delegate?.renderViewBox ?: return
        if (list.isEmpty()) return

        val renderBounds = renderViewBox.renderBounds
        val originPoint = renderViewBox.getOriginPoint()

        //偏移到画布bounds
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            clipRect(0f, 0f, renderBounds.width(), renderBounds.height())//剪切画布
            //平移到画布原点
            translate(originPoint.x, originPoint.y)
            //---
            for (renderer in list) {
                canvas.withMatrix(renderViewBox.renderMatrix) {
                    renderer.renderOnInside(canvas, params)
                }
            }
        }
    }

    /**[com.angcyo.canvas.render.core.IRenderer.renderOnOutside]*/
    protected fun dispatchRenderOnOutside(
        canvas: Canvas,
        list: List<IRenderer>,
        params: RenderParams
    ) {
        val renderViewBox = params.delegate?.renderViewBox ?: return
        if (list.isEmpty()) return

        val renderBounds = renderViewBox.renderBounds

        //偏移到画布bounds
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            //---
            for (renderer in list) {
                if (renderer.renderFlags.have(IRenderer.RENDERER_FLAG_CLIP_RECT_OUTSIDE)) {
                    withSave {
                        clipRect(0f, 0f, renderBounds.width(), renderBounds.height())//剪切画布
                        renderer.renderOnOutside(canvas, params)
                    }
                } else {
                    renderer.renderOnOutside(canvas, params)
                }
            }
        }
    }

    /**[com.angcyo.canvas.render.core.IRenderer.renderOnView]*/
    protected fun dispatchRenderOnView(
        canvas: Canvas,
        list: List<IRenderer>,
        params: RenderParams
    ) {
        if (list.isEmpty()) return

        for (renderer in list) {
            renderer.renderOnView(canvas, params)
        }
    }

    //---

    fun renderBefore(canvas: Canvas, list: List<IRenderer>, params: RenderParams) {
        for (renderer in list) {
            renderer.renderBefore(canvas, params)
        }
    }

    fun renderOnView(canvas: Canvas, list: List<IRenderer>, params: RenderParams) {
        for (renderer in list) {
            if (renderer is BaseRenderer && !renderer.isVisible) {
                //不可见
                continue
            }
            if (!renderer.renderFlags.have(IRenderer.RENDERER_FLAG_ON_VIEW)) {
                continue
            }
            renderer.renderOnView(canvas, params)
        }
    }

    fun renderOnInside(canvas: Canvas, list: List<IRenderer>, params: RenderParams) {
        for (renderer in list) {
            if (renderer is BaseRenderer && !renderer.isVisible) {
                //不可见
                continue
            }
            if (!renderer.renderFlags.have(IRenderer.RENDERER_FLAG_ON_INSIDE)) {
                //不需要此绘制回调
                continue
            }
            if (renderer is BaseRenderer &&
                !renderer.isVisibleInRender(params.delegate, false, true)
            ) {
                //超出了绘制范围, 则不绘制
                continue
            }
            renderer.renderOnInside(canvas, params)
        }
    }

    fun renderOnOutside(canvas: Canvas, list: List<IRenderer>, params: RenderParams) {
        for (renderer in list) {
            if (renderer is BaseRenderer && !renderer.isVisible) {
                //不可见
                continue
            }
            if (!renderer.renderFlags.have(IRenderer.RENDERER_FLAG_ON_OUTSIDE)) {
                continue
            }
            renderer.renderOnOutside(canvas, params)
        }
    }

    //endregion---Core---
}