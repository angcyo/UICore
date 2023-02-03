package com.angcyo.media

import android.text.TextUtils
import com.angcyo.library.L
import com.angcyo.library.ex.toElapsedTime
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslSeekBar
import com.angcyo.widget.progress.HSProgressView
import kotlin.math.max

/**
 * 进度ui面板控制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/24
 */

object MediaHelper {

    /** 从url中, 获取录制的音频时长, 对应的数字 */
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

    /**重置布局*/
    fun resetLayout(itemHolder: DslViewHolder?, onSeekTo: (value: Float, fraction: Float) -> Unit) {
        showMediaLoadingView(itemHolder, false)
        itemHolder?.v<DslSeekBar>(R.id.lib_seek_bar)?.setProgress(0f, 0f, 0)

        itemHolder?.v<DslSeekBar>(R.id.lib_seek_bar)?.config {
            onSeekTouchEnd = onSeekTo
        }
    }

    /**显示下载加载中提示*/
    fun showMediaLoadingView(itemHolder: DslViewHolder?, show: Boolean = true) {
        itemHolder?.visible(R.id.bottom_wrap_layout, show)
        itemHolder?.visible(R.id.hs_progress_view, show)
        itemHolder?.gone(R.id.media_progress_layout)
        if (show) {
            itemHolder?.v<HSProgressView>(R.id.hs_progress_view)?.startAnimator()
        }
    }

    /**显示播放进度
     * [progress] 当前播放进度, 毫秒
     * [duration] 媒体总时长, 毫秒*/
    fun showMediaProgressView(itemHolder: DslViewHolder?, progress: Long, duration: Long) {
        itemHolder?.visible(R.id.bottom_wrap_layout, true)
        itemHolder?.visible(R.id.hs_progress_view, false)
        itemHolder?.visible(R.id.media_progress_layout)

        val pattern = intArrayOf(-1, 1, 1)
        val units = arrayOf("", "", ":", ":", ":")
        itemHolder?.tv(R.id.left_text_view)?.text = progress.toElapsedTime(pattern, units = units)
        val maxProgress = max(1, duration)
        itemHolder?.tv(R.id.right_text_view)?.text =
            maxProgress.toElapsedTime(pattern, units = units)

        itemHolder?.v<DslSeekBar>(R.id.lib_seek_bar)?.run {
            if (!isTouchDown) {
                setProgress(progress * 1f / maxProgress * 100)
            }
        }
    }
}