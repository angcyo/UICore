package com.angcyo.doodle.brush

import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BitmapBrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.BitmapBrushElement
import com.angcyo.doodle.info.PathSampleInfo

/**
 * 图片画笔手势收集
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapBrush : BasePathSampleBrush() {

    init {
        pathSampleStep = 3f
    }

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement? {
        return BitmapBrushElement(BitmapBrushElementData())
    }

    /**收集所有的采样数据*/
    override fun onPathSample(pathSampleInfo: PathSampleInfo) {
        (brushElement as? BitmapBrushElement)?.data?.pathSampleInfoList?.add(pathSampleInfo)
    }
}