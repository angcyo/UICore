package com.angcyo.camera

import android.content.Context
import android.util.AttributeSet
import android.view.View
import cool.capturer.android.capturer.CameraView

/**
 * 根据可见性自动控制Camera的生命周期
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/21
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

class CameraPreviewView(context: Context, attributeSet: AttributeSet? = null) :
    CameraView(context, attributeSet) {

    var visibilityOld = GONE

    override fun init(context: Context?) {
        super.init(context?.applicationContext)
        //setBackgroundColor(Color.RED)
    }

    /**一旦销毁了, [android.opengl.GLSurfaceView]就无法被重新渲染了*/
    override fun release() {
        super.release()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        _changed(VISIBLE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _changed(INVISIBLE)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        _changed(visibility)
    }

    fun _changed(visibility: Int) {
        if (isEnabled) {
            //Log.i(TAG, "...onVisibilityChanged:" + visibility);
            if (visibility == VISIBLE) {
                if (visibilityOld != VISIBLE) {
                    if (havePermission()) {
                        onResume()
                    } else {
                        onPause()
                    }
                }
            } else {
                onPause()
            }
            visibilityOld = visibility
        }
    }
}