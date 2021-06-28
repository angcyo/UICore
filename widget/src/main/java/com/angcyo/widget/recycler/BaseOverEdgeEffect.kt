package com.angcyo.widget.recycler

import android.graphics.Canvas
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.widget.EdgeEffect
import com.angcyo.library.ex.dpi
import com.angcyo.widget.layout.ITouchHold
import kotlin.math.min

/**
 * 下拉偏移的边界效果
 *  [view]需要实现[com.angcyo.widget.layout.ITouchHold]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseOverEdgeEffect(val view: View) : EdgeEffect(view.context) {

    companion object {
        const val STATE_IDLE = 0
        const val STATE_PULL = 1 //下拉中
        const val STATE_ABSORB = 2
        const val STATE_RECEDE = 3 //后退中
        const val STATE_PULL_DECAY = 4
        const val PULL_TIME = 167
        const val RECEDE_TIME = 600
        const val EPSILON = 0.001f
        const val PULL_DECAY_TIME = 2000
    }

    /**状态*/
    var _state: Int = STATE_IDLE

    /**释放时, 回退的步长*/
    var releaseStep = 10 * dpi

    var scaledTouchSlop: Int = 0

    init {
        scaledTouchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
    }

    /**
     * 2-> 是否绘制完成
     * [androidx.recyclerview.widget.RecyclerView.draw]
     * */
    override fun isFinished(): Boolean {
        return _state == STATE_IDLE
    }

    override fun finish() {
        _state = STATE_IDLE
    }

    /**
     * 3-> 释放滚动后
     * [androidx.recyclerview.widget.RecyclerView.considerReleasingGlowsOnScroll]
     * */
    override fun onRelease() {
        if (_state != STATE_PULL && _state != STATE_PULL_DECAY) {
            return
        }
        if (view is ITouchHold) {
            if (!view.isTouchHold) {
                _state = STATE_RECEDE
            }
        } else {
            _state = STATE_RECEDE
        }

        _startTime = AnimationUtils.currentAnimationTimeMillis()
        _duration = RECEDE_TIME.toFloat()
    }

    override fun onAbsorb(velocity: Int) {
        _state = STATE_ABSORB

        _startTime = AnimationUtils.currentAnimationTimeMillis()
        _duration = 0.15f + velocity * 0.02f
    }

    /**
     * 4-> 绘制
     * [androidx.recyclerview.widget.RecyclerView.draw]
     * */
    override fun draw(canvas: Canvas?): Boolean {
        update()
        return !isFinished
    }

    var _startTime: Long = 0
    var _duration = 0f

    /**
     * 1-> 边缘下拉时触发
     * [deltaDistance] 本次回调滚动的比例
     * [displacement] 整体的比例
     * [androidx.recyclerview.widget.RecyclerView.pullGlows]
     * */
    override fun onPull(deltaDistance: Float, displacement: Float) {
        val now = AnimationUtils.currentAnimationTimeMillis()
        if (_state == STATE_PULL_DECAY && now - _startTime < _duration) {
            return
        }
        _state = STATE_PULL

        _startTime = now
        _duration = PULL_TIME.toFloat()

        //this
        onPullInner(deltaDistance, displacement)
    }

    open fun onPullInner(deltaDistance: Float, displacement: Float) {

    }

    open fun update() {
        val time = AnimationUtils.currentAnimationTimeMillis()
        val t = min((time - _startTime) / _duration, 1f)
        if (t >= 1f - EPSILON) {
            when (_state) {
                STATE_ABSORB -> {
                    _state = STATE_RECEDE
                    _startTime = AnimationUtils.currentAnimationTimeMillis()
                    _duration = RECEDE_TIME.toFloat()
                }
                STATE_PULL -> {
                    _state = STATE_PULL_DECAY
                    _startTime = AnimationUtils.currentAnimationTimeMillis()
                    _duration = PULL_DECAY_TIME.toFloat()
                }
                STATE_PULL_DECAY -> _state = STATE_RECEDE
                STATE_RECEDE -> _state = STATE_IDLE
            }
        }
    }

}