package com.angcyo.media.audio.record

import android.app.Activity
import android.graphics.Rect
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library.ex.dpi
import com.angcyo.media.R
import com.angcyo.media.audio.widget.RecordAnimView
import com.angcyo.widget.base.find
import com.angcyo.widget.base.frameParams

/**
 *  模仿微信录音对话框的UI
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/13
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RecordUI {

    companion object {
        /**
         * 从url中, 获取录制的音频时长
         */
        fun getRecordTime(url: String?): Int {
            if (TextUtils.isEmpty(url)) {
                return -1
            }
            var result = -1
            try {
                val end =
                    url!!.split("_t_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val index = end.indexOf(".")
                result = if (index != -1) {
                    Integer.parseInt(end.substring(0, index))
                } else {
                    Integer.parseInt(end)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                L.w("$url 无时间参数信息`_t_`")
            }

            return result
        }

        /**
         * 00:00的格式输出, 如果有小时: 01:00:00
         */
        fun formatTime(millisecond: Long /*毫秒*/): String {
            val mill = millisecond / 1000

            val min = mill / 60
            val hour = min / 60

            val h = hour % 24
            val m = min % 60
            val s = mill % 60

            val builder = StringBuilder()
            if (hour > 0) {
                builder.append(if (h >= 10) h else "0$h")
                builder.append(":")
            }
            builder.append(if (m >= 10) m else "0$m")
            builder.append(":")
            builder.append(if (s >= 10) s else "0$s")

            return builder.toString()
        }
    }

    private var parent: ViewGroup? = null
    private var recordLayout: ViewGroup? = null
    private var touchView: View? = null

    var recordStartTime = 0L

    /**需要限制最大录制的时长 秒, -1不限制*/
    var maxRecordTime = -1L

    /**需要限制最小录制的时长 秒, -1不限制*/
    var minRecordTime = -1L

    /**
     * 当前录制的时间, 毫秒
     * */
    var currentRecordTime = 0L
        set(value) {
            field = value
            showRecordTime(value)
        }

    /**需要绘制的振幅数量*/
    var onGetMeterCount: () -> Int = {
        1
    }

    //最后一次记录的录制时间, 毫秒
    private var recordTimeLast = 0L

    private val checkTimeRunnable: Runnable by lazy {
        Runnable {
            recordTimeLast = System.currentTimeMillis()
            val millis = recordTimeLast - recordStartTime

            currentRecordTime = millis

            //更新振幅
            recordLayout?.find<RecordAnimView>(R.id.record_anim_view)?.drawCount = onGetMeterCount()

            if (maxRecordTime > 0 && millis >= maxRecordTime * 1000) {
                //到达最大值
                touchView?.postDelayed({
                    onMaxRecordTimeAction?.run()
                }, 60)
            } else {
                touchView?.postDelayed(checkTimeRunnable, 160)
            }
        }
    }

    /**
     * 达到最大时间的回调
     * */
    var onMaxRecordTimeAction: Runnable? = null

    private var touchDownY = -1f

    /**
     * @param activity 用来附着显示界面的Activity
     * @param touchView 用来请求拦截Touch事件
     * */
    fun show(activity: Activity, touchView: View? = null, touchY: Float = -1f) {
        touchView?.parent?.requestDisallowInterceptTouchEvent(true)

        if (recordLayout != null) {

        } else {
            this.touchView = touchView
            touchDownY = touchY

            parent = activity.window.findViewById(Window.ID_ANDROID_CONTENT)

            recordLayout = LayoutInflater.from(activity)
                .inflate(R.layout.layout_record_ui, parent, false) as? ViewGroup

            if (touchY >= 0) {
                recordLayout?.find<View>(R.id.record_wrap_layout)?.apply {
                    layoutParams = layoutParams.frameParams {
                        if (isViewPreferBottom()) {
                            //在屏幕的下方按下
                            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                            topMargin = ((touchY - 160 * dpi) / 2).toInt()
                        } else {
                            //在屏幕的上方按下
                            val screenHeight = _screenHeight
                            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                            bottomMargin = ((screenHeight - touchY - 160 * dpi) / 2).toInt()
                        }
                    }
                }
            }
            parent?.addView(recordLayout)
            recordStartTime = System.currentTimeMillis()
            touchView?.post(checkTimeRunnable)
        }

        showCancel(false)
    }

    fun hide() {
        touchView?.removeCallbacks(checkTimeRunnable)
        touchView?.parent?.requestDisallowInterceptTouchEvent(false)
        parent?.removeView(recordLayout)
        recordLayout = null
        parent = null
        touchView = null
    }

    private val tempRect: Rect by lazy {
        Rect()
    }

    /**
     * 视图是否在屏幕偏下的位置
     * */
    fun isViewPreferBottom(): Boolean {
        if (touchDownY >= 0) {
            val screenHeight = _screenHeight
            return touchDownY >= screenHeight / 2f
        }

        var result = false
        touchView?.let { view ->
            val height = view.resources.displayMetrics.heightPixels
            view.getGlobalVisibleRect(tempRect)

            result = tempRect.bottom > height / 2
        }

        return result
    }

    /**
     * 自动检查 是否需要显示取消的提示
     * */
    fun checkCancel(event: MotionEvent) {
        recordLayout?.let {
            touchView?.let { view ->
                val height = view.resources.displayMetrics.heightPixels
                view.getGlobalVisibleRect(tempRect)

                val startY: Int = if (touchDownY >= 0) {
                    touchDownY.toInt()
                } else {
                    tempRect.centerY()
                }

                val cancel = if (isViewPreferBottom()) {
                    //当前View 在屏幕中下位置
                    startY - event.rawY > height / 3
                } else {
                    event.rawY - startY > height / 3
                }

                showCancel(cancel)
            }
        }
    }

    /**
     * 是否触发了取消
     * */
    var isCancel = false

    /**是否是最小录制时间内*/
    val isMinRecordTime: Boolean = false
        get() {
            val millis = recordTimeLast - recordStartTime

            if (minRecordTime > 0 && millis < minRecordTime * 1000) {
                //未达到
                return true
            }
            return field
        }

    /**
     * 触发 取消录音提示
     * */
    fun showCancel(show: Boolean) {
        isCancel = show

        recordLayout?.let {
            val tipView: TextView = it.findViewById(R.id.record_cancel_tip_view)
            val tipImageView: View = it.findViewById(R.id.record_tip_layout)
            val tipImageCancelView: View = it.findViewById(R.id.record_cancel_tip_image_view)
            if (show) {
                tipView.text = "释放手指, 取消录制"
                tipView.setBackgroundResource(R.drawable.media_cancel_record_tip_shape)
                tipImageView.visibility = View.GONE
                tipImageCancelView.visibility = View.VISIBLE
            } else {
                if (isViewPreferBottom()) {
                    tipView.text = "上滑取消录制"
                } else {
                    tipView.text = "下滑取消录制"
                }
                tipView.background = null
                tipImageView.visibility = View.VISIBLE
                tipImageCancelView.visibility = View.GONE
            }
        }
    }

    /**
     *
     * @param millis 多少毫秒
     * */
    fun showRecordTime(millis: Long) {
        recordLayout?.let {
            val timeView: TextView = it.findViewById(R.id.record_time_view)

            if (maxRecordTime > 0) {
                timeView.text = "${formatTime(
                    millis
                )}/${formatTime(
                    maxRecordTime * 1000
                )}"
            } else {
                timeView.text =
                    formatTime(
                        millis
                    )
            }
        }
    }
}