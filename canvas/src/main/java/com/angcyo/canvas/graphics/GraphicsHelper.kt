package com.angcyo.canvas.graphics

import android.graphics.RectF
import androidx.annotation.AnyThread
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.graphics.PathGraphicsParser.Companion.MIN_PATH_SIZE
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.LibHawkKeys
import com.angcyo.library.ex._string
import com.angcyo.library.ex.abs
import com.angcyo.library.toastQQ

/**
 * 用来解析[CanvasProjectItemBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
object GraphicsHelper {

    /**解析器集合*/
    val _parserList = mutableListOf<IGraphicsParser>()

    /**初始化解析器*/
    fun initParser() {
        if (_parserList.isEmpty()) {
            _parserList.add(BitmapGraphicsParser())
            _parserList.add(TextGraphicsParser())
            _parserList.add(CodeGraphicsParser())
            _parserList.add(LineGraphicsParser())
            _parserList.add(OvalGraphicsParser())
            _parserList.add(RectGraphicsParser())
            _parserList.add(PolygonGraphicsParser())
            _parserList.add(PentagramGraphicsParser())
            _parserList.add(LoveGraphicsParser())
            _parserList.add(SvgGraphicsParser())
            _parserList.add(GCodeGraphicsParser())
            _parserList.add(PathGraphicsParser())
            _parserList.add(RawGraphicsParser())
        }
    }

    //---位置分配---

    /**如果配置了此属性, 则分配位置的时候, 会在此矩形的中心*/
    @Pixel
    var assignLocationBounds: RectF? = null

    /**最小位置分配, 应该为设备最佳预览范围的左上角
     * [com.angcyo.engrave.model.FscDeviceModel.initDevice]*/
    @MM
    var _minLeft = 0f

    @MM
    var _minTop = 0f

    /**最后一次分配的坐标*/
    @MM
    var _lastLeft = 0f

    @MM
    var _lastTop = 0f

    //

    var _lastTopIndex = 0

    @MM
    const val POSITION_STEP = 5f

    //当位置增加到此值时, 进行换行

    /**[com.angcyo.engrave.EngraveProductLayoutHelper.bindCanvasView]*/
    @MM
    var POSITION_CUT_LEFT = 30f

    @MM
    var POSITION_CUT_TOP = 30f * 5

    /**分配一个位置, 和智能调整缩放*/
    fun assignLocation(canvasViewBox: CanvasViewBox, bean: CanvasProjectItemBean) {
        if (_lastLeft > POSITION_CUT_LEFT) {
            //换行
            _lastLeft = 0f
            _lastTopIndex++
            _lastTop = POSITION_STEP * _lastTopIndex
        }
        if (_lastTop > POSITION_CUT_TOP) {
            _lastTopIndex = 0
        }
        _lastLeft += POSITION_STEP
        _lastTop += POSITION_STEP

        //2022-11-29 默认在中心位置添加元素
        val bounds = assignLocationBounds
        if (bounds == null) {
            bean.left = _minLeft + _lastLeft
            bean.top = _minTop + _lastTop
        } else {
            bean.left = bounds.centerX().toMm() - bean._width / 2
            bean.top = bounds.centerY().toMm() - bean._height / 2
        }
/*2022-12-26 移除缩放, 1:1显示
        //调整可视化的缩放比例
        val visualRect = canvasViewBox.getVisualRect()
        if (!visualRect.isEmpty) {
            val maxWidth = visualRect.width() * 3 / 4
            val maxHeight = visualRect.height() * 3 / 4

            val width = bean.width.toPixel()
            val height = bean.height.toPixel()

            if (width > 0 && height > 0) {
                val targetWidth: Float
                val targetHeight: Float

                limitMaxWidthHeight(width, height, maxWidth, maxHeight).apply {
                    targetWidth = this[0]
                    targetHeight = this[1]
                }

                bean.scaleX = targetWidth / width
                bean.scaleY = targetHeight / height
            }
        }*/
    }

    /**当界面关闭后, 恢复分配的默认位置*/
    fun restoreLocation() {
        _lastLeft = 0f
        _lastTop = 0f
        _lastTopIndex = 0
    }

    //region ---ItemDataBean解析---

    /**开始解析, 可能会有耗时操作, 请在子线程中处理
     * 根据[bean]解析出一个可以用来渲染的[BaseItem]
     * */
    @CallPoint
    @AnyThread
    fun parseRenderItemFrom(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        initParser()
        var result: DataItem? = null
        for (parser in _parserList) {
            result = parser.parse(bean, canvasView)
            if (result != null) {
                break
            }
        }
        if (result != null) {
            if (bean._width == 0f && bean._height == 0f) {
                L.e("请注意,添加了一个无大小的[Item].${bean}")
            }
        }
        return result
    }

    /**渲染一个[bean]
     * [selected] 是否要选中
     * [assignLocation] 是否需要分配一个位置
     * */
    @CallPoint
    @AnyThread
    fun renderItemDataBean(
        canvasView: ICanvasView,
        bean: CanvasProjectItemBean,
        selected: Boolean,
        assignLocation: Boolean = false,
        strategy: Strategy = Strategy.normal
    ): DataItemRenderer? {
        val item = parseRenderItemFrom(bean, canvasView) ?: return null
        val renderer = DataItemRenderer(canvasView)
        renderer.setRendererRenderItem(item)
        if (assignLocation) {
            //更新位置和可视的缩放比例
            assignLocation(canvasView.getCanvasViewBox(), bean)
        }
        updateRendererProperty(renderer, bean)
        if (canvasView is CanvasDelegate) {
            canvasView.addItemRenderer(renderer, strategy)
            if (selected) {
                canvasView.selectedItem(renderer)
            }
        }
        return renderer
    }

    /**渲染一组数据*/
    @CallPoint
    @AnyThread
    fun renderItemDataBeanList(
        canvasView: ICanvasView,
        beanList: List<CanvasProjectItemBean>,
        selected: Boolean,
        strategy: Strategy
    ): List<DataItemRenderer> {
        val result = mutableListOf<DataItemRenderer>()
        beanList.forEach { bean ->
            val item = parseRenderItemFrom(bean, canvasView)
            item?.let {
                val renderer = DataItemRenderer(canvasView)
                renderer.setRendererRenderItem(item)
                result.add(renderer)

                //更新坐标
                updateRendererProperty(renderer, bean)
            }
        }
        if (result.isNotEmpty()) {
            (canvasView as? CanvasDelegate)?.apply {
                addItemRenderer(result, strategy)
                if (selected) {
                    selectGroupRenderer.selectedRendererList(result, Strategy.preview)
                }
            }
        }
        return result
    }

    /**添加一个元素用来渲染指定的数据
     * [renderItemDataBean] 此方法的缩短写法*/
    @CallPoint
    @AnyThread
    fun addRenderItemDataBean(
        canvasView: ICanvasView?,
        bean: CanvasProjectItemBean?
    ): DataItemRenderer? {
        return if (bean == null || canvasView == null) {
            null
        } else {
            if (canvasView is CanvasDelegate) {
                if (canvasView.itemRendererCount > LibHawkKeys.canvasRenderMaxCount) {
                    toastQQ(_string(R.string.canvas_item_outof_limit))
                    return null
                }
            }
            renderItemDataBean(canvasView, bean, true, true)
        }
    }

    /**更新一个新的渲染[DataItem], 重新渲染数据.
     * 在后台线程运行, 提高渲染效率
     * */
    @CallPoint
    @AnyThread
    fun updateRenderItem(renderer: DataItemRenderer, bean: CanvasProjectItemBean, reason: Reason) {
        doBack(true) {
            L.w("更新渲染数据:[${bean.index}]${reason}")
            renderer.renderItemDataChanged(reason)
            val item = parseRenderItemFrom(bean, renderer.canvasView) ?: return@doBack
            updateRenderItem(renderer, item)
        }
    }

    /**更新[renderer]的[DataItem]*/
    @CallPoint
    @AnyThread
    fun updateRenderItem(renderer: DataItemRenderer, item: DataItem) {
        //更新渲染item
        renderer.setRendererRenderItem(item)
        //更新渲染的坐标/旋转信息
        updateRendererProperty(renderer, item.dataBean)
    }

    //endregion ---ItemDataBean解析---

    /**根据[bean]提供的参数, 更新[renderer]相关属性*/
    @AnyThread
    fun updateRendererProperty(renderer: BaseItemRenderer<*>, bean: CanvasProjectItemBean) {
        //可见性
        renderer._visible = bean.isVisible

        //角度
        renderer.rotate = bean.angle

        //bounds
        renderer.changeBoundsAction(Reason(Reason.REASON_CODE, true, Reason.REASON_FLAG_BOUNDS)) {
            bean.updateToRenderBounds(this)
            if (renderer.isLineShape() && height().abs() < MIN_PATH_SIZE) {
                //如果是线条, 则高度强制使用1像素
                bottom = top + MIN_PATH_SIZE
            }
        }
    }

    //endregion ---ItemDataBean解析---
}