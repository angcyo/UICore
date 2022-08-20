package com.angcyo.doodle

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withTranslation
import com.angcyo.doodle.brush.EraserBrush
import com.angcyo.doodle.component.DoodleMagnifier
import com.angcyo.doodle.component.MagnifierCanvas
import com.angcyo.doodle.component.PreviewCanvas
import com.angcyo.doodle.core.*
import com.angcyo.doodle.element.BaseElement
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.doodle.layer.NormalLayer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.longFeedback
import com.angcyo.library.ex.scale
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleDelegate(val view: View) : IDoodleView {

    //region ---成员---

    /**配置类*/
    val doodleConfig = DoodleConfig()

    /**事件回调*/
    val doodleListenerList = mutableSetOf<IDoodleListener>()

    /**透明底层*/
    val alphaElement = AlphaElement()

    /**放大镜*/
    var doodleMagnifier: DoodleMagnifier? = DoodleMagnifier(view)

    //endregion ---成员---

    //region ---核心成员---

    /**视口*/
    var viewBox = DoodleViewBox(this)

    /**回退栈管理*/
    var undoManager = DoodleUndoManager(this)

    /**手势管理*/
    var doodleTouchManager = DoodleTouchManager(this)

    /**图层管理*/
    var doodleLayerManager = DoodleLayerManager(this)

    //endregion ---核心成员---

    //region ---成员变量---

    /**当前操作的图层*/
    var operateLayer: BaseLayer?
        get() = doodleLayerManager.operateLayer
        set(value) = doodleLayerManager.updateOperateLayer(value)

    //endregion ---成员变量---

    //region ---入口---

    @CallPoint
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewBox.onSizeChanged(w, h, oldw, oldh)
    }

    @CallPoint
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = doodleTouchManager.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                doodleMagnifier?.apply {
                    isEnable = doodleTouchManager.touchRecognize is EraserBrush
                    if (isEnable) {
                        magnifierScale = min(4f, 200 / doodleConfig.paintWidth) //最大4倍
                        update(event)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                doodleMagnifier?.apply {
                    if (isEnable) {
                        update(event)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                doodleMagnifier?.apply {
                    if (isEnable) {
                        update(event)
                        isEnable = false
                        refresh()
                    }
                }
            }
        }
        return result
    }

    @CallPoint
    override fun onDraw(canvas: Canvas) {
        //透明背景显示
        if (canvas !is PreviewCanvas) {
            //预览时, 不绘制透明背景提示
            alphaElement.onDraw(canvas)
        }

        //图层
        doodleLayerManager.onDraw(canvas)

        //放大镜
        if (canvas !is MagnifierCanvas && canvas !is PreviewCanvas) {
            doodleMagnifier?.apply {
                if (isEnable) {
                    onDraw(canvas)
                }
            }
        }
    }

    //endregion ---入口---

    //region ---Core---

    override fun dispatchLayerAdd(layer: BaseLayer) {
        doodleListenerList.forEach {
            it.onLayerAdd(layer)
        }
    }

    override fun dispatchLayerRemove(layer: BaseLayer) {
        doodleListenerList.forEach {
            it.onLayerRemove(layer)
        }
    }

    override fun dispatchOperateLayerChanged(from: BaseLayer?, to: BaseLayer?) {
        doodleListenerList.forEach {
            it.onOperateLayerChanged(from, to)
        }
    }

    override fun dispatchTouchRecognizeChanged(from: ITouchRecognize?, to: ITouchRecognize?) {
        doodleListenerList.forEach {
            it.onTouchRecognizeChanged(from, to)
        }
    }

    override fun dispatchDoodleUndoChanged() {
        doodleListenerList.forEach {
            it.onDoodleUndoChanged(undoManager)
        }
    }

    override fun dispatchCreateElement(element: BaseElement, brush: ITouchRecognize?) {
        doodleListenerList.forEach {
            it.onCreateElement(element, brush)
        }
    }

    override fun dispatchElementAttach(elementList: List<BaseElement>, layer: BaseLayer) {
        doodleListenerList.forEach {
            it.onElementAttach(elementList, layer)
        }
    }

    override fun dispatchElementDetach(elementList: List<BaseElement>, layer: BaseLayer) {
        doodleListenerList.forEach {
            it.onElementDetach(elementList, layer)
        }
    }

    //endregion ---Core---

    //region ---操作方法---

    /**刷新界面*/
    override fun refresh() {
        view.postInvalidateOnAnimation()
    }

    //endregion ---操作方法---

    //region ---operate---

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    fun addLayer() {

    }

    /**添加一个元素*/
    fun addElement(element: BaseElement, strategy: Strategy = Strategy.Normal()) {
        if (doodleLayerManager.layerList.isEmpty()) {
            //添加一个默认的背景层
            doodleLayerManager.addLayer(NormalLayer(this), Strategy.Redo())
        }
        operateLayer?.addElement(element, strategy)
    }

    /**获取预览的图片*/
    fun getPreviewBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            viewBox.contentRect.width(),
            viewBox.contentRect.height(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = PreviewCanvas(bitmap)
        canvas.withTranslation(
            viewBox.contentRect.left.toFloat(),
            viewBox.contentRect.top.toFloat()
        ) {
            onDraw(canvas)
        }
        return bitmap
    }

    /**获取指定图层预览的图片*/
    fun getLayerPreviewBitmap(layer: BaseLayer = doodleLayerManager.operateLayer!!): Bitmap {
        val bitmap = Bitmap.createBitmap(
            viewBox.contentRect.width(),
            viewBox.contentRect.height(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = PreviewCanvas(bitmap)
        canvas.withTranslation(
            viewBox.contentRect.left.toFloat(),
            viewBox.contentRect.top.toFloat()
        ) {
            //doodleLayerManager.backgroundLayer?.onDraw(this)
            doodleLayerManager.onDrawLayer(this, layer)
        }
        return bitmap
    }

    /**获取一个用来居中显示元素的矩形*/
    fun getCenterRect(width: Int, height: Int, margin: Int = 40 * dpi): RectF {
        val rect = RectF()

        val contentRect = viewBox.contentRect
        val centerX = contentRect.centerX().toFloat()
        val centerY = contentRect.centerY().toFloat()
        rect.set(
            centerX - width / 2,
            centerY - height / 2,
            centerX + width / 2,
            centerY + height / 2
        )

        val maxWidth = contentRect.width() - 2 * margin
        val maxHeight = contentRect.height() - 2 * margin

        var scaleWidth = 1f
        var scaleHeight = 1f

        if (width > maxWidth) {
            scaleWidth = maxWidth * 1f / width
        }

        if (height > maxHeight) {
            scaleHeight = maxHeight * 1f / height
        }

        val scale = max(scaleWidth, scaleHeight)
        rect.scale(scale, scale, centerX, centerY)
        return rect
    }

    //endregion ---operate---
}