package com.angcyo.widget.blur

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.angcyo.library.ex.activityContent

/**
 * 实时模糊, 来自:
 * https://github.com/kongzue/DialogV3
 *
 *
 * 实时模糊库:
 * https://github.com/mmin18/RealtimeBlurView
 *
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class ActivityRealtimeBlurView(context: Context, attrs: AttributeSet? = null) :
    BlurView(context, attrs) {

    val _activityDecorView: View?
        get() {
            val ctx = context.activityContent()
            return if (ctx is Activity) {
                ctx.window.decorView
            } else {
                null
            }
        }

    override var blurTargetView: View? = null
        get() = _activityDecorView
}