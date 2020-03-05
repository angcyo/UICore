package com.angcyo.core.dslitem

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.angcyo.core.R
import com.angcyo.core.component.file.FileItem
import com.angcyo.core.component.file.file
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setRBgDrawable
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
class DslFileSelectorItem : DslAdapterItem() {

    /**加载的数据*/
    var itemFile: FileItem? = null

    /**是否显示文件MD5值, 如果有*/
    var itemShowFileMd5: Boolean = false

    /**外包扩展加载图片, 视频*/
    var onLoadImageView: (imageView: ImageView, fileItem: FileItem) -> Unit = { _, _ ->

    }

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
        itemFile?.also { fileItem ->
            val fileBean = fileItem.file()
            itemHolder.tv(R.id.lib_text_view)?.text = fileBean?.name
            itemHolder.tv(R.id.lib_time_view)?.text = fileBean?.lastModified()?.toTime()

            //权限信息
            itemHolder.tv(R.id.file_auth_view)?.text = span {
                append(if (fileBean.isFolder()) "d" else "-")
                append(if (fileBean?.canExecute() == true) "e" else "-")
                append(if (fileBean?.canRead() == true) "r" else "-")
                append(if (fileBean?.canWrite() == true) "w" else "-")
            }

            itemHolder.gone(R.id.file_md5_view)

            //文件/文件夹 提示信息
            when {
                fileBean.isFolder() -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(R.drawable.lib_ic_folder)
                    }
                    if (fileBean?.canRead() == true) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text = "${fileItem.fileCount} 项"
                    }
                }
                fileBean.isFile() -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(R.drawable.lib_ic_file)
                        onLoadImageView(this, fileItem)
                    }
                    if (fileBean?.canRead() == true) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text =
                            "${fileItem.fileLength.fileSizeString()} ${fileItem.mimeType ?: ""}"
                    }

                    //MD5值
                    itemHolder.visible(
                        R.id.file_md5_view,
                        itemShowFileMd5 && !fileItem.fileMd5.isNullOrBlank()
                    )
                    itemHolder.tv(R.id.file_md5_view)?.text = fileItem.fileMd5
                }
                else -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageDrawable(null)
                    }
                    if (fileBean?.canRead() == true) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text = "unknown"
                    }
                }
            }
        }

        if (itemIsSelected) {
            itemHolder.itemView.setRBgDrawable(ColorDrawable(_color(R.color.colorAccent).alpha(0x30)))
        } else {
            itemHolder.itemView.setRBgDrawable(null)
        }
    }
}