package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean

/**
 * 真实数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/10
 */
class RawGraphicsParser : IGraphicsParser {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_RAW) {
            return DataItem(bean)
        }
        return super.parse(bean, canvasView)
    }

}