package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library._refreshRateRatio

/**
 * 加载动画基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/01
 */
abstract class BaseLoadingDrawable : AbsDslDrawable() {

    /**是否需要动画*/
    var loading: Boolean = true
        set(value) {
            field = value
            invalidateSelf()
        }

    /**进度[0~100]*/
    var loadingProgress: Int = 0
        set(value) {
            field = value
            invalidateSelf()
        }

    /**步长*/
    var loadingStep: Int = 1

    /**是否要支持反向动画*/
    var enableReverse: Boolean = true

    //用来实现反向进度
    private var _loadingStep: Int = 0

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    /**开始循环动画*/
    open fun doLoading() {
        if (_loadingStep == 0) {
            _loadingStep = loadingStep
        }
        if (loading) {
            if (loadingProgress >= 100) {
                if (enableReverse) {
                    //反向进度
                    _loadingStep = -loadingStep
                } else {
                    loadingProgress = -_loadingStep
                }
            } else if (loadingProgress <= 0) {
                if (enableReverse) {
                    //反向进度
                    _loadingStep = loadingStep
                }
            }

            loadingProgress += (_loadingStep / _refreshRateRatio).toInt()
        }
    }
}