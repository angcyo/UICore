package com.angcyo.drawable.base

import android.graphics.Canvas
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.OverridePoint
import com.angcyo.library.ex.nowTime

/**
 * 互不相干的item绘制, item支持进度, 支持动画延迟
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
abstract class BaseItemDrawable : AbsDslDrawable() {

    /**需要控制的[DrawItem]*/
    var items = mutableListOf<DrawItem>()

    /**是否需要动画, 自动累加[progress]值*/
    var loading: Boolean = false
        set(value) {
            field = value
            invalidateSelf()
            if (value) {
                _loadingStartTime = nowTime()
            }
        }

    /**动画步长*/
    var loadingStep: Int = 1

    //动画起始的时间, 用来计算延迟参数
    var _loadingStartTime = 0L

    override fun draw(canvas: Canvas) {
        if (items.isNotEmpty()) {
            //开始绘制

            items.forEachIndexed { index, drawItem ->
                onDrawItem(canvas, drawItem, index)
            }

            if (loading) {
                updateDrawItemProgress()
            }
        }
    }

    /**开始更新进度*/
    open fun updateDrawItemProgress() {
        val nowTime = nowTime()
        for (item in items) {
            if (nowTime - _loadingStartTime >= item.startDelay) {
                item.onUpdateProgress(loadingStep)
            }
        }
        invalidateSelf()
    }

    /**重写此方法, 绘制具体项*/
    @OverridePoint
    open fun onDrawItem(canvas: Canvas, drawItem: DrawItem, index: Int) {

    }

    /**绘制的数据结构*/
    open class DrawItem {

        /**当前的进度, [0~100]*/
        var progress: Int = 0

        /**动画开始的延迟时长, 毫秒*/
        var startDelay: Long = 0

        /**是否反向更新进度, 比如 0~100 然后 从 100~0*/
        var reverse: Boolean = true

        /**根据[reverse]调整的step*/
        var _progressStep: Int = 0

        @CallPoint
        open fun onUpdateProgress(step: Int) {
            if (_progressStep == 0) {
                _progressStep = step
            }
            if (progress >= 100) {
                //达到最大值
                if (reverse) {
                    _progressStep = -step
                } else {
                    progress = 0
                }
            } else if (progress <= 0) {
                //达到最小值
                if (reverse) {
                    _progressStep = +step
                } else {
                    progress = 100
                }
            }
            progress += _progressStep
            if (progress > 100) {
                progress = 100
            } else if (progress < 0) {
                progress = 0
            }
        }
    }

}