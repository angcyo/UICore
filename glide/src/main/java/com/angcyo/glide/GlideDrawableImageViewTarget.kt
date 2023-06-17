package com.angcyo.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.transition.Transition

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */
class GlideDrawableImageViewTarget(
    imageView: ImageView,
    waitForLayout: Boolean = !imageView.isLaidOut
) :
    DrawableImageViewTarget(imageView, waitForLayout) {

    init {
        //waitForLayout()
    }

    override fun getSize(cb: SizeReadyCallback) {
        super.getSize(cb)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        super.onResourceReady(resource, transition)
    }

    override fun setResource(resource: Drawable?) {
        super.setResource(resource)
    }
}