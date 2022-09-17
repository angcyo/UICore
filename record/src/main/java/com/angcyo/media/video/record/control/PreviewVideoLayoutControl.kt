package com.angcyo.media.video.record.control

import android.view.View
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toUri
import com.angcyo.media.video.widget.TextureVideoView
import com.angcyo.record.R

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class PreviewVideoLayoutControl(var parent: View) {

    fun showPreview(videoPath: String?, onCancel: Runnable) {
        parent.visibility = View.VISIBLE
        val cancelButton =
            parent.findViewById<View>(R.id.video_cancel_button)
        val confirmButton =
            parent.findViewById<View>(R.id.video_confirm_button)
        val videoView: TextureVideoView = parent.findViewById(R.id.video_view)
        cancelButton.setOnClickListener {
            videoView.stop()
            parent.visibility = View.GONE
            onCancel.run()
        }
        cancelButton.translationX = 100 * dp
        cancelButton.animate().translationX(0f).setDuration(300).start()
        confirmButton.translationX = -100 * dp
        confirmButton.animate().translationX(0f).setDuration(300).start()
        videoView.setRepeatPlay(true)
        videoView.setVideoURI(videoPath.toUri()!!)
        videoView.start()
    }

    fun stop() {
        val videoView: TextureVideoView = parent.findViewById(R.id.video_view)
        videoView.stop()
    }

}