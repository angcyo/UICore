package com.angcyo.doodle

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.base.translucentStatusBar
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.glide.loadImage
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.anim
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.github.chrisbanes.photoview.OnViewTapListener
import com.github.chrisbanes.photoview.PhotoView

/**
 *
 * 简单的图片查看对话框配置
 *
 * [com.angcyo.image.dslitem.DslPhotoViewItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/22
 */
open class SinglePhotoViewDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    /**媒体uri*/
    var photoLoadUri: Uri? = null

    /**[PhotoView]点击事件*/
    var onPhotoViewTapListener: OnViewTapListener = OnViewTapListener { view, x, y ->
        (view as PhotoView).apply {
            if (scale > 1) {
                setScale(1f, true)
            } else {
                _dialog?.cancel()
            }
        }
    }

    /**[PhotoView]长按事件*/
    var onPhotoViewLongClickListener: View.OnLongClickListener = View.OnLongClickListener { false }

    init {
        dialogLayoutId = R.layout.dialog_single_photo_view
        dialogWidth = -1
        dialogHeight = -1

        animStyleResId = R.style.LibDialogAlphaAnimation
        setDialogBgColor(Color.TRANSPARENT)
    }

    override fun configWindow(window: Window) {
        super.configWindow(window)
        window.enableLayoutFullScreen(true)
        window.translucentStatusBar(true)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.img(R.id.lib_image_view)?.apply {
            loadImage(dialogViewHolder, this)
            if (this is PhotoView) {
                this.setOnViewTapListener(onPhotoViewTapListener)
                this.setOnLongClickListener(onPhotoViewLongClickListener)
            }
        }

        //确定按钮
        val positiveButton = dialogViewHolder.view(R.id.dialog_positive_button)
        positiveButton?.apply {
            if (positiveButton is TextView) {
                positiveButton.text = positiveButtonText
            }

            clickIt {
                positiveButtonListener?.invoke(dialog, dialogViewHolder)
            }
        }
    }

    open fun loadImage(itemHolder: DslViewHolder, imageView: ImageView) {
        imageView.apply {
            val loadUri = photoLoadUri
            loadImage(loadUri) {
                //检查gif
                checkGifType = true
                //使用原始大小
                originalSize = true

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
        itemHolder.visible(R.id.dialog_positive_button, positiveButtonText != null)
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

@DSL
fun Context.singlePhotoViewDialog(
    uri: Uri?,
    config: SinglePhotoViewDialogConfig.() -> Unit = {}
): Dialog {
    return SinglePhotoViewDialogConfig(this).run {
        photoLoadUri = uri
        config()
        show()
    }
}