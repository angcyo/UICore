package com.angcyo.canvas.core.renderer.items

import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils._tempMatrix

/**
 * 绘制在[CanvasView]上的具体项目
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IItemRenderer : IRenderer {

    /**变压器*/
    val transformer: Transformer

    /**平移元素
     * [distanceX] 横向需要移动的像素距离
     * [distanceY] 纵向需要移动的像素距离*/
    fun translateBy(distanceX: Float, distanceY: Float) {
        _tempMatrix.reset()
        _tempMatrix.postTranslate(distanceX, distanceY)
        getRendererBounds().apply {
            _tempMatrix.mapRect(this, this)
        }
    }

    /**缩放元素
     * [scaleX] 横向需要移动的像素距离
     * [scaleY] 纵向需要移动的像素距离*/
    fun scaleBy(scaleX: Float, scaleY: Float) {
        _tempMatrix.reset()
        getRendererBounds().apply {
            _tempMatrix.postScale(scaleX, scaleY, centerX(), centerY())
            _tempMatrix.mapRect(this, this)
        }
    }

    /**旋转元素
     * [degrees] 旋转的角度*/
    fun rotateBy(degrees: Float) {
        _tempMatrix.reset()
        getRendererBounds().apply {
            _tempMatrix.postRotate(degrees, centerX(), centerY())
            _tempMatrix.mapRect(this, this)
        }
    }
}