package com.angcyo.image.dslitem

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.loadImage
import com.angcyo.image.R
import com.angcyo.library.ex.anim
import com.angcyo.widget.DslViewHolder
import com.github.chrisbanes.photoview.OnViewTapListener
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */
open class DslPhotoViewItem : DslAdapterItem() {

    /**媒体uri*/
    open var itemLoadUri: Uri? = null

    /**占位图获取*/
    var drawableProvider: IDrawableProvider? = null

    /**[PhotoView]点击事件*/
    var onPhotoViewTapListener: OnViewTapListener = OnViewTapListener { view, x, y ->
        (view as PhotoView).apply {
            if (scale > 1) {
                setScale(1f, true)
            } else {
                itemClick?.invoke(this)
            }
        }
    }

    /**[PhotoView]长按事件*/
    var onPhotoViewLongClickListener: View.OnLongClickListener = View.OnLongClickListener { false }

    init {
        itemLayoutId = R.layout.dsl_photo_view_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.img(R.id.lib_image_view)?.apply {
            loadImage(itemHolder, this)
            if (this is PhotoView) {
                this.setOnViewTapListener(onPhotoViewTapListener)
                this.setOnLongClickListener(onPhotoViewLongClickListener)
            }
        }
    }

    open fun loadImage(itemHolder: DslViewHolder, imageView: ImageView) {
        imageView.apply {
            val loadUri = itemLoadUri
            loadImage(loadUri) {
                //检查gif
                checkGifType = true
                //使用原始大小
                originalSize = true

                //占位图
                drawableProvider?.getPlaceholderDrawable(loadUri)?.run {
                    placeholderDrawable = this
                }

                //加载中状态提示
                val loadingView = itemHolder.view(R.id.lib_loading_view)
                itemHolder.visible(loadingView)
                onLoadSucceed = { _, _ ->
                    onImageLoadSucceed(itemHolder, imageView)
                    false
                }
            }
        }
    }

    open fun onImageLoadSucceed(itemHolder: DslViewHolder, targetView: ImageView) {
        //加载中状态提示
        val loadingView = itemHolder.view(R.id.lib_loading_view)
        itemHolder.view(R.id.lib_loading_view)?.run {
            animate().alpha(0f)
                .setDuration(300)
                .withEndAction {
                    loadingView?.alpha = 1f
                    itemHolder.gone(loadingView)
                }
                .start()
        }
        if (targetView is PhotoView) {
            //有一个莫名其妙的BUG, 占位图 喧宾夺主
            anim(0f, 1f) {
                onAnimatorUpdateValue = { value, _ ->
                    (targetView as? PhotoView)?.run {
                        //scale = 1f
                        setRotationTo(value as Float * 0.01f)
                    }
                }
            }
            //(targetView as? PhotoView)?.setScale(1f, true)
        }
    }
}