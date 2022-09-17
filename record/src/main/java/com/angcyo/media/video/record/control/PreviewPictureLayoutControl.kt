package com.angcyo.media.video.record.control

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.angcyo.library.ex.dp
import com.angcyo.record.R

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class PreviewPictureLayoutControl(var parent: View) {
    fun showPreview(bitmap: Bitmap?) {
        parent.visibility = View.VISIBLE
        (parent.findViewById<View>(R.id.camera_display_image_view) as ImageView).setImageBitmap(
            bitmap
        )
        val cancelButton =
            parent.findViewById<View>(R.id.camera_cancel_button)
        val confirmButton =
            parent.findViewById<View>(R.id.camera_confirm_button)
        cancelButton.setOnClickListener { parent.visibility = View.GONE }
        cancelButton.translationX = 100 * dp
        cancelButton.animate().translationX(0f).setDuration(300).start()
        confirmButton.translationX = -100 * dp
        confirmButton.animate().translationX(0f).setDuration(300).start()
    }
}