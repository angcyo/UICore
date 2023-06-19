package com.angcyo.dialog.other

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.MainThread
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.R
import com.angcyo.dialog.configCenterDialog
import com.angcyo.glide.giv
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.setWidthHeight
import com.angcyo.widget.DslViewHolder

/**
 * 简单的显示单图的对话框配置
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/19
 */
class SingleImageDialogConfig(context: Context? = null) : DslDialogConfig(context) {

    /**媒体uri*/
    var loadUri: Uri? = null

    /**本地内存对象*/
    var loadDrawable: Drawable? = null

    /**强行指定图片控件的宽高*/
    var imageWidth: Int = -1
    var imageHeight: Int = -1

    init {
        dialogLayoutId = R.layout.lib_dialog_single_image_layout
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        dialogViewHolder.gone(R.id.lib_close_view, !cancelable)
        dialogViewHolder.click(R.id.lib_close_view) {
            dialog.cancel()
        }

        dialogViewHolder.giv(R.id.lib_image_view)?.apply {
            if (imageWidth == -1 && imageHeight == -1) {
                parseImageSize()
            }
            if (imageWidth > 0 && imageHeight > 0) {
                setWidthHeight(imageWidth, imageHeight)
            }
            if (loadUri != null) {
                load(loadUri)
            } else {
                loadDrawable?.let { setImageDrawable(it) }
            }
        }
    }

    private fun parseImageSize() {
        loadUri?.let {
            val text = "$it"
            text.split("&").forEach { value ->
                val list = value.split("=")
                val key = list.getOrNull(0)?.toString()?.lowercase()
                if (key == "w" || key == "width") {
                    imageWidth = list.getOrNull(1)?.toIntOrNull() ?: -1
                } else if (key == "h" || key == "height") {
                    imageHeight = list.getOrNull(1)?.toIntOrNull() ?: -1
                }
            }
        }
    }
}

/**展示一张图片*/
@DSL
@MainThread
fun Context.singleImageDialog(
    drawable: Drawable?,
    config: SingleImageDialogConfig.() -> Unit = {}
): Dialog {
    return SingleImageDialogConfig(this).run {
        configCenterDialog(_screenWidth - 2 * 36 * dpi, -2)
        loadDrawable = drawable
        config()
        show()
    }
}

/**展示一张图片*/
@DSL
@MainThread
fun Context.singleImageDialog(uri: Uri?, config: SingleImageDialogConfig.() -> Unit = {}): Dialog {
    return SingleImageDialogConfig(this).run {
        configCenterDialog(-1, -1)
        loadUri = uri
        config()
        show()
    }
}
