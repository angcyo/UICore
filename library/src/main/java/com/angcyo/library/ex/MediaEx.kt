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
