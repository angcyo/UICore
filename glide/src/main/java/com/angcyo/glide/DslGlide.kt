package com.angcyo.glide

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.angcyo.http.OkType
import com.angcyo.library.L
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import okhttp3.Call
import pl.droidsonroids.gif.GifDrawable
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

class DslGlide {

    /**设置目标*/
    var targetView: ImageView? = null
        set(value) {
            val old = field
            field = value
            if (old != value) {
                old?.clear()
            }
        }

    /**是否检查gif, 如果为gif, 则使用[GifDrawable]加载gif, 否则使用[glide]默认加载处理*/
    var checkGifType = false
    /**当检查到gif时, 是否自动播放*/
    var autoPlayGif = true

    var placeholderDrawable: Drawable? = null
        set(value) {
            field = value
            if (fallbackDrawable == null) {
                fallbackDrawable = value
            }
        }
    //url = null
    var fallbackDrawable: Drawable? = null
    //url = ""
    var errorDrawable: Drawable? = null

    /**Target.SIZE_ORIGINAL*/
    var originalSize: Boolean = false

    //强制指定override的宽高
    var overrideWidth: Int = -1
    var overrideHeight: Int = -1

    /**开始过渡动画*/
    var transition = true

    /**开启[checkGifType]时, 才有效*/
    var onTypeCallback: (OkType.ImageType) -> Unit = {}

    var onConfigRequest: (builder: RequestBuilder<*>) -> Unit = {}

    //<editor-fold desc="方法">

    /**开始加载, 支持本地, 网络, gif, 视频, 图片*/
    fun load(string: String?) {
        _checkLoad {
            if (checkGifType) {
                targetView?.setImageDrawable(placeholderDrawable)
                _checkType(string) {
                    onTypeCallback(it)
                    _load(string, it == OkType.ImageType.GIF)
                }
            } else {
                _load(string)
            }
        }
    }

    /**清理请求*/
    fun clear() {
        targetView?.clear()
    }

    /**重置属性*/
    fun reset() {
        clear()
        checkGifType = false
        autoPlayGif = true
        originalSize = false
        transition = true
        overrideWidth = -1
        overrideHeight = -1
        onTypeCallback = {}
        onConfigRequest = {}
    }

    //</editor-fold desc="方法">

    //<editor-fold desc="辅助方法">

    fun _checkLoad(action: () -> Unit) {
        when {
            isDestroyed() -> L.w("activity isDestroyed!")
            targetView == null -> L.w("targetView is null!")
            else -> action()
        }
    }

    //检查图片类型
    fun _checkType(string: String?, action: (imageType: OkType.ImageType) -> Unit) {
        clear()
        if (string.isNullOrBlank()) {
            action(OkType.ImageType.UNKNOWN)
        } else {
            OkType.type(string, object : OkType.OnImageTypeListener {
                override fun onImageType(imageUrl: String, imageType: OkType.ImageType) {
                    L.d("type: $imageUrl ->$imageType")
                    action(imageType)
                }

                override fun onLoadStart() {
                    L.v("check $string type.")
                }
            })?.attach()
        }
    }

    //开始加载
    fun _load(string: String?, asGif: Boolean = false) {
        clear()

        _checkLoad {
            if (asGif) {
                _glide()
                    .download(string)
                    .configRequest()
                    .into(object : CustomTarget<File>() {
                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                            setDrawable(placeholder)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            setDrawable(errorDrawable)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            setDrawable(placeholder)
                        }

                        override fun onResourceReady(
                            resource: File,
                            transition: Transition<in File>?
                        ) {
                            setDrawable(gifOfFile(resource)?.apply {
                                if (autoPlayGif) {
                                    start()
                                } else {
                                    stop()
                                }
                            })
                            //clear()
                        }

                        fun setDrawable(drawable: Drawable?) {
                            targetView?.setImageDrawable(drawable)
                        }
                    })
            } else {
                _glide()
                    .load(string)
                    .configRequest()
                    .into(object : DrawableImageViewTarget(targetView!!) {
                        override fun getSize(cb: SizeReadyCallback) {
                            super.getSize(cb)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            super.onLoadCleared(placeholder)
                        }

                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            super.onResourceReady(resource, transition)
                            //getView().clear()
                        }

                        override fun setResource(resource: Drawable?) {
                            super.setResource(resource)
                        }
                    }.waitForLayout())
            }
        }
    }

    fun _glide(): RequestManager {
        return Glide.with(targetView!!)
    }

    /**[Activity]是否已经销毁*/
    fun isDestroyed(): Boolean {
        if (targetView?.context is Activity) {
            if ((targetView?.context as Activity).isDestroyed) {
                return true
            }
        }
        return false
    }

    //</editor-fold desc="辅助方法">

    //<editor-fold desc="辅助扩展">

    fun View.clear() {
        try {
            Glide.with(this).clear(this)
            tag = null
        } catch (e: Exception) {
            L.e(e)
        }
        (getTag(R.id.tag_ok_type) as? Call)?.cancel()
        setTag(R.id.tag_ok_type, null)
    }

    //附加请求
    fun Call.attach() {
        targetView?.setTag(R.id.tag_ok_type, this)
    }

    //配置请求
    @SuppressLint("CheckResult")
    inline fun <reified T> RequestBuilder<T>.configRequest(): RequestBuilder<T> {

        //override
        if (originalSize) {
            override(Target.SIZE_ORIGINAL)
        }
        if (overrideWidth > 0 && overrideHeight > 0) {
            override(overrideWidth, overrideHeight)
        }

        //drawable
        this@DslGlide.placeholderDrawable?.run {
            placeholder(this)
        }
        this@DslGlide.fallbackDrawable?.run {
            fallback(this)
        }
        this@DslGlide.errorDrawable?.run {
            error(this)
        }

        //transition https://muyangmin.github.io/glide-docs-cn/doc/options.html#%E8%BF%87%E6%B8%A1%E9%80%89%E9%A1%B9
        if (transition) {
            val clazz = T::class.java
            if (Bitmap::class.java == clazz) {
                transition(BitmapTransitionOptions.withCrossFade() as TransitionOptions<*, T>)
            } else if (Drawable::class.java.isAssignableFrom(clazz)) {
                transition(DrawableTransitionOptions.withCrossFade() as TransitionOptions<*, T>)
            }
        }

        //thumbnail https://muyangmin.github.io/glide-docs-cn/doc/options.html#%E7%BC%A9%E7%95%A5%E5%9B%BE-thumbnail-%E8%AF%B7%E6%B1%82

        //custom
        onConfigRequest(this)

        //copy from com.bumptech.glide.RequestBuilder.into(android.widget.ImageView)
        if (!isTransformationSet && isTransformationAllowed && targetView?.scaleType != null) {
            // Clone in this method so that if we use this RequestBuilder to load into a View and then
            // into a different target, we don't retain the transformation applied based on the previous
            // View's scale type.
            when (targetView?.scaleType) {
                ScaleType.CENTER_CROP -> optionalCenterCrop()
                ScaleType.CENTER_INSIDE -> optionalCenterInside()
                ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END -> optionalFitCenter()
                ScaleType.FIT_XY -> optionalCenterInside()
                ScaleType.CENTER, ScaleType.MATRIX -> {
                }
                else -> {
                }
            }
        }

        return this
    }

    //</editor-fold desc="辅助扩展">

}