package com.angcyo.item.style

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.isUpdateMedia
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.glide.DslGlide
import com.angcyo.glide.GlideImageView
import com.angcyo.glide.R
import com.angcyo.glide.loadAvatar
import com.angcyo.library.L
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.toUri
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.image.DslImageView

/**
 * 使用单个[GlideImageView]显示图片的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IImageItem : IAutoInitItem {

    var imageItemConfig: ImageItemConfig

    /**初始化*/
    @ItemInitEntryPoint
    fun initImageItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //更新媒体
        val mediaUpdate = payloads.isUpdateMedia() || imageItemConfig.itemLoadImageFlag

        itemHolder.img(imageItemConfig.itemImageViewId)?.apply {
            if (this is GlideImageView) {
                imageItemConfig.onConfigImageView(this)
            }

            //2021-10-26 不必要的设置
            /*if (this@IImageItem is DslAdapterItem) {
                setOnClickListener(_clickListener)
            }*/
        }

        if (mediaUpdate) {
            //缩略图
            itemHolder.img(imageItemConfig.itemImageViewId)?.apply {
                imageItemConfig.itemLoadImageFlag = false

                imageItemConfig.imageStyleConfig.updateStyle(this)

                when (val image = imageItemConfig.itemLoadImage ?: imageItemConfig.itemLoadUri) {
                    is Number -> setImageResource(image as Int)
                    is Drawable -> setImageDrawable(image)
                    is Bitmap -> setImageBitmap(image)
                    else -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && image is Icon) {
                            setImageIcon(image)
                        } else {
                            if (this is GlideImageView) {
                                val uri: Uri? = when {
                                    image is String && image.isNotEmpty() -> image.toUri()
                                    image is Uri -> image
                                    else -> null
                                }

                                val itemLoadImageText = imageItemConfig.itemLoadImageText
                                if (uri == null && itemLoadImageText != null) {
                                    loadAvatar(
                                        null,
                                        itemLoadImageText,
                                        imageItemConfig.itemLoadImageTextColor,
                                        solidColor = imageItemConfig.itemLoadImageTextBgColor
                                    )
                                } else {
                                    load(uri) {
                                        checkGifType = imageItemConfig.itemCheckGifType
                                        imageItemConfig.onConfigGlide(this)
                                    }
                                }
                            } else {
                                L.w("注意->控件不是:${GlideImageView::class.java.name}类型,无法加载图片")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**此方式仅支持[Uri], 请使用[itemLoadImage]*/
var IImageItem.itemLoadUri: Uri?
    get() = imageItemConfig.itemLoadUri ?: if (itemLoadImage is String) {
        (itemLoadImage as String).toUri()
    } else {
        null
    }
    set(value) {
        imageItemConfig.itemLoadUri = value
    }

/**支持[AnyImage]*/
var IImageItem.itemLoadImage: AnyImage?
    get() = imageItemConfig.itemLoadImage
    set(value) {
        imageItemConfig.itemLoadImage = value
    }

var IImageItem.itemLoadImageText: String?
    get() = imageItemConfig.itemLoadImageText
    set(value) {
        imageItemConfig.itemLoadImageText = value
    }

var IImageItem.itemLoadImageTextBgColor: Int
    get() = imageItemConfig.itemLoadImageTextBgColor
    set(value) {
        imageItemConfig.itemLoadImageTextBgColor = value
    }

/**
 * 支持[res]图片
 * 支持[String] http图片, file图片
 * 支持[Uri]图片
 * 支持[Drawable]图片
 * 支持[Bitmap]图片
 * 支持[Icon]图片
 * */
typealias AnyImage = Any

class ImageItemConfig : IDslItemConfig {

    /**[R.id.lib_image_view]*/
    var itemImageViewId: Int = R.id.lib_image_view

    /**检查gif图片类型, 如果是会使用[GifDrawable]*/
    var itemCheckGifType: Boolean = true

    /**是否需要加载图片的标识, 加载图片后, 自动恢复[false]*/
    var itemLoadImageFlag: Boolean = true

    /**加载的媒体*/
    @Deprecated("已废弃,请使用[itemLoadImage],2021-9-23")
    var itemLoadUri: Uri? = null

    /**加载的媒体*/
    var itemLoadImage: AnyImage? = null
        set(value) {
            val old = field
            field = value
            if (old != value) {
                itemLoadImageFlag = true
            }
        }

    /**当[itemLoadImage]为空时, 需要绘制的文本*/
    var itemLoadImageText: String? = null

    /**绘制[itemLoadImageText]时的文本颜色*/
    var itemLoadImageTextColor: Int = Color.WHITE

    /**绘制[itemLoadImageText]时的背景颜色*/
    var itemLoadImageTextBgColor: Int = getColor(R.color.colorPrimaryDark)

    /**图片控件样式调整*/
    var imageStyleConfig = ImageStyleConfig().apply {
        imageScaleType = ImageView.ScaleType.CENTER_CROP
    }

    /**配置[DslGlide]*/
    var onConfigGlide: (DslGlide) -> Unit = {

    }

    /**配置[DslImageView]*/
    var onConfigImageView: (GlideImageView) -> Unit = {

    }
}