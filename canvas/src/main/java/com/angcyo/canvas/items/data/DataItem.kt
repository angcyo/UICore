package com.angcyo.canvas.items.data

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.AnyThread
import androidx.core.graphics.scale
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.data.toTypeNameString
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.graphics.IEngraveProvider
import com.angcyo.canvas.graphics.flipEngraveBitmap
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.CanvasDataHandleOperate
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.unit.IValueUnit.Companion.MM_UNIT
import com.angcyo.library.unit.toPixel

/**
 * [com.angcyo.canvas.data.CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
open class DataItem(val dataBean: CanvasProjectItemBean) : BaseItem(), IEngraveProvider {

    companion object {

        /**默认笔的宽度 */
        const val DEFAULT_PAINT_WIDTH = 1f
    }

    //region ---属性---

    /**画笔*/
    val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = DEFAULT_PAINT_WIDTH
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**边框或者线(虚线和实线), 当前绘制的画笔宽度
     * [com.angcyo.canvas.graphics.PathGraphicsParser.createPathDrawable]*/
    var drawStrokeWidth: Float = 1f

    /**数据实际的[Drawable], 通常情况下会等于[renderDrawable]
     * 在绘制描边[Path]时, 为了能在坐标系缩放后同样得到相同的视觉效果.
     * */
    var dataDrawable: Drawable? = null

    /**用来渲染在界面上的[Drawable]
     * 通过改变此对象, 呈现出不同的可视图画
     * 可绘制的对象, 此对象不带旋转和缩放
     * 此对象为[null], 则不应该渲染在界面上?
     * [com.angcyo.canvas.graphics.GraphicsHelper.renderItemDataBean]
     * */
    var renderDrawable: Drawable? = null

    //endregion ---属性---

    init {
        itemLayerName = dataBean.name ?: dataBean.mtype.toTypeNameString()
    }

    override fun getItemScaleX(renderer: BaseItemRenderer<*>): Float {
        val width = MM_UNIT.convertValueToPixel(dataBean._width)
        return renderer.getBounds().width() / width
    }

    override fun getItemScaleY(renderer: BaseItemRenderer<*>): Float {
        val height = MM_UNIT.convertValueToPixel(dataBean._height)
        return renderer.getBounds().height() / height
    }

    /**当渲染的bounds改变时, 是否需要刷新[updateRenderItem]*/
    open fun needUpdateOfBoundsChanged(reason: Reason): Boolean {
        if (this is DataPathItem) {
            return reason.reason == Reason.REASON_USER && reason.flag.have(Reason.REASON_FLAG_BOUNDS)
        }
        if (this is DataBitmapItem) {
            if (dataBean.imageFilter == CanvasConstant.DATA_MODE_GCODE /*||
                dataBean.imageFilter == CanvasConstant.DATA_MODE_DITHERING*/ /*抖动数据也要实时更新*/) {
                //2022-12-12 抖动图预览的时候是灰度图, 所以不需要实时刷新数据
                return reason.reason == Reason.REASON_USER && reason.flag.have(Reason.REASON_FLAG_BOUNDS)
            }
        }
        return false
    }

    //---方法---

    /**重新更新需要渲染的界面数据*/
    @AnyThread
    fun updateRenderItem(
        renderer: DataItemRenderer,
        reason: Reason = Reason(flag = Reason.REASON_FLAG_STYLE)
    ) {
        //更新
        GraphicsHelper.updateRenderItem(renderer, dataBean, reason)
    }

    //---操作---

    /**获取需要绘制的[Drawable]*/
    override fun getDrawDrawable(renderParams: RenderParams): Drawable? {
        val drawable = if (renderParams.isFromRenderer) {
            renderDrawable
        } else {
            dataDrawable ?: renderDrawable
        }
        return drawable
    }

    /**更新笔的样式*/
    fun updatePaintStyle(
        style: Paint.Style,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.paintStyle.toPaintStyle()
        val new = style
        if (old == new) {
            return
        }

        clearPathFillCache()

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.paintStyle = old.toPaintStyleInt()
            updateRenderItem(renderer)
        }) {
            dataBean.paintStyle = new.toPaintStyleInt()
            updateRenderItem(renderer)
        }
    }

    /**更新可见性*/
    fun updateVisible(
        visible: Boolean,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.isVisible
        val new = visible
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.isVisible = old
            updateRenderItem(renderer, Reason.user)
        }) {
            dataBean.isVisible = new
            updateRenderItem(renderer, Reason.user)
        }
    }

    /**水平翻转切换*/
    fun toggleFlipX(renderer: DataItemRenderer, strategy: Strategy = Strategy.normal) {
        val old = dataBean.flipX
        val new = !dataBean._flipX

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.flipX = old
            updateRenderItem(renderer)
        }) {
            dataBean.flipX = new
            updateRenderItem(renderer)
        }
    }

    /**垂直翻转切换*/
    fun toggleFlipY(renderer: DataItemRenderer, strategy: Strategy = Strategy.normal) {
        val old = dataBean.flipY
        val new = !dataBean._flipY

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.flipY = old
            updateRenderItem(renderer)
        }) {
            dataBean.flipY = new
            updateRenderItem(renderer)
        }
    }

    /**路径填充*/
    fun updatePathFill(
        gcodeFillStep: Float,
        gcodeFillAngle: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.gcodeFillStep
        val oldAngle = dataBean.gcodeFillAngle
        val new = gcodeFillStep
        val newAngle = gcodeFillAngle

        if (old == new && oldAngle == newAngle) {
            return
        }

        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.gcodeFillStep = old
            dataBean.gcodeFillAngle = oldAngle

            //提前处理
            if (this is DataPathItem) {
                pathFillToGCode(dataPathList, itemPaint)?.let {
                    pathFillGCodePath = it
                }
            }
            updateRenderItem(renderer)
        }) {
            dataBean.gcodeFillStep = new
            dataBean.gcodeFillAngle = newAngle

            //提前处理
            if (this is DataPathItem) {
                pathFillToGCode(dataPathList, itemPaint)?.let {
                    pathFillGCodePath = it
                }
            }
            updateRenderItem(renderer)
        }
    }

    /**清理掉路径填充的缓存*/
    fun clearPathFillCache() {
        if (this is DataPathItem) {
            pathFillGCodePath = null
        }
    }

    /**转换成对应的GCode[Path]*/
    fun pathFillToGCode(pathList: List<Path>, paint: Paint): Path? {
        if (this is DataPathItem) {
            val bean = dataBean
            val dataPathList = pathList.toList()
            val renderBounds = acquireTempRectF()
            bean.updateToRenderBounds(renderBounds)
            val gcode = CanvasDataHandleOperate.pathToGCode(
                dataPathList,
                renderBounds,
                0f,
                style = if (paint.style == Paint.Style.FILL) Paint.Style.FILL else Paint.Style.FILL_AND_STROKE,
                fillPathStep = bean.gcodeFillStep.toPixel(),
                fillAngle = bean.gcodeFillAngle
            ).readText()
            renderBounds.release()
            val gCodeDrawable = GCodeHelper.parseGCode(gcode, paint)
            return gCodeDrawable?.gCodePath
        }
        return null
    }

    /**栅格化*/
    fun itemRasterize(renderer: DataItemRenderer, strategy: Strategy = Strategy.normal) {
        if (dataBean.mtype != CanvasConstant.DATA_TYPE_BITMAP) {
            val oldType = dataBean.mtype
            renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
                dataBean.mtype = oldType
                updateRenderItem(renderer)
                renderer.canvasView.dispatchItemTypeChanged(renderer)
            }) {
                dataBean.mtype = CanvasConstant.DATA_TYPE_BITMAP
                dataBean.imageFilter = CanvasConstant.DATA_MODE_BLACK_WHITE
                dataBean.imageOriginal = getEngraveBitmap(
                    RenderParams(
                        isFromRenderer = true,
                        renderOrigin = true
                    )
                )?.toBase64Data()
                updateRenderItem(renderer)
                renderer.canvasView.dispatchItemTypeChanged(renderer)
            }
        }
    }

    //<editor-fold desc="IEngraveProvider">

    override fun getEngraveRenderer(): IItemRenderer<*>? = null

    override fun getEngraveDataItem(): DataItem? = this

    /**
     * 这个方法从来处理[DataBitmapItem]
     * [getEngraveBitmap]*/
    fun _getEngraveBitmap(renderParams: RenderParams): Bitmap? {
        val item = this
        val bitmap = if (item is DataBitmapItem) {
            if (item.dataBean.mtype == CanvasConstant.DATA_TYPE_BITMAP &&
                item.dataBean.imageFilter == CanvasConstant.DATA_MODE_DITHERING
            ) {
                //如果是抖动数据, 则返回的依旧是原始图片
                item.originBitmap?.flipEngraveBitmap(item.dataBean)
            } else {
                //其他
                item.modifyBitmap ?: item.originBitmap?.flipEngraveBitmap(item.dataBean)
            }
        } else {
            null
        }

        //---
        val rotate = _rotate
        if (bitmap != null && !renderParams.renderOrigin) {
            //这里需要处理缩放和旋转
            val bounds = getEngraveBounds()
            val width = bounds.width().toInt()
            val height = bounds.height().toInt()
            val scaleBitmap = bitmap.scale(width, height)
            return scaleBitmap.rotate(rotate)
        }
        return bitmap
    }

    /**
     * [com.angcyo.canvas.items.data.DataItem.getEngraveBitmap]
     * [com.angcyo.canvas.items.data.DataItemRenderer.getEngraveBitmap]
     * */
    override fun getEngraveBitmap(renderParams: RenderParams): Bitmap? {
        val bitmap = _getEngraveBitmap(renderParams)
        if (bitmap != null) {
            return bitmap
        }
        val rotate = if (renderParams.renderOrigin) 0f else _rotate
        getDrawDrawable(renderParams)?.let { drawable ->
            if (renderParams.renderOrigin) {
                return drawable.toBitmap()
            } else {
                val renderBounds = getEngraveBounds()
                val renderWidth = renderBounds.width()
                val renderHeight = renderBounds.height()

                val rotateBounds = getEngraveRotateBounds()
                val width = rotateBounds.width()
                val height = rotateBounds.height()

                /*val result = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                    withRotation(rotate, width / 2, height / 2) {
                        withTranslation( width / 2 - renderWidth / 2, height / 2 - renderHeight / 2 ) {
                            drawable.setBounds(
                                renderBounds.left.toInt(),
                                renderBounds.top.toInt(),
                                renderBounds.right.toInt(),
                                renderBounds.bottom.toInt()
                            )
                            drawable.draw(this)
                        }
                    }
                })
                return result.toBitmap()
                */

                val result = bitmapCanvas(width.toInt(), height.toInt()) {
                    rotate(rotate, width / 2, height / 2)
                    translate(width / 2 - renderWidth / 2, height / 2 - renderHeight / 2)
                    drawable.setBounds(
                        0,
                        0,
                        renderBounds.width().toInt(),
                        renderBounds.height().toInt()
                    )
                    drawable.draw(this)
                }
                return result
            }
        }
        return null
    }

    override fun getEngraveBounds(): RectF {
        val rect = RectF()
        dataBean.updateToRenderBounds(rect)
        return rect
    }

    override fun getEngraveRotateBounds(): RectF {
        val rect = RectF()
        getEngraveBounds().rotate(_rotate, result = rect)
        return rect
    }

    //</editor-fold desc="IEngraveProvider">
}