package com.angcyo.library.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.angcyo.library.L
import com.angcyo.library.ex.bitmapSuffix
import java.io.File


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */

object Media {

    /**
     * 将文件保存到公共的媒体文件夹
     * 这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似
     * 这里的filename单纯的指文件名，不包含路径
     * */
    @Deprecated("代码待测试")
    fun saveSignImage(contxt: Context, filePath: String?, fileName: String?, bitmap: Bitmap) {
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/signImage")
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(
                    MediaStore.Images.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                )
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri: Uri? = contxt.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                contxt.contentResolver.openOutputStream(uri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
            }
        } catch (e: Exception) {
            L.w(e)
        }
    }

    fun targetFile(source: File, path: String, width: Int, height: Int): File {
        val sourcePath = source.absolutePath
        val suffix = sourcePath.bitmapSuffix()

        val targetFileName = fileNameUUID()
        val targetFilePath =
            "${path}${File.separator}${targetFileName}_s_${width}x${height}.$suffix"

        return File(targetFilePath)
    }

    /**重命名*/
    fun renameFrom(source: File, path: String, width: Int, height: Int): File {
        val targetFile = targetFile(source, path, width, height)
        targetFile.createNewFile()
        val result = source.renameTo(targetFile)
        return if (result) {
            targetFile
        } else {
            source
        }
    }

    /**复制*/
    fun copyFrom(source: File, path: String, width: Int, height: Int): File {
        val targetFile = targetFile(source, path, width, height)

        if (targetFile.createNewFile()) {
            source.copyTo(targetFile, true)
        }
        return targetFile
    }

}