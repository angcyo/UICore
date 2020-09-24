package com.angcyo.core.component.accessibility.action.a

import android.graphics.PointF
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.arg
import com.angcyo.core.component.accessibility.base.TouchTipLayer
import com.angcyo.library.ex.reset

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTouchAction : BaseAction() {

    var showTouchTip = true

    //点位
    val p1 = PointF()
    val p2 = PointF()

    override fun reset() {
        super.reset()
        p1.reset()
        p2.reset()
    }

    override fun parseAction(autoParseAction: AutoParseAction, action: String?) {
        super.parseAction(autoParseAction, action)
        autoParseAction.parsePoint(arg?.arg()).let {
            p1.set(it[0])
            p2.set(it[1])
        }
    }

    fun click(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        x: Float, y: Float
    ): Boolean {
        if (showTouchTip) {
            TouchTipLayer.showTouch(x, y)
        }
        return service.gesture.click(x, y, autoParseAction.getGestureStartTime(arg?.arg(1)))
    }

    fun double(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        x: Float, y: Float
    ): Boolean {
        if (showTouchTip) {
            TouchTipLayer.showTouch(x, y)
        }
        return service.gesture.double(x, y, autoParseAction.getGestureStartTime(arg?.arg(1)))
    }

    fun move(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Boolean {
        if (showTouchTip) {
            TouchTipLayer.showMove(x1, y1, x2, y2)
        }
        return service.gesture.move(
            x1,
            y1,
            x2,
            y2,
            autoParseAction.getGestureStartTime(arg?.arg(1))
        )
    }

    fun fling(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Boolean {
        if (showTouchTip) {
            TouchTipLayer.showMove(x1, y1, x2, y2)
        }
        return service.gesture.fling(
            x1,
            y1,
            x2,
            y2,
            autoParseAction.getGestureStartTime(arg?.arg(1))
        )
    }

}