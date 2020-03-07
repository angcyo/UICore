package com.angcyo.image.ex

import androidx.annotation.IdRes
import com.angcyo.image.widget.RPhotoView
import com.angcyo.image.widget.RSamplingScaleImageView
import com.angcyo.widget.DslViewHolder
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */

fun DslViewHolder.photoView(@IdRes id: Int): PhotoView? = v(id)

fun DslViewHolder._photoView(@IdRes id: Int): RPhotoView? = v(id)
fun DslViewHolder.subSampling(@IdRes id: Int): SubsamplingScaleImageView? = v(id)
fun DslViewHolder._subSampling(@IdRes id: Int): RSamplingScaleImageView? = v(id)