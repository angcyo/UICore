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

    companion object {

        /**获取文件类型图标*/
        fun getFileIconRes(fileName: String?): Int {
            val name = fileName?.lowercase() ?: return R.drawable.core_file_icon_unknown
            return when {
                name.isImageType() -> R.drawable.core_file_icon_picture
                name.isVideoType() -> R.drawable.core_file_icon_video
                name.isAudioType() -> R.drawable.core_file_icon_audio
                name.endsWith(".zip") -> R.drawable.core_file_icon_zip
                name.endsWith(".7z") -> R.drawable.core_file_icon_7z
                name.endsWith(".rar") -> R.drawable.core_file_icon_rar
                name.endsWith(".log") -> R.drawable.core_file_icon_log
                name.endsWith(".txt") -> R.drawable.core_file_icon_text
                name.endsWith(".xml") -> R.drawable.core_file_icon_xml
                else -> R.drawable.core_file_icon_unknown
            }
        }

    }

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
            val file = fileItem.file()
            itemHolder.tv(R.id.lib_text_view)?.text = file?.name
            itemHolder.tv(R.id.lib_time_view)?.text = file?.lastModified()?.toTime()

            //权限信息
            itemHolder.tv(R.id.file_auth_view)?.text = span {
                append(if (file.isFolder()) "d" else "-")
                append(if (file?.canExecute() == true) "e" else "-")
                append(if (file?.canRead() == true) "r" else "-")
                append(if (file?.canWrite() == true) "w" else "-")
            }

            itemHolder.gone(R.id.file_md5_view)

            //文件/文件夹 提示信息
            when {
                file.isFolder() -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(R.drawable.core_file_icon_folder)
                    }
                    if (file?.canRead() == true) {
                        itemHolder.tv(R.id.lib_sub_text_view)?.text = "${fileItem.fileCount} 项"
                    }
                }
                file.isFile() -> {
                    itemHolder.img(R.id.lib_image_view)?.apply {
                        setImageResource(getFileIconRes(file?.name))
                        onLoadImageView(this, fileItem)
                    }
                    if (file?.canRead() == true) {
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
                    if (file?.canRead() == true) {
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