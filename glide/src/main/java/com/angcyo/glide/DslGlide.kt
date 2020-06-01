package com.angcyo.glide

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.angcyo.http.OkType
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.app
import com.angcyo.library.ex.isFileExist
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.loadUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import jp.wasabeef.glide.transformations.SupportRSBlurTransformation
import okhttp3.Call
import pl.droidsonroids.gif.GifDrawable
import java.io.File


/**
 *
 * https://muyangmin.github.io/glide-docs-cn/
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

class DslGlide {
    companion object {
        //https://muyangmin.github.io/glide-docs-cn/doc/caching.html#%E8%B5%84%E6%BA%90%E7%AE%A1%E7%90%86

        fun clearDiskCache() {
            Glide.get(app()).clearDiskCache()
        }

        fun clearMemory() {
            Glide.get(app()).clearMemory()
        }

    }

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
    var transition = false

    /**
     * 转换
     * https://github.com/wasabeef/glide-transformations
     * */
    val transformations = mutableListOf<Transformation<Bitmap>>()

    /**开启[checkGifType]时, 才有效*/
    var onTypeCallback: (OkType.ImageType) -> Unit = {}

    /**自定请求选项*/
    var onConfigRequest: (builder: RequestBuilder<*>, model: Class<*>) -> Unit = { _, _ -> }

    /**自定义请求头*/
    var onConfigHeader: (LazyHeaders.Builder) -> Unit = {}

    /**[RequestListener]*/
    var onLoadFailed: ((model: String?, error: GlideException?) -> Boolean) = { _, _ -> false }

    /**[model]请求的url地址, [data]加载的数据, Drawable File等*/
    var onLoadSucceed: ((model: String?, data: Any?) -> Boolean) = { _, _ -> false }

    //<editor-fold desc="方法">

    /**开始加载, 支持本地, 网络, gif, 视频, 图片*/
    fun load(string: String?) {
        if (string.isNullOrBlank()) {
            _load(null, false)
        } else {
            load(Uri.parse(string))
        }
    }

    fun load(uri: Uri?) {
        LTime.tick()
        _checkLoad(uri) {
            if (checkGifType) {
                if (_loadSucceedUrl() == null) {
                    //首次加载
                    targetView?.setImageDrawable(placeholderDrawable)
                }
                _checkType(uri) {
                    onTypeCallback(it)
                    _load(uri, it == OkType.ImageType.GIF)
                }
            } else {
                _load(uri)
            }
        }
    }

    /**下载文件*/
    fun download(string: String?, callback: (File) -> Unit = {}) {
        clear()

        _glide()
            .download(GlideUrl(string, _header()))
            .override(Target.SIZE_ORIGINAL)
            .configRequest(string, File::class.java)
            .into(object : CustomTarget<File>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    callback(resource)
                }
            })
    }

    /**清理请求*/
    fun clear() {
        targetView?.clear()
    }

    /**重置属性, 回调不清理, Drawable不清理*/
    fun reset() {
        clear()
        checkGifType = false
        autoPlayGif = true
        originalSize = false
        transition = false
        overrideWidth = -1
        overrideHeight = -1
        transformations.clear()
        _loadSucceed(null)
    }

    /**添加一个模糊[Transformation]*/
    fun blur(radius: Int = 25, sampling: Int = 1) {
        addTransformation(SupportRSBlurTransformation(radius, sampling))
    }

    /**添加一个灰度[Transformation]*/
    fun grayscale() {
        addTransformation(GrayscaleTransformation())
    }

    /**追加一个转换器*/
    fun addTransformation(transformation: Transformation<Bitmap>) {
        transformations.add(transformation)
    }

    //</editor-fold desc="方法">

    //<editor-fold desc="辅助方法">

    inline fun _checkLoad(uri: Uri?, action: () -> Unit) {
        when {
            isDestroyed() -> L.w("activity isDestroyed!")
            targetView == null -> L.w("targetView is null!")
            isLoadSucceed(uri.loadUrl()) -> L.i("${uri.loadUrl()} is load succeed.")
            else -> action()
        }
    }

    //检查图片类型
    fun _checkType(uri: Uri?, action: (imageType: OkType.ImageType) -> Unit) {
        clear()
        if (uri == null) {
            action(OkType.ImageType.UNKNOWN)
        } else {
            OkType.type(uri, object : OkType.OnImageTypeListener {
                override fun onImageType(imageUrl: String, imageType: OkType.ImageType) {
                    L.d("type: $imageUrl ->$imageType")
                    action(imageType)
                }

                override fun onLoadStart() {
                    L.v("check $uri type.")
                }
            })?.attach()
        }
    }

    //正在加载的uri
    var _loadUri: Uri? = null

    //开始加载
    fun _load(uri: Uri?, asGif: Boolean = false) {
        clear()

        if (uri == null) {
            targetView?.setImageDrawable(placeholderDrawable ?: errorDrawable)
            return
        }

        _checkLoad(uri) {
            _loadUri = uri
            val targetView = targetView!!
            val path = uri.path
            val url: String? = uri.toString()

            if (uri.isHttpScheme()) {
                if (asGif && !url.isNullOrBlank()) {
                    _glide()
                        .download(GlideUrl(url, _header()))
                        .override(Target.SIZE_ORIGINAL)
                        .configRequest(url, File::class.java)
                        .into(GifDrawableImageViewTarget(targetView, autoPlayGif, transition))
                } else {
                    if (url.isNullOrBlank()) {
                        _glide()
                            .load(url)
                    } else if (url.isFileExist()) {
                        _glide()
                            .load(url)
                    } else {
                        _glide()
                            .load(GlideUrl(url, _header()))
                    }
                        .configRequest(url, Drawable::class.java)
                        .into(GlideDrawableImageViewTarget(targetView))
                }
            } else {
                if (asGif && !path.isNullOrBlank()) {
                    _glide()
                        .download(uri)
                        .override(Target.SIZE_ORIGINAL)
                        .configRequest(path, File::class.java)
                        .into(GifDrawableImageViewTarget(targetView, autoPlayGif, transition))
                } else {
                    if (path.isFileExist()) {
                        //优先直接加载路径
                        _glide()
                            .load(path)
                    } else {
                        //其次加载uri
                        _glide()
                            .load(uri)
                    }
                        .configRequest(path, Drawable::class.java)
                        .into(GlideDrawableImageViewTarget(targetView))
                }
            }
        }
    }

    fun _glide(): RequestManager {
        return Glide.with(targetView ?: View(app()).apply {
            L.w("注意:targetView is null!")
        })
    }

    /**停止Glide请求*/
    fun stop() {
        _glide().onStop()
    }

    /**恢复Glide请求*/
    fun start() {
        _glide().onStart()
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

    fun _header(): Headers {
        return LazyHeaders.Builder()
            .apply {
                onConfigHeader(this)
            }
            .build()
    }

    //标记加载成功的url
    fun _loadSucceed(url: String? = _loadUri?.loadUrl()) {
        targetView?.setTag(R.id.tag_glide_load_url, url)
    }

    //加载成功过的url
    fun _loadSucceedUrl() = targetView?.getTag(R.id.tag_glide_load_url)

    /**当前url是否已经加载成功*/
    fun isLoadSucceed(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }
        _loadSucceedUrl().apply {
            if (this == null) {
                return false
            }
            if (this is String) {
                if (this == url) {
                    return true
                }
            }
            return false
        }
    }

    //</editor-fold desc="辅助方法">

    //<editor-fold desc="辅助扩展">

    //附加请求
    fun Call.attach() {
        targetView?.setTag(R.id.tag_ok_type, this)
    }

    //配置请求
    @SuppressLint("CheckResult")
    fun <T> RequestBuilder<T>.configRequest(string: String?, model: Class<*>): RequestBuilder<T> {

        //override
        if (originalSize) {
            override(Target.SIZE_ORIGINAL)
        }
        if (this@DslGlide.overrideWidth > 0 && this@DslGlide.overrideHeight > 0) {
            override(this@DslGlide.overrideWidth, this@DslGlide.overrideHeight)
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
            when (model) {
                Bitmap::class.java -> transition(BitmapTransitionOptions.withCrossFade() as TransitionOptions<*, T>)
                Drawable::class.java -> transition(DrawableTransitionOptions.withCrossFade() as TransitionOptions<*, T>)
                File::class.java -> {
                }
            }
        }

        //thumbnail https://muyangmin.github.io/glide-docs-cn/doc/options.html#%E7%BC%A9%E7%95%A5%E5%9B%BE-thumbnail-%E8%AF%B7%E6%B1%82

        //https://github.com/wasabeef/glide-transformations
        transform(*this@DslGlide.transformations.toTypedArray())

        //other
        //disallowHardwareConfig()
        //format(DecodeFormat.PREFER_RGB_565)

        //listener
        addListener(object : RequestListener<T> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<T>?,
                isFirstResource: Boolean
            ): Boolean {
                L.w("$model 加载失败(${LTime.time()}) $e")
                _loadSucceed(null)
                return this@DslGlide.onLoadFailed(string, e)
            }

            override fun onResourceReady(
                resource: T,
                model: Any?,
                target: Target<T>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                L.w("$model 加载成功(${LTime.time()}) $dataSource")
                _loadSucceed(_loadUri.loadUrl())
                //targetView?.invalidate()
                return this@DslGlide.onLoadSucceed(string, resource)
            }
        })

        //custom
        onConfigRequest(this, model)

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
                ScaleType.CENTER, ScaleType.MATRIX -> Unit
                else -> Unit
            }
        }

        return this
    }

    //</editor-fold desc="辅助扩展">
}

fun dslGlide(imageView: ImageView, uri: Uri?, action: DslGlide.() -> Unit = {}) {
    DslGlide().apply {
        targetView = imageView
        if (placeholderDrawable == null) {
            imageView.drawable?.also {
                placeholderDrawable = it
            }
        }
        action()
        load(uri)
    }
}

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
