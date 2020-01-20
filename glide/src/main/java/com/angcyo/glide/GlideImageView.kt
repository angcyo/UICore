package com.angcyo.glide

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.IdRes
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.doOnPreDraw
import com.angcyo.widget.image.DslImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

open class GlideImageView : DslImageView {

    /**设置需要加载的url, 支持本地路径, GIF, 视频*/
    var url: String? = null
        set(value) {
            val old = field
            field = value
//            if (old != value) {
            if (value == null) {
                clear()
            } else {
                load(value)
            }
//            }
        }

    var placeholderDrawable: Drawable? = null
    var errorDrawable: Drawable? = null

    /**Target.SIZE_ORIGINAL*/
    var originalSize: Boolean = false
    var overrideSize: Boolean = true
    //强制指定override的宽高
    var overrideWidth: Int = -1
    var overrideHeight: Int = -1

    var onConfigRequest: (builder: RequestBuilder<*>) -> Unit = {}

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        placeholderDrawable = drawable
    }

    //<editor-fold desc="Glide操作">

    fun _glide(): RequestManager {
        return Glide.with(this)
    }

    fun clear() {
        _glide().clear(this)
    }

    fun _override(action: () -> Unit) {
        if (overrideSize &&
            measuredWidth <= 0 && measuredHeight <= 0 &&
            overrideWidth <= 0 && overrideHeight <= 0
        ) {
            doOnPreDraw {
                action()
            }
        } else {
            action()
        }
    }

    /**请求配置*/
    fun RequestBuilder<*>._configRequest() {
        RequestOptions().apply {
            //override
            if (originalSize) {
                override(Target.SIZE_ORIGINAL)
            } else if (overrideSize) {
                if (overrideWidth > 0 && overrideHeight > 0) {
                    override(overrideWidth, overrideHeight)
                } else {
                    override(measuredWidth, measuredHeight)
                }
            }

            //drawable
            this@GlideImageView.placeholderDrawable?.run {
                placeholder(this)
            }
            errorDrawable?.run {
                error(this)
            }

            when (scaleType) {
                ScaleType.CENTER_CROP -> centerCrop()
                ScaleType.CENTER_INSIDE -> centerInside()
                ScaleType.CENTER -> centerInside()
                ScaleType.FIT_CENTER -> fitCenter()
                else -> {
                }
            }

            //自定义回调
            onConfigRequest(this@_configRequest)

            this@_configRequest.apply(this)
        }
    }

    fun load(url: String) {
        if (context is Activity) {
            if ((context as Activity).isDestroyed) {
                return
            }
        }

        _override {
            _glide().load(url).apply {
                _configRequest()
                into(this@GlideImageView)
            }
        }
    }

    //</editor-fold desc="Glide操作">

    //<editor-fold desc="操作">

    /**重置属性*/
    fun reset() {
        clear()
        originalSize = false
        overrideSize = true
        onConfigRequest = {}
    }

    //</editor-fold desc="操作">
}

fun DslViewHolder.giv(@IdRes id: Int): GlideImageView? {
    return v(id)
}