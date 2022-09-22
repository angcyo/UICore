package com.angcyo.canvas.graphics

import androidx.annotation.MainThread
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.DataItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.DataItemRenderer
import com.angcyo.http.rx.doMain
import com.angcyo.library.annotation.CallPoint

/**
 * 用来解析[ItemDataBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
object GraphicsHelper {

    /**解析器集合*/
    val _parserList = mutableListOf<IGraphicsParser>()

    fun initParser() {
        if (_parserList.isEmpty()) {
            _parserList.add(BitmapGraphicsParser())
            _parserList.add(TextGraphicsParser())
            _parserList.add(CodeGraphicsParser())
        }
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
        return result
    }

    /**渲染一个[bean]
     * [selected] 是否要选中*/
    @CallPoint
    fun renderItemData(
        canvasView: ICanvasView,
        bean: ItemDataBean,
        selected: Boolean = true
    ): DataItemRenderer? {
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
        renderer.changeBoundsAction {
            bean.setRenderBounds(this)
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