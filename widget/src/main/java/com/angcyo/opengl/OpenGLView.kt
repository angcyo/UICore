package com.angcyo.opengl

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.angcyo.library.L
import com.angcyo.library.ex.randomColor

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/17
 *
 * https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=zh-cn
 *
 * https://developer.android.com/develop/ui/views/graphics/opengl/environment?hl=zh-cn
 */
class OpenGLView(context: Context, attr: AttributeSet?) : GLSurfaceView(context, attr) {

    /**渲染器*/
    var glRenderer = OpenGLRenderer()

    init {
        setEGLContextFactory(OpenGLContextFactory())
        //setEGLContextClientVersion(glVersion.toInt())
        setRenderer(glRenderer)

        //renderMode = RENDERMODE_WHEN_DIRTY
        //Android Extension Pack (AEP)
        //https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=zh-cn#aep
        val packageManager = context.packageManager
        val deviceSupportsAEP: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_OPENGLES_EXTENSION_PACK)
        L.i("deviceSupportsAEP: $deviceSupportsAEP")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                glRenderer.backgroundColor = randomColor()
            }
        }
        return true
    }
}


