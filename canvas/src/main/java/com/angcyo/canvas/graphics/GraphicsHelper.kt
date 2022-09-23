package com.angcyo.canvas.graphics

import androidx.annotation.MainThread
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.graphics.PathGraphicsParser.Companion.MIN_PATH_SIZE
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.http.rx.doMain
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.MM
import com.angcyo.library.ex.abs

/**
 * 用来解析[ItemDataBean]
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
        }
    }

    @MM
    var _lastLeft = 0f

    @MM
    var _lastTop = 0f

    var _lastTopIndex = 0

    @MM
    const val POSITION_STEP = 5f

    @MM
    const val POSITION_CUT = 30f

    /**分配一个位置*/
    fun assignLocation(bean: ItemDataBean) {
        if (_lastLeft > POSITION_CUT) {
            //换行
            _lastLeft = 0f
            _lastTopIndex++
            _lastTop = POSITION_STEP * _lastTopIndex
        }
        if (_lastTop > POSITION_CUT) {
            _lastTopIndex = 0
        }
        _lastLeft += POSITION_STEP
        _lastTop += POSITION_STEP
        bean.left = _lastLeft
        bean.top = _lastTop
    }

    /**开始解析
     * 更具[bean]解析出一个可以用来渲染的[BaseItem]
     * */
    @CallPoint
    fun parseItemFrom(bean: ItemDataBean): DataItem? {
        initParser()
        var result: DataItem? = null
        for (parser in _parserList) {
            result = parser.parse(bean)
            if (result != null) {
                break
            }
        }
        if (result != null) {
            if (bean.width == 0f && bean.height == 0f) {
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
    fun renderItemDataBean(
        canvasView: ICanvasView,
        bean: ItemDataBean,
        selected: Boolean,
        assignLocation: Boolean = false
    ): DataItemRenderer? {
        if (assignLocation) {
            assignLocation(bean)
        }
        val item = parseItemFrom(bean) ?: return null
        val renderer = DataItemRenderer(canvasView)
        renderer.setRendererRenderItem(item)
        doMain {
            updateRendererProperty(renderer, bean)
            (canvasView as? CanvasDelegate)?.apply {
                addItemRenderer(renderer, Strategy.normal)
                if (selected) {
                    selectedItem(renderer)
                }
            }
        }
        return renderer
    }

    /**添加一个元素用来渲染指定的数据
     * [renderItemDataBean]*/
    @CallPoint
    fun addRenderItemDataBean(canvasView: ICanvasView, bean: ItemDataBean) =
        renderItemDataBean(canvasView, bean, true, true)

    /**更新一个新的渲染[DataItem]*/
    @CallPoint
    fun updateRenderItem(renderer: DataItemRenderer, bean: ItemDataBean) {
        val item = parseItemFrom(bean) ?: return
        updateRenderItem(renderer, item)
    }

    /**更新一个新的渲染[DataItem]*/
    @CallPoint
    fun updateRenderItem(renderer: DataItemRenderer, item: DataItem) {
        //更新渲染item
        renderer.setRendererRenderItem(item)
        //更新渲染的坐标/旋转信息
        updateRendererProperty(renderer, item.dataBean)
    }

    //---

    /**根据[bean]提供的参数, 更新[renderer]相关属性*/
    @MainThread
    fun updateRendererProperty(renderer: BaseItemRenderer<*>, bean: ItemDataBean) {
        //可见性
        renderer._visible = bean.isVisible

        //角度
        renderer.rotate = bean.angle

        //bounds
        renderer.changeBoundsAction(Reason(Reason.REASON_CODE, true, Reason.REASON_FLAG_BOUNDS)) {
            bean.setRenderBounds(this)
            if (renderer.isLineShape() && height().abs() < MIN_PATH_SIZE) {
                //如果是线条, 则高度强制使用1像素
                bottom = top + MIN_PATH_SIZE
            }
        }
    }

    // ---
/*
    */
    /**转换一个[path]*//*
    fun transformPath(renderer: DataItemRenderer, path: Path, result: Path): Path {
        val matrix = Matrix()
        val pathBounds = path.computePathBounds()

        //平移到左上角0,0, 然后缩放, 旋转
        matrix.setTranslate(-pathBounds.left, -pathBounds.top)

        //缩放
        val bounds = renderer.getBounds()
        matrix.postScale(bounds.width() / itemWidth, bounds.height() / itemHeight, 0f, 0f)

        //旋转到指定角度
        matrix.postRotate(renderer.rotate, bounds.width() / 2f, bounds.height() / 2f)

        //平移到指定位置
        matrix.postTranslate(bounds.left, bounds.top)

        //
        path.transform(matrix, result)
        return result
    }

    */
    /**获取数据变换的矩阵*//*
    fun getMatrix(renderer: DataItemRenderer): Matrix {
        val matrix = Matrix()
        val bounds = renderer.getBounds()
        //缩放到指定大小
        matrix.setScale(bounds.width() / itemWidth, bounds.height() / itemHeight, 0f, 0f)
        //旋转到指定角度
        matrix.postRotate(renderer.rotate, bounds.width() / 2f, bounds.height() / 2f)
        //平移到指定位置
        matrix.postTranslate(bounds.left, bounds.top)
        return matrix
    }*/

}