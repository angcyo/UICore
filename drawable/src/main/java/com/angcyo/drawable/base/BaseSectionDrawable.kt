package com.angcyo.drawable.base

import android.graphics.Canvas
import android.view.animation.Interpolator
import androidx.core.math.MathUtils.clamp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

abstract class BaseSectionDrawable : AbsDslDrawable() {
    /**
     * 需要分成几段绘制.
     * 如 {0.2f 0.3f 0.3f 0.1f 0.1f} 总和要为1
     */
    var sections = floatArrayOf(1f)
        set(value) {
            field = value
            ensureSection()
            invalidateSelf()
        }

    /**
     * 每一段的差值器, 可以和sections数量不一致, 有的就取, 没有就默认
     */
    var interpolatorList: List<Interpolator?>? = null

    /**
     * 总进度, 100表示需要绘制path的全部, 这个值用来触发动画
     */
    var progress = 100
        set(value) {
            field = clamp(value, 0, 100)
            invalidateSelf()
        }

    /**动画步长*/
    var loadingStep: Int = 1

    /**是否需要动画, 自动累加[progress]值*/
    var loading: Boolean = false
        set(value) {
            field = value
            invalidateSelf()
        }

    /**
     * 所有sections加起来的总和
     */
    var sumSectionProgress = 1f

    /**当前进度所在的section索引*/
    val progressSectionIndex: Int
        get() {
            val totalProgress = progress * 1f / 100f
            val maxSection = sections.size
            var sum = 0f
            for (i in 0 until maxSection) {
                val section = sections[i]
                sum += section
                if (totalProgress <= sum) {
                    return i
                }
            }
            return -1
        }

    /**当前进度在自己的Section中的进度*/
    val progressSectionProgress: Float
        get() {
            val totalProgress = progress * 1f / 100f
            val maxSection = sections.size
            var sum = 0f
            for (i in 0 until maxSection) {
                var sectionProgress = -1f
                val section = sections[i]
                if (totalProgress <= sum + section) { //绘制中
                    sectionProgress = (totalProgress - sum) / section
                    return sectionProgress
                }
                sum += section
            }
            return 0f
        }

    override fun draw(canvas: Canvas) {
        if (sections.isNotEmpty()) {
            //总进度
            val totalProgress = progress * 1f / 100f
            val maxSection = sections.size
            //绘制前
            onDrawBefore(canvas, maxSection, totalProgress)
            var sum = 0f
            for (i in 0 until maxSection) {
                var sectionProgress = -1f
                val section = sections[i]
                if (totalProgress <= sum + section) { //绘制中
                    sectionProgress = (totalProgress - sum) / section
                }
                //差值器
                if (!interpolatorList.isNullOrEmpty()) {
                    sectionProgress =
                        interpolatorList?.getOrNull(i)?.getInterpolation(sectionProgress)
                            ?: sectionProgress
                }
                if (totalProgress >= sum && totalProgress < sum + section) {
                    onDrawProgressSection(
                        canvas,
                        i,
                        sum,
                        sum + section,
                        totalProgress,
                        sectionProgress
                    )
                }
                /*小于总进度的 section 都会执行绘制, 到了这里之前的section进度肯定已经是100%了*/
                if (totalProgress >= sum) {
                    if (totalProgress > sum + section) { //当section的总和小于1时, 最后一个是section会多执行剩余的进度
                        sectionProgress = 1f
                    }
                    onDrawSection(canvas, maxSection, i, totalProgress, sectionProgress)
                }
                sum += section
            }
            //绘制后
            onDrawAfter(canvas, maxSection, totalProgress)

            if (loading) {
                doLoading()
            }
        }
    }

    open fun doLoading() {
        if (progress >= 100) {
            progress = 0
        } else {
            progress += loadingStep
            if (progress >= 100) {
                progress = 100
            }
        }
    }

    open fun ensureSection() {
        if (sections.isNotEmpty()) {
            sumSectionProgress = 0f
            for (section in sections) {
                sumSectionProgress += section
            }
            check(sumSectionProgress <= 1f) { "Section 总和不能超过1f" }
            return
        }
        throw IllegalStateException("请设置 Section")
    }

    /**
     * 当前进度在对应的section中, 只执行当前section的绘制
     */
    open fun onDrawProgressSection(
        canvas: Canvas,
        index: Int /*当前绘制的第几段, 0开始*/,
        startProgress: Float /*当前section开始的进度值*/,
        endProgress: Float /*当前section结束的进度值*/,
        totalProgress: Float /*总进度*/,
        sectionProgress: Float /*section中的进度*/
    ) {
    }

    open fun onDrawBefore(
        canvas: Canvas,
        maxSection: Int,
        totalProgress: Float
    ) {
    }

    /**
     * 重写此方法, 根据section绘制不同内容,
     * 当[index]为2时, 那么[0,1]的[progress]一定是1f
     * [progress] 当前的进度[sectionProgress]
     */
    open fun onDrawSection(
        canvas: Canvas,
        maxSection: Int,  /*path 最多分成了几段, 至少1段*/
        index: Int /*当前绘制的第几段, 0开始*/,
        totalProgress: Float,  /*总进度 0-1*/
        progress: Float /*当前path段的进度 0-1*/
    ) {
    }

    open fun onDrawAfter(
        canvas: Canvas,
        maxSection: Int,
        totalProgress: Float
    ) {
    }
}