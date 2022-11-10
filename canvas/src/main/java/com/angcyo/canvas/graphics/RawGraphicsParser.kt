package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant

/**
 * 真实数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/10
 */
class RawGraphicsParser : IGraphicsParser {

    override fun parse(bean: CanvasProjectItemBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_RAW) {
            return DataItem(bean)
        }
        return super.parse(bean)
    }

}