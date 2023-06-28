package com.angcyo.canvas.render.core.component

import android.view.MotionEvent
import com.angcyo.canvas.render.core.CanvasControlManager
import com.angcyo.library.canvas.core.Reason
import com.angcyo.canvas.render.core.component.BaseControlPoint.Companion.CONTROL_TYPE_TRANSLATE
import kotlin.math.absoluteValue

/**
 * 元素平移控制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/23
 */
class TranslateRendererControl(controlManager: CanvasControlManager) : BaseControl(controlManager) {

    override fun onTouchMoveEvent(event: MotionEvent) {
        if (event.pointerCount <= 1 && handleControl) {
            //单指才能移动元素
            var tx = getTouchTranslateDxInside()
            var ty = getTouchTranslateDyInside()

            if (smartAssistantComponent.isEnableComponent) {
                smartAssistantComponent.findSmartDx(controlRendererBounds, tx, getTouchMoveDx())
                    ?.let {
                        tx = it
                    }
                smartAssistantComponent.findSmartDy(controlRendererBounds, ty, getTouchMoveDy())
                    ?.let {
                        ty = it
                    }
            }

            if (isControlHappen || tx.absoluteValue >= translateThreshold || ty.absoluteValue >= translateThreshold) {
                //已经发生过移动, 或者移动距离大于阈值
                if (tx != 0f && ty != 0f) {
                    translate(tx, ty)
                }
            }
        }
    }

    //region---操作---

    /**结束平移控制*/
    override fun endControl() {
        if (isNeedApply()) {
            controlRendererInfo?.let {
                applyTranslate(Reason.user.apply {
                    controlType = CONTROL_TYPE_TRANSLATE
                }, controlManager.delegate)
            }
        }
        super.endControl()
    }

    //endregion---操作---
}