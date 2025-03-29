package com.angcyo.opengl.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.L
import com.angcyo.library.annotation.ConfigProperty
import com.angcyo.library.annotation.OutputProperty
import com.angcyo.library.app
import com.angcyo.library.canvas.core.BaseCanvasTouchComponent
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.midPoint
import com.angcyo.library.ex.size
import com.angcyo.library.ex.spacing
import com.angcyo.library.gesture.DoubleGestureDetector2
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/29
 *
 * [OpenGLTextureView]
 * 2D手势识别, 支持平移, 缩放手势
 */
class OpenGL2DGesture : View.OnTouchListener {

    /**是否发生过平移手势*/
    @OutputProperty
    var isHappenedTranslate = false

    /**是否发生过缩放手势*/
    @OutputProperty
    var isHappenedScale = false

    /**当手指移动的距离大于此值, 是否有效的移动*/
    @ConfigProperty
    var translateThreshold = 3 * dp

    /**当手指缩放/放大的距离大于此值, 是否有效的缩放*/
    @ConfigProperty
    var scaleThreshold = 10 * dp

    /**双击时, 需要放大的比例*/
    @ConfigProperty
    var doubleScaleValue = 1.5f

    /**手势回调监听器*/
    @ConfigProperty
    val listeners = mutableListOf<OpenGL2DGestureListener>()

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> obtainPointList(
                    event,
                    _downPointList
                )

                MotionEvent.ACTION_MOVE -> obtainPointList(event, _movePointList)
            }
            handleTouchEvent(event)//---
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_UP -> obtainPointList(event, _downPointList)

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHappenedTranslate = false
                    isHappenedScale = false

                    _downPointList.clear()
                    _movePointList.clear()
                }
            }
        }
        return true
    }

    //region --core--

    /**数据缓存*/
    protected val _downPointList = mutableListOf<PointF>()
    protected val _movePointList = mutableListOf<PointF>()

    fun handleTouchEvent(event: MotionEvent) {
        doubleGestureDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            handleTranslateIntent()
            handleScaleIntent()
        }
    }

    /**获取事件[event]中所有手指的点位信息*/
    fun obtainPointList(
        event: MotionEvent,
        list: MutableList<PointF>
    ): List<PointF> {
        return BaseCanvasTouchComponent.obtainPointList(event, list)
    }

    /**消耗完[_movePointList]之后, 应该将[_downPointList]更新*/
    fun updateDownPointList(list: List<PointF> = _movePointList) {
        _downPointList.clear()
        _downPointList.addAll(list)
    }

    /**处理平移意图*/
    private fun handleTranslateIntent() {
        if (_movePointList.size() < 2) {
            return
        }

        val dx1 = _movePointList[0].x - _downPointList[0].x
        val dy1 = _movePointList[0].y - _downPointList[0].y

        val dx2 = _movePointList[1].x - _downPointList[1].x
        val dy2 = _movePointList[1].y - _downPointList[1].y

        if (dx1.absoluteValue >= translateThreshold && dx2.absoluteValue >= translateThreshold) {
            if (dx1 > 0 && dx2 > 0) {
                //双指向右移动
                translateBy(max(dx1, dx2), 0f)
            } else if (dx1 < 0 && dx2 < 0) {
                //双指向左移动
                translateBy(min(dx1, dx2), 0f)
            }
        } else if (dy1.absoluteValue >= translateThreshold && dy2.absoluteValue >= translateThreshold) {
            if (dy1 > 0 && dy2 > 0) {
                //双指向下移动
                translateBy(0f, max(dy1, dy2))
            } else if (dy1 < 0 && dy2 < 0) {
                //双指向上移动
                translateBy(0f, min(dy1, dy2))
            }
        }
    }

    private fun translateBy(dx: Float, dy: Float) {
        L.d("平移手势:dx:${dx} dy:${dy}")
        for (listener in listeners) {
            listener.onTranslateBy(dx, dy)
        }
        isHappenedTranslate = true
        updateDownPointList()
    }

    //--scale

    private val _tempPoint = PointF()

    /**双击检测*/
    private val doubleGestureDetector =
        DoubleGestureDetector2(app()) { event ->
            isHappenedScale = true
            //双击
            _tempPoint.set(event.x, event.y)
            for (listener in listeners) {
                listener.onScaleBy(doubleScaleValue, doubleScaleValue, _tempPoint.x, _tempPoint.y)
            }
        }

    /**处理缩放意图*/
    private fun handleScaleIntent() {
        if (_movePointList.size() < 2) {
            return
        }

        val c1 = spacing(
            _downPointList[0].x,
            _downPointList[0].y,
            _downPointList[1].x,
            _downPointList[1].y
        )
        val c2 = spacing(
            _movePointList[0].x,
            _movePointList[0].y,
            _movePointList[1].x,
            _movePointList[1].y
        )

        if ((c2 - c1).absoluteValue >= scaleThreshold) {
            //需要处理缩放
            val sx = c2 / c1
            val sy = sx
            midPoint(_movePointList[0], _movePointList[1], _tempPoint)
            //delegate.renderViewBox.transformToInside(_tempPoint)
            scaleBy(sx, sy, _tempPoint.x, _tempPoint.y)
        }
    }

    private fun scaleBy(sx: Float, sy: Float, px: Float, py: Float) {
        L.d("缩放手势:sx:${sx} sy:${sy} px:${px} py:${py}")
        for (listener in listeners) {
            listener.onScaleBy(sx, sy, px, py)
        }
        isHappenedScale = true
        updateDownPointList()
    }

    //endregion --core--

}

interface OpenGL2DGestureListener {

    /**回调本次手势移动的距离增量*/
    fun onTranslateBy(dx: Float, dy: Float)

    /**回调本次手势缩放的比例增量
     * [px] [py] 手势在view上的坐标*/
    fun onScaleBy(sx: Float, sy: Float, px: Float, py: Float)

}