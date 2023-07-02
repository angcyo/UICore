package com.angcyo.camerax

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.camera.view.PreviewView
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.angcyo.camerax.control.CameraXPreviewControl
import com.angcyo.camerax.ui.FocusPointDrawable
import com.angcyo.camerax.ui.RectFDrawable
import com.angcyo.core.fragment.BaseTitleFragment
import com.angcyo.http.rx.doMain
import com.angcyo.library.annotation.OverridePoint
import com.angcyo.library.ex.load
import com.angcyo.library.toastQQ

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/17
 */
open class CameraXPreviewFragment : BaseTitleFragment() {

    private companion object {
        // animation constants for focus point
        private const val SPRING_STIFFNESS_ALPHA_OUT = 100f
        private const val SPRING_STIFFNESS = 800f
        private const val SPRING_DAMPING_RATIO = 0.35f
    }

    /**核心控制器*/
    val cameraXPreviewControl: CameraXPreviewControl by lazy {
        CameraXPreviewControl().apply {
            rgbImageAnalysisAnalyzer.onRectTestAction = {
                previewView?.apply {
                    overlay.clear()
                    overlay.add(RectFDrawable(it))
                }
            }
        }
    }

    val previewView: PreviewView?
        get() = _vh.v(R.id.lib_camera_view)

    init {
        //fragmentLayoutId = R.layout.lib_camerax_preview_layout
        contentLayoutId = R.layout.lib_camerax_preview_layout
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        initCameraXLayout()
    }

    @OverridePoint
    open fun initCameraXLayout() {
        //预览
        previewView?.let { view ->
            if (cameraXPreviewControl.havePermission()) {
                cameraXPreviewControl.bindToLifecycle(view, this)
            } else {
                view.post {
                    cameraXPreviewControl.requestPermission {
                        if (it) {
                            cameraXPreviewControl.bindToLifecycle(view, this)
                        }
                    }
                }
            }

            //touch
            initCameraXTouch(view)
        }

        //切换摄像头
        _vh.click(R.id.lib_camera_switch_view) {
            it.isEnabled = cameraXPreviewControl.hasCamera()
            cameraXPreviewControl.switchCamera()
            //cameraXPreviewControl.updateImageAnalysisTargetSize()
        }

        //拍照
        _vh.click(R.id.lib_camera_shutter_view) {
            cameraXPreviewControl.capturePhoto { uri, exception ->
                exception?.let {
                    toastQQ(it.message)
                }
                uri?.let {
                    doMain {
                        showPhoto(it)
                    }
                }
            }
        }
        _vh.click(R.id.lib_camera_close_photo_preview) {
            hidePhoto()
        }
    }

    /**手势事件*/
    open fun initCameraXTouch(previewView: PreviewView) {
        val gestureDetector =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    cameraXPreviewControl.focus(e.x, e.y)
                    showFocusPoint(_vh.v(R.id.lib_camera_focus_point), e.x, e.y)
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    cameraXPreviewControl.switchCamera()
                    return true
                }
            })

        val scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    cameraXPreviewControl.scale(detector.scaleFactor)
                    return true
                }
            })

        //对焦/缩放手势
        previewView.setOnTouchListener { _, event ->
            var didConsume = scaleGestureDetector.onTouchEvent(event)
            if (!scaleGestureDetector.isInProgress) {
                didConsume = gestureDetector.onTouchEvent(event)
            }
            didConsume
        }
    }

    /**显示对焦动画*/
    protected fun showFocusPoint(view: View?, x: Float, y: Float) {
        view ?: return
        val drawable = FocusPointDrawable()
        val strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3f,
            context.resources.displayMetrics
        )
        drawable.setStrokeWidth(strokeWidth)

        val alphaAnimation = SpringAnimation(view, DynamicAnimation.ALPHA, 1f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING_RATIO

            addEndListener { _, _, _, _ ->
                SpringAnimation(view, DynamicAnimation.ALPHA, 0f)
                    .apply {
                        spring.stiffness = SPRING_STIFFNESS_ALPHA_OUT
                        spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                    }
                    .start()
            }
        }
        val scaleAnimationX = SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING_RATIO
        }
        val scaleAnimationY = SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
            spring.stiffness = SPRING_STIFFNESS
            spring.dampingRatio = SPRING_DAMPING_RATIO
        }

        view.apply {
            background = drawable
            isVisible = true
            translationX = x - width / 2f
            translationY = y - height / 2f
            alpha = 0f
            scaleX = 1.5f
            scaleY = 1.5f
        }

        alphaAnimation.start()
        scaleAnimationX.start()
        scaleAnimationY.start()
    }

    /**显示预览图*/
    protected fun showPhoto(uri: Uri?) {
        if (uri == null) return
        //后摄: bitmap:图片宽:3072 高:1574 :18.45MB
        //前摄: bitmap:图片宽:2304 高:1180 :10.37MB
        //val bitmap = uri.readBitmap()
        //L.i("bitmap:${bitmap?.logInfo()}")
        _vh.img(R.id.lib_camera_photo_preview)?.isVisible = true
        _vh.img(R.id.lib_camera_photo_preview)?.load(uri)
        _vh.img(R.id.lib_camera_close_photo_preview)?.isVisible = true
    }

    /**关闭预览图*/
    protected fun hidePhoto() {
        _vh.img(R.id.lib_camera_photo_preview)?.isVisible = false
        _vh.img(R.id.lib_camera_close_photo_preview)?.isVisible = false
    }

}