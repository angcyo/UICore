package com.angcyo.gcode

import android.graphics.PointF
import com.angcyo.library.annotation.MM

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/10/23
 */
data class CollectPoint(
    /**
     * 这条线段上的关键点集合
     * 首 + 折点 + 折点 + 折点 + ... + 尾
     * */
    @MM
    val pointList: MutableList<PointF> = mutableListOf(),
)

/**转换成二维数组*/
fun List<CollectPoint>.toPointArray(): ArrayList<ArrayList<Double>> {
    val result = arrayListOf<ArrayList<Double>>()
    forEach { collectPoint ->
        val pointList = collectPoint.pointList
        val size = pointList.size
        if (size > 0) {
            val array = arrayListOf<Double>()
            for (i in 0 until size) {
                val point = pointList[i]
                array.add(point.x.toDouble())
                array.add(point.y.toDouble())
            }
            result.add(array)
        }
    }
    return result
}

/**反转*/
fun ArrayList<ArrayList<Double>>.toCollectPointList(): List<CollectPoint> {
    val result = mutableListOf<CollectPoint>()
    forEach { array ->
        val size = array.size
        if (size > 1) {
            val collectPoint = CollectPoint()
            for (i in 0 until size step 2) {
                collectPoint.pointList.add(PointF(array[i].toFloat(), array[i + 1].toFloat()))
            }
            result.add(collectPoint)
        }
    }
    return result
}