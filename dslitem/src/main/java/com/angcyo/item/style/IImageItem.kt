package com.angcyo.item.style

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isUpdateMedia
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.glide.DslGlide
import com.angcyo.glide.GlideImageView
import com.angcyo.glide.R
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
    fun initImageItem(itemHolder: DslViewHolder, payloads: List<Any>) {
        //更新媒体
        val mediaUpdate = payloads.isUpdateMedia()

        itemHolder.img(imageItemConfig.itemImageViewId)?.apply {
            if (this is GlideImageView) {
                imageItemConfig.onConfigImageView(this)
            }

            if (this@IImageItem is DslAdapterItem) {
                setOnClickListener(_clickListener)
            }
        }

        if (mediaUpdate) {
            //缩略图
            itemHolder.img(imageItemConfig.itemImageViewId)?.apply {
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
                                val uri: Uri? = when (image) {
                                    is String -> image.toUri()
                                    is Uri -> image
                                    else -> null
                                }
                                load(uri) {
                                    checkGifType = imageItemConfig.itemCheckGifType
                                    imageItemConfig.onConfigGlide(this)
                                }
                            }
                        }
                        //L.w("不支持的图片类型:${image}")
                    }
                }
            }
        }
    }
}

var IImageItem.itemLoadUri: Uri?
    get() = imageItemConfig.itemLoadUri
    set(value) {
        imageItemConfig.itemLoadUri = value
    }

var IImageItem.itemLoadImage: AnyImage?
    get() = imageItemConfig.itemLoadImage
    set(value) {
        imageItemConfig.itemLoadImage = value
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

    /**加载的媒体*/
    @Deprecated("已废弃,请使用[itemLoadImage],2021-9-23")
    var itemLoadUri: Uri? = null

    /**加载的媒体*/
    var itemLoadImage: AnyImage? = null

    var imageStyleConfig = ImageStyleConfig().apply {
        imageScaleType = ImageView.ScaleType.CENTER_CROP
    }

    var imageResStyleConfig = ImageStyleConfig().apply {
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }

    /**配置[DslGlide]*/
    var onConfigGlide: (DslGlide) -> Unit = {

    }

    /**配置[DslImageView]*/
    var onConfigImageView: (GlideImageView) -> Unit = {

    }
}