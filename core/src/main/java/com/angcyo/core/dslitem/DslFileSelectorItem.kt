package com.angcyo.core.dslitem

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.angcyo.core.R
import com.angcyo.core.component.file.FileItem
import com.angcyo.core.component.file.file
import com.angcyo.core.component.manage.InnerFileManageModel
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.isAudioType
import com.angcyo.library.ex.isFile
import com.angcyo.library.ex.isFolder
import com.angcyo.library.ex.isImageType
import com.angcyo.library.ex.isVideoType
import com.angcyo.library.ex.mimeType
import com.angcyo.library.ex.toStr
import com.angcyo.library.ex.toTime
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setRBgDrawable
import com.angcyo.widget.span.span
import java.io.File

/**
 * 文件选择器, 文件列表item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
open class DslFileSelectorItem : DslAdapterItem() {

    companion object {

        /**获取文件类型图标
         * [fileName] 文件名, 包含后缀
         * [def] 未知类型时的默认图标
         * [extMap] 自定义扩展名对应的图标
         * */
        fun getFileIconRes(
            fileName: String?,
            extMap: Map<String, Int>? = InnerFileManageModel.innerFileIconMap,
            def: Int = R.drawable.core_file_icon_unknown,
        ): Int {
            val name = fileName?.lowercase() ?: return def
            val extName = name.substringAfterLast(".")
            extMap?.get(extName)?.let {
                return it
            }
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
                name.endsWith(".apk") -> R.drawable.core_file_icon_apk
                else -> def
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

        val file = itemFile?.file()
        val itemFileName = getItemFileName(file)
        itemHolder.tv(R.id.lib_text_view)?.text = itemFileName
        itemHolder.tv(R.id.lib_time_view)?.text = getItemFileLastModified(file)?.toTime()

        //权限信息
        itemHolder.tv(R.id.file_auth_view)?.text = span {
            append(if (itemIsFolder(file)) "d" else "-")
            append(if (itemCanExecute(file)) "e" else "-")
            append(if (itemCanRead(file)) "r" else "-")
            append(if (itemCanWrite(file)) "w" else "-")
        }

        itemHolder.gone(R.id.file_md5_view)

        //文件/文件夹 提示信息
        when {
            itemIsFolder(file) -> {
                itemHolder.img(R.id.lib_image_view)?.apply {
                    setImageResource(R.drawable.core_file_icon_folder)
                }
                if (itemCanRead(file)) {
                    itemHolder.tv(R.id.lib_sub_text_view)?.text = "${getSubFileCount()} 项"
                }
            }

            itemIsFile(file) -> {
                itemHolder.img(R.id.lib_image_view)?.apply {
                    setImageResource(getFileIconRes(itemFileName?.toStr()))
                    itemFile?.let { onLoadImageView(this, it) }
                }
                if (itemCanRead(file)) {
                    itemHolder.tv(R.id.lib_sub_text_view)?.text =
                        "${getItemFileLength(file)?.fileSizeString() ?: ""} ${
                            getItemFileMimeType(file) ?: ""
                        }"
                }

                //MD5值
                val fileMd5 = getItemFileMd5(file)
                itemHolder.visible(R.id.file_md5_view, itemShowFileMd5 && !fileMd5.isNullOrBlank())
                itemHolder.tv(R.id.file_md5_view)?.text = fileMd5
            }

            else -> {
                itemHolder.img(R.id.lib_image_view)?.apply {
                    setImageDrawable(null)
                }
                if (itemCanRead(file)) {
                    itemHolder.tv(R.id.lib_sub_text_view)?.text = "unknown"
                }
            }
        }

        if (itemIsSelected) {
            itemHolder.itemView.setRBgDrawable(
                ColorDrawable(_color(R.color.colorAccentNight).alpha(0x30))
            )
        } else {
            itemHolder.itemView.setRBgDrawable(null)
        }
    }

    //---

    open fun getItemFileName(file: File?): CharSequence? = file?.name

    open fun getItemFileLastModified(file: File?): Long? = file?.lastModified()
    open fun getSubFileCount(): Long = itemFile?.fileCount ?: 0L
    open fun getItemFileLength(file: File?) = itemFile?.fileLength
    open fun getItemFileMimeType(file: File?) =
        itemFile?.mimeType ?: getItemFileName(file)?.toStr()?.mimeType()

    open fun getItemFileMd5(file: File?) = itemFile?.fileMd5

    open fun itemIsFolder(file: File?) = file.isFolder()
    open fun itemIsFile(file: File?) = file.isFile()
    open fun itemCanExecute(file: File?) = file?.canExecute() == true
    open fun itemCanRead(file: File?) = file?.canRead() == true
    open fun itemCanWrite(file: File?) = file?.canWrite() == true
}