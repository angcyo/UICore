package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.gcode.GCodeDrawable
import com.angcyo.library.annotation.Pixel

/**
 * 图片数据item
 * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_BITMAP]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class DataBitmapItem(bean: ItemDataBean) : DataItem(bean) {

    //region ---属性---

    /**原始的图片, 未修改前的数据*/
    var originBitmap: Bitmap? = null

    /**算法修改后的图片*/
    var modifyBitmap: Bitmap? = null

    /**转成GCode后的Drawable*/
    var gCodeDrawable: GCodeDrawable? = null

    //endregion ---属性---

    //region ---方法---

    /**更新图片模式
     * [src] 可以是修改后的图片base64数据, 也可以是GCode数据
     * [mode] 数据模式
     * */
    fun updateBitmapByMode(
        src: String?,
        mode: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val oldMode = dataBean.imageFilter
        val oldSrc = dataBean.src

        dataBean.imageFilter = mode
        dataBean.src = src

        if (mode == CanvasConstant.DATA_MODE_GCODE) {
            //GCode数据放这里
            dataBean.data = src
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            renderer.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateBitmapByMode(oldSrc, oldMode, renderer, Strategy.undo)
                }

                override fun runRedo() {
                    updateBitmapByMode(src, mode, renderer, Strategy.redo)
                }
            })
        }

        //更新
        updateRenderItem(renderer)
    }

    /**多了2个指定宽高的参数*/
    fun updateBitmapByMode(
        src: String?,
        mode: Int,
        renderer: DataItemRenderer,
        @Pixel width: Float, //更新到的宽度
        @Pixel height: Float, //更新到的高度
        strategy: Strategy = Strategy.normal
    ) {
        val oldMode = dataBean.imageFilter
        val oldSrc = dataBean.src
        val oldWidth = renderer.getBounds().width()
        val oldHeight = renderer.getBounds().height()

        dataBean.imageFilter = mode
        dataBean.src = src

        if (mode == CanvasConstant.DATA_MODE_GCODE) {
            //GCode数据放这里
            dataBean.data = src
        }

        //bounds
        dataBean.updateScale(width, height)

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            renderer.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateBitmapByMode(
                        oldSrc,
                        oldMode,
                        renderer,
                        oldWidth,
                        oldHeight,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateBitmapByMode(src, mode, renderer, width, height, Strategy.redo)
                }
            })
        }

        //更新
        updateRenderItem(renderer)
    }

    //endregion ---方法---


}