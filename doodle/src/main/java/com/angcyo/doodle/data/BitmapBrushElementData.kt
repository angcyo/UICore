package com.angcyo.doodle.data

import com.angcyo.doodle.info.PathSampleInfo

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class BitmapBrushElementData : BrushElementData() {
    /**路径采样信息*/
    val pathSampleInfoList = mutableListOf<PathSampleInfo>()
}