package com.angcyo.core.dslitem

import android.graphics.drawable.ColorDrawable
import com.angcyo.core.R
import com.angcyo.core.component.file.FileItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.toTime
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.layout.RLayoutDelegate
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
class DslFileSelectorItem : DslAdapterItem() {

    var itemFile: FileItem? = null
    var itemShowFileMd5: Boolean = false

    init {
        itemLayoutId = R.layout.item_fragment_file_selector
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemFile?.also { item ->
            val bean = item.file
            itemHolder.tv(R.id.lib_text_view)?.text = bean.name
            itemHolder.tv(R.id.lib_time_view)?.text = bean.lastModified().toTime()

            //权限信息
            itemHolder.tv(R.id.file_auth_view)?.text = span {
                append(if (bean.isDirectory) "d" else "-")
                append(if (bean.canExecute()) "e" else "-")
                append(if (bean.canRead()) "r" else "-")
                append(if (bean.canWrite()) "w" else "-")
            }

            itemHolder.gone(R.id.file_md5_view)

            //文件/文件夹 提示信息
            when {
                bean.isDirectory -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(R.drawable.lib_ic_folder)
                    }
                    if (bean.canRead()) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text = "${bean.listFiles().size} 项"
                    }
                }
                bean.isFile -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(R.drawable.lib_ic_file)
//                        if (item.imageType != OkType.ImageType.UNKNOWN ||
//                            item.mimeType?.startsWith("video") == true
//                        ) {
//
//                        } else {
//                            setImageResource(R.drawable.lib_ic_file)
//                        }
                    }
                    if (bean.canRead()) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text =
                            "${bean.length().fileSizeString()} ${item.mimeType ?: ""}"
                    }

                    //MD5值
                    itemHolder.visible(
                        R.id.file_md5_view,
                        itemShowFileMd5 && !item.fileMd5.isNullOrBlank()
                    )
                    itemHolder.tv(R.id.file_md5_view)?.text = item.fileMd5
                }
                else -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageDrawable(null)
                    }
                    if (bean.canRead()) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text = "unknown"
                    }
                }
            }
        }

        if (itemIsSelected) {
            (itemHolder.itemView as? RLayoutDelegate)?.bDrawable =
                ColorDrawable(_color(R.color.colorAccent).alpha(0x30))
        } else {
            (itemHolder.itemView as? RLayoutDelegate)?.bDrawable = null
        }
    }
}