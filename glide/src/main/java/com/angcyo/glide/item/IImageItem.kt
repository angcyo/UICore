package com.angcyo.glide.item

import android.net.Uri
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isUpdateMedia
import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.glide.DslGlide
import com.angcyo.glide.GlideImageView
import com.angcyo.glide.R
import com.angcyo.glide.giv
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.image.DslImageView

/**
 * 使用单个[GlideImageView]显示图片的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IImageItem : IDslItem {

    /**[R.id.lib_image_view]*/
    var itemImageViewId: Int

    /**检查gif图片类型, 如果是会使用[GifDrawable]*/
    var itemCheckGifType: Boolean

    /**加载的媒体*/
    var itemLoadUri: Uri?

    /**配置[DslGlide]*/
    var onConfigGlide: (DslGlide) -> Unit

    /**配置[DslImageView]*/
    var onConfigImageView: (GlideImageView) -> Unit

    /**初始化*/
    fun initImageItem(itemHolder: DslViewHolder, payloads: List<Any>) {
        //更新媒体
        val mediaUpdate = payloads.isUpdateMedia()

        itemHolder.giv(itemImageViewId)?.apply {
            onConfigImageView(this)

            if (this@IImageItem is DslAdapterItem) {
                setOnClickListener(_clickListener)
            }
        }

        if (mediaUpdate) {
            //缩略图
            itemHolder.giv(itemImageViewId)?.apply {
                load(itemLoadUri) {
                    checkGifType = itemCheckGifType
                    onConfigGlide(this)
                }
            }
        }
    }
}