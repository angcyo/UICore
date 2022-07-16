package com.angcyo.glide

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import com.angcyo.dsladapter.internal.DrawText
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.widget.image.DslImageView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.Request

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

open class GlideImageView : DslImageView {

    var showDebugInfo: Boolean = false

    val dslGlide: DslGlide by lazy {
        DslGlide().apply {
            targetView = this@GlideImageView
        }
    }

    val debugInfoDraw: DrawText by lazy {
        DrawText()
    }

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
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GlideImageView)
        showDebugInfo =
            typedArray.getBoolean(R.styleable.GlideImageView_r_show_debug_info, showDebugInfo)
        enableShape = typedArray.getBoolean(R.styleable.ShapeImageView_r_enable_shape, true)
        typedArray.recycle()
        dslGlide.placeholderDrawable = drawable
    }

    override fun setImageDrawable(drawable: Drawable?) {
        try {
            super.setImageDrawable(drawable)
        } catch (e: Exception) {
            L.w(e)
        }

        if (!isInEditMode) {
            drawable?.apply {
                L.v("${this@GlideImageView.simpleHash()}:${this.simpleHash()} w:$minimumWidth:$measuredWidth h:$minimumHeight:$measuredHeight")
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val tag = tag

        var isInGlide = false
        if (tag != null) {
            if (tag is Request) {
                //load is glide.
                isInGlide = true
            }
        }

        if (!isInGlide) {
            //Glide自行管理回收, 不需要手动处理.否则Glide回收时会崩溃
            if (drawable is GifDrawable) {
                //(drawable as GifDrawable).recycle()
                setImageDrawable(null)
            } else if (drawable is pl.droidsonroids.gif.GifDrawable) {
                (drawable as pl.droidsonroids.gif.GifDrawable).recycle()
                setImageDrawable(null)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (showDebugInfo) {
            debugInfoDraw.apply {
                drawText = buildString {
                    append(width)
                    append("x")
                    append(height)
                    appendln()
                    append(dslGlide._loadUri?.toString())
                }
                textWidth = measuredWidth
                makeLayout().apply {
                    canvas.translate(0f, (this@GlideImageView.height - height).toFloat())
                    onDraw(canvas)
                }
            }
        }
    }

    //<editor-fold desc="操作">

    open fun load(url: String?, action: DslGlide.() -> Unit = {}) {
        dslGlide.apply {
            action()
            load(url)
        }
    }

    open fun load(uri: Uri?, action: DslGlide.() -> Unit = {}) {
        dslGlide.apply {
            action()
            load(uri)
        }
    }

    //</editor-fold desc="操作">
}