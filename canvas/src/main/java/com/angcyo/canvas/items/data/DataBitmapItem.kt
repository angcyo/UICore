package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.gcode.GCodeDrawable
import com.angcyo.library.annotation.Pixel

/**
 * 图片数据item
 * [com.angcyo.canvas.utils.CanvasConstant.DATA_TYPE_BITMAP]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class DataBitmapItem(bean: CanvasProjectItemBean) : DataItem(bean) {

    //region ---属性---

    /**原始的图片, 未修改前的数据*/
    var originBitmap: Bitmap? = null

    /**扭曲后的图片,
     * 如果有扭曲后的图片, 则图片算法/剪切都应该在此基础上进行*/
    var meshBitmap: Bitmap? = null

    /**算法修改后的图片*/
    var modifyBitmap: Bitmap? = null

    /**转成GCode后的Drawable*/
    var gCodeDrawable: GCodeDrawable? = null

    /**返回图片算法, 需要处理在什么图片上*/
    val operateBitmap: Bitmap?
        get() = if (dataBean.isMesh) {
            meshBitmap
        } else {
            originBitmap
        }

    //endregion ---属性---

    //region ---方法---

    /**
     * 更新原始图片, 比如裁剪后/扭曲后
     * 处理剪切图片, 剪切图片使用原图
     * 剪切完之后, 替换原图, 并且使用相同算法处理剪切后的图片(除GCode)
     * [origin] 剪切后的图片base64
     * [filter] 剪切后图片过滤的图片base64
     * [imageFilter] 剪切后图片过滤的算法那
     * */
    fun updateBitmapOriginal(
        origin: String?,
        filter: String?,
        imageFilter: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val oldImageFilter = dataBean.imageFilter
        val oldOrigin = dataBean.imageOriginal
        val oldSrc = dataBean.src

        val newOrigin = origin
        val newSrc = filter
        val newImageFilter = imageFilter

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.imageOriginal = oldOrigin
            dataBean.src = oldSrc
            dataBean.imageFilter = oldImageFilter

            updateRenderItem(renderer)
        }) {
            dataBean.imageOriginal = newOrigin
            dataBean.src = newSrc
            dataBean.imageFilter = newImageFilter

            updateRenderItem(renderer)
        }
    }

    /**图片扭曲
     * [image] 扭曲后, 并且应用了算法的图片base64
     * [imageFilter] 算法类型
     * */
    fun updateBitmapMesh(
        image: String?,
        imageFilter: Int,
        meshShale: String?,
        @Pixel
        minDiameter: Float,
        @Pixel
        maxDiameter: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val oldImageFilter = dataBean.imageFilter
        val oldImage = dataBean.src
        val oldIsMesh = dataBean.isMesh
        val oldMeshShape = dataBean.meshShape
        val oldMinDiameter = dataBean.minDiameter
        val oldMaxDiameter = dataBean.maxDiameter

        dataBean.imageFilter = imageFilter
        dataBean.src = image

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.src = oldImage
            dataBean.imageFilter = oldImageFilter

            dataBean.isMesh = oldIsMesh
            dataBean.meshShape = oldMeshShape
            dataBean.minDiameter = oldMinDiameter
            dataBean.maxDiameter = oldMaxDiameter

            updateRenderItem(renderer)
        }) {
            dataBean.src = image
            dataBean.imageFilter = imageFilter

            dataBean.isMesh = true
            dataBean.meshShape = meshShale
            dataBean.minDiameter = MM_UNIT.convertPixelToValue(minDiameter)
            dataBean.maxDiameter = MM_UNIT.convertPixelToValue(maxDiameter)

            updateRenderItem(renderer)
        }
    }

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

        /*if (mode == CanvasConstant.DATA_MODE_GCODE) {
            //GCode数据放这里
            dataBean.data = src
            dataBean.src = null
        }*/

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