package com.angcyo.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import pl.droidsonroids.gif.GifDrawable
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */
class GifDrawableImageViewTarget(
    val imageView: ImageView,
    val autoPlayGif: Boolean = true,
    val transition: DrawableCrossFadeTransition? = null
) : CustomViewTarget<ImageView, File>(imageView), Transition.ViewAdapter {

    var gifDrawable: GifDrawable? = null

    override fun onStop() {
        super.onStop()
        gifDrawable?.stop()
    }

    override fun onStart() {
        super.onStart()
        if (autoPlayGif) {
            gifDrawable?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gifDrawable?.recycle()
    }

    override fun onResourceReady(
        resource: File,
        tran: Transition<in File>?
    ) {
        gifDrawable = gifOfFile(resource)
        maybeUpdateAnimatable()
        if (transition == null || !transition.transition(gifDrawable, this)) {
            setDrawable(gifDrawable)
        }
    }

    private fun maybeUpdateAnimatable() {
        if (autoPlayGif) {
            gifDrawable?.start()
        } else {
            gifDrawable?.stop()
        }
    }

    override fun getCurrentDrawable(): Drawable? = imageView.drawable

    override fun onResourceLoading(placeholder: Drawable?) {
        super.onResourceLoading(placeholder)
        imageView.setImageDrawable(placeholder)
    }

    override fun setDrawable(drawable: Drawable?) {
        onResourceLoading(drawable)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        onResourceLoading(errorDrawable)
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        onResourceLoading(placeholder)
    }
}