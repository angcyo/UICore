package com.angcyo.widget.recycler

import android.graphics.Canvas
import android.view.View
import androidx.core.view.doOnLayout
import com.angcyo.behavior.refresh.RefreshEffectConfig
import com.angcyo.library.L
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.*
import com.angcyo.library.ex.mW
import com.angcyo.library.ex.offsetLeftTo

/**
 * 横向拉动偏移的边界效果
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

@Deprecated("左右滑动容易冲突")
class HorizontalOverEdgeEffect(view: View, val isRight: Boolean = false /*是否是右边*/) :
    BaseOverEdgeEffect(view) {

    /**目标布局位置*/
    var _endLeft: Int = 0

    /**布局当前位置*/
    var _left: Int = 0

    var refreshEffectConfig = RefreshEffectConfig()

    init {
        view.doOnLayout {
            _endLeft = view.left
            _left = _endLeft
        }
    }

    override fun onRelease() {
        super.onRelease()
        _pull = true
    }

    override fun onAbsorb(velocity: Int) {
        super.onAbsorb(velocity)
        _pull = true
    }

    override fun finish() {
        super.finish()
        _pull = false
    }

    override fun draw(canvas: Canvas?): Boolean {
        super.draw(canvas)

        if (_pull && (_state == STATE_PULL || _state == STATE_RECEDE)) {
            val mW = view.mW()
            view.offsetLeftTo(_left, -mW, mW)
            L.i("offsetLeftTo:$_left ->${view.left} $isRight")

            if (view.left == _endLeft) {
                finish()
                L.w("finish $isRight")
            }

            if (!isFinished && _state == STATE_RECEDE) {
                //反向滚动
                _left = if (isRight) {
                    clamp(view.left + releaseStep, -mW, _endLeft)
                } else {
                    clamp(view.left - releaseStep, _endLeft, mW)
                }
                L.i("nextLeft:$_left $isRight")
            } else {
                _pull = false
            }
        }

        return !isFinished
    }

    /**触发了pull*/
    var _pull = false

    override fun onPullInner(deltaDistance: Float, displacement: Float) {
        val mW = view.mW()
        val dxLeft = if (isRight) {
            -(deltaDistance * mW).toInt()
        } else {
            (deltaDistance * mW).toInt()
        }

        /*_left = view.left + refreshEffectConfig.getScrollInterpolation(
            _endLeft - view.left,
            dxLeft,
            mW
        )*/

        if (dxLeft.abs() >= scaledTouchSlop) {
            _left = view.left + dxLeft
            _pull = true

            L.i(deltaDistance, " ", displacement, " ", dxLeft, " $_left $isRight")
        }
    }

}