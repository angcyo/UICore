package com.angcyo.pager.dslitem

import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.loadImage
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.anim
import com.github.chrisbanes.photoview.OnViewTapListener
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */
open class DslPhotoViewItem : DslAdapterItem() {
    init {
        itemLayoutId = R.layout.dsl_photo_view_item
    }

    var imageUrl: String? = null

    /**占位图获取*/
    var placeholderDrawableProvider: IPlaceholderDrawableProvider? = null

    /**[PhotoView]点击事件*/
    var onPhotoViewTapListener: OnViewTapListener =
        OnViewTapListener { view, x, y ->
            (view as PhotoView).apply {
                if (scale > 1) {
                    setScale(1f, true)
                } else {
                    onItemClick?.invoke(this)
                }
            }
        }

    /**[PhotoView]长按事件*/
    var onPhotoViewLongClickListener: View.OnLongClickListener = View.OnLongClickListener { false }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.img(R.id.lib_image_view)?.apply {
            loadImage(imageUrl) {
                //检查gif
                checkGifType = true
                //使用原始大小
                originalSize = true

                //占位图
                placeholderDrawableProvider?.getPlaceholderDrawable(imageUrl)?.run {
                    placeholderDrawable = this
                }

                //加载中状态提示
                val loadingView = itemHolder.view(R.id.lib_loading_view)
                itemHolder.visible(loadingView)
                onLoadSucceed = { _, _ ->
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
                            onAnimatorUpdateValue = { _, _ ->
                                (targetView as? PhotoView)?.scale = 1f
                            }
                        }
                    }
                    false
                }
            }

            if (this is PhotoView) {
                this.setOnViewTapListener(onPhotoViewTapListener)
                this.setOnLongClickListener(onPhotoViewLongClickListener)
            }
        }
    }
}