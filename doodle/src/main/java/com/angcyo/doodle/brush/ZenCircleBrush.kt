package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenCircleBrushElement
import com.angcyo.doodle.info.PathSampleInfo
import com.angcyo.library.ex.*

/** 在曲线路径上, 绘制无数个半径不等的圆, 达到笔锋效果
 *
 * 通过无限多的[addCircle]实现的笔锋效果, 性能可能差一点
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenCircleBrush : BasePathSampleBrush() {

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        return ZenCircleBrushElement(BrushElementData())
    }

    override fun onPathSample(pathSampleInfo: PathSampleInfo) {
        brushElement?.brushElementData?.brushPath?.apply {
            addCircle(
                pathSampleInfo.x,
                pathSampleInfo.y,
                pathSampleInfo.width / 2,
                Path.Direction.CW
            )
        }
    }
}