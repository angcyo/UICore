package com.angcyo.canvas.render.util

import android.graphics.Picture
import android.graphics.drawable.PictureDrawable

/**
 * 不支持跟随Bounds的变化进行Scale
 * [android.graphics.Picture.beginRecording]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class PictureRenderDrawable(picture: Picture?) : PictureDrawable(picture)