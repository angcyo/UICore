package com.angcyo.library.ex

import android.media.MediaMetadataRetriever
import com.angcyo.library.L
import com.angcyo.library.utils.Constant

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/24
 */

/** 获取视频/音频时长 毫秒 */
fun String?.getMediaDuration(): Long {
    if (this.isNullOrBlank()) {
        return -1
    }
    val retriever = MediaMetadataRetriever()
    val duration: Long
    try {

        if (this.isHttpScheme()) {
            val headers = hashMapOf<String, String>()
            headers["User-Agent"] = Constant.UA
            retriever.setDataSource(this, headers)
        } else {
            retriever.setDataSource(this)
        }
        duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
    } catch (e: Exception) {
        L.w("$e")
        return -1
    } finally {
        retriever.release()
    }
    return duration
}

/**时长, 宽度, 高度*/
fun String?.getMediaMetadata(): LongArray {
    if (this.isNullOrBlank()) {
        return longArrayOf(-1, -1, -1)
    }
    val retriever = MediaMetadataRetriever()
    val duration: Long
    var width: Long = -1
    var height: Long = -1
    try {

        if (this.isHttpScheme()) {
            val headers = hashMapOf<String, String>()
            headers["User-Agent"] = Constant.UA
            retriever.setDataSource(this, headers)
        } else {
            retriever.setDataSource(this)
        }
        duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toLong()
        height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toLong()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    .toInt()
            if (rotation == 90 || rotation == 270) {
                val temp = width
                width = height
                height = temp
            }
        }
    } catch (e: Exception) {
        L.w("$e")
        return longArrayOf(-1, -1, -1)
    } finally {
        retriever.release()
    }
    return longArrayOf(duration, width, height)
}