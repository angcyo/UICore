package com.angcyo.canvas.items.renderer

import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.widget.LinearLayout
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.SmartAssistant
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.PictureItem
import com.angcyo.canvas.items.PictureShapeItem

/**
 * [PictureDrawable]
 *
 * [com.angcyo.library.component.ScalePictureDrawable]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
abstract class PictureItemRenderer<T : PictureItem>(canvasView: ICanvasView) :
    DrawableItemRenderer<T>(canvasView) {

    override fun changeBounds(reason: Reason, block: RectF.() -> Unit): Boolean {
        return super.changeBounds(reason, block)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            val item = getRendererItem()
            if (item is PictureShapeItem) {
                if (item.shapePath is LinePath) {
                    //线段不支持任意比例缩放
                    return false
                }
            }
        }
        return super.isSupportControlPoint(type)
    }

    override fun isSupportSmartAssistant(type: Int): Boolean {
        val item = getRendererItem()
        if (item is PictureShapeItem) {
            val shapePath = item.shapePath
            if (shapePath is LinePath) {
                if (shapePath.orientation == LinearLayout.VERTICAL) {
                    //垂直的线, 不支持w调整
                    return type != SmartAssistant.SMART_TYPE_W
                } else {
                    //水平的线, 不支持h调整
                    return type != SmartAssistant.SMART_TYPE_H
                }
            }
        }
        return super.isSupportSmartAssistant(type)
    }

    override fun onChangeBoundsAfter(reason: Reason) {
        super.onChangeBoundsAfter(reason)
        /*getRendererItem()?.let {
            if (it is PictureShapeItem) {
                val path = it.shapePath
                if (path is LinePath) {
                    val bounds = getBounds()
                    if (path.orientation == LinearLayout.VERTICAL) {
                        val size = path.lineBounds.width()
                        //只能调整高度
                        bounds.adjustSize(size, bounds.height(), ADJUST_TYPE_LT)
                        path.initPath(getBounds().height())
                        it.updatePictureDrawable(true)
                    } else {
                        //只能调整宽度
                        val size = path.lineBounds.height()
                        //只能调整高度
                        bounds.adjustSize(bounds.width(), size, ADJUST_TYPE_LT)
                        path.initPath(getBounds().width())
                        it.updatePictureDrawable(true)
                    }
                }
            }
        }*/
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)
        /*getRendererItem()?.let {
            if (it is PictureShapeItem && !oldBounds.isNoSize() && oldBounds.isSizeChanged(getBounds())) {
                it.shapePath?.apply {
                    if (this is LinePath) {
                        //no
                    } else {
                        val scaleX = getBounds().width() / oldBounds.width()
                        val scaleY = getBounds().height() / oldBounds.height()
                        if (scaleX != 1f || scaleY != 1f) {
                            val matrix = Matrix()
                            matrix.postScale(
                                scaleX,
                                scaleY,
                                it.shapeBounds.left,
                                it.shapeBounds.top
                            )
                            transform(matrix)
                            it.updatePictureDrawable(true)
                        }
                    }
                }
            }
        }*/
    }

    //<editor-fold desc="渲染操作方法">

    /**直接更新[drawable]*/
    fun updateItemDrawable(
        drawable: Drawable?,
        holdData: Map<String, Any?>? = null,
        keepBounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem() ?: return
        val oldValue = item.drawable

        if (oldValue == drawable) {
            return
        }

        val oldBounds = RectF(getBounds())
        val oldData = item.holdData

        if (keepBounds != null) {
            item.holdData = holdData
            item.drawable = drawable
            changeBounds {
                set(keepBounds)
            }
        } else {
            item.holdData = holdData
        }

        onRendererItemUpdate()//

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateItemDrawable(oldValue, oldData, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemDrawable(drawable, holdData, newBounds, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="渲染操作方法">
}