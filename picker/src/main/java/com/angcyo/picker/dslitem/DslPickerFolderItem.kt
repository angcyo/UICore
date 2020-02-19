package com.angcyo.picker.dslitem

import android.widget.ImageView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.giv
import com.angcyo.library.ex._color
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.or
import com.angcyo.loader.LoaderFolder
import com.angcyo.loader.isAudio
import com.angcyo.loader.loadUri
import com.angcyo.loader.mediaCount
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

/**
 * 图片选择器, 文件夹选择布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */
class DslPickerFolderItem : DslAdapterItem() {

    var loaderFolder: LoaderFolder? = null
        get() = field ?: (itemData as? LoaderFolder)

    var showFolderLine: Boolean = false

    init {
        itemLayoutId = R.layout.dsl_picker_folder_layout
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        //name
        itemHolder.tv(R.id.text_view)?.text = span {
            append(loaderFolder?.folderName?.or("--"))
            append(" (${loaderFolder?.mediaCount() ?: 0})") {
                foregroundColor = _color(R.color.picker_bottom_text_color)
            }
        }

        //image
        if (loaderFolder?.mediaItemList?.firstOrNull()?.isAudio() == true) {
            //audio
            itemHolder.img(R.id.lib_image_view)?.apply {
                scaleType = ImageView.ScaleType.CENTER
                setImageDrawable(_drawable(R.drawable.lib_audio_cover_tip))
            }
        } else {
            itemHolder.giv(R.id.lib_image_view)?.apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                load(loaderFolder?.mediaItemList?.firstOrNull()?.loadUri())
            }
        }

        //line
        itemHolder.visible(R.id.lib_line_view, showFolderLine)

        //check
        itemHolder.visible(R.id.lib_tip_image_view, itemIsSelected)

    }
}