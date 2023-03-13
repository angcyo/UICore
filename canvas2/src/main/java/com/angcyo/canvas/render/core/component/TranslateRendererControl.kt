package com.angcyo.canvas.render.core.component

import android.view.MotionEvent
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.BaseControlPoint.Companion.CONTROL_TYPE_KEEP_GROUP_PROPERTY
import com.angcyo.canvas.render.core.component.BaseControlPoint.Companion.CONTROL_TYPE_TRANSLATE
import com.angcyo.library.L
import kotlin.math.absoluteValue

/**
 * 元素平移控制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class TranslateRendererControl(controlManager: CanvasControlManager) : BaseControl(controlManager) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount <= 1) {
                    //单指才能移动元素
                    val tx = touchMovePointInside.x - touchDownPointInside.x
                    val ty = touchMovePointInside.y - touchDownPointInside.y
                    if (isControlHappen ||
                        tx.absoluteValue >= translateThreshold ||
                        ty.absoluteValue >= translateThreshold
                    ) {
                        //已经发生过移动, 或者移动距离大于阈值
                        if (tx != 0f && ty != 0f) {
                            translate(tx, ty)
                        }
                    }
                }
            }
        }
        return true
    }

    /**直接将渲染器移动多少距离
     * [tx] [ty] 如果是手指触发的, 那么距离应该是移动的位置减去首次按下时的距离*/
    private fun translate(tx: Float, ty: Float) {
        if (handleControl) {
            L.d("移动元素:tx:$tx ty:$ty")
            controlRendererInfo?.let {
                isControlHappen = true
                controlMatrix.setTranslate(tx, ty)

                applyTranslate(Reason.preview, controlManager.delegate)
            }
        }
    }

    //region---操作---

    /**结束平移控制*/
    override fun endControl() {
        if (isControlHappen && handleControl) {
            controlRendererInfo?.let {
                applyTranslate(Reason.user.apply {
                    controlType = CONTROL_TYPE_KEEP_GROUP_PROPERTY or CONTROL_TYPE_TRANSLATE
                }, controlManager.delegate)
            }
        }
        super.endControl()
    }

    //endregion---操作---
}