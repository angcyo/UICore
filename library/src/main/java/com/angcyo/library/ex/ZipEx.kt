package com.angcyo.library.ex

import com.angcyo.library.libCacheFile
import com.angcyo.library.libCacheFolderPath
import com.angcyo.library.utils.fileNameUUID
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 压缩/解压文件工具
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/06
 */

/**压缩文件列表到指定的压缩包中
 *
 * [zipFilePath] zip文件输出路径
 * @return 成功:返回对应的路径 or 失败:返回空
 * */
fun List<File>.zipFile(zipFilePath: String = libCacheFile(fileNameUUID(".zip")).absolutePath): String? {
    return try {
        val zip = ZipOutputStream(FileOutputStream(zipFilePath))
        forEach {
            try {
                zip.writeEntry(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        zip.finish()
        zip.close()
        zipFilePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun List<String>.zip(zipFilePath: String = libCacheFile(fileNameUUID(".zip")).absolutePath): String? {
    return mapTo(mutableListOf()) { it.file() }.zipFile(zipFilePath)
}

/**向zip流中, 写入文件/文件夹
 * [file] 支持文件/文件夹
 * [parentFolderName] 可以为空字符, xxx/
 * */
fun ZipOutputStream.writeEntry(file: File, parentFolderName: String = "") {
    var entryName = parentFolderName + file.name
    if (!file.exists()) {
        //不存在的文件
        val entry = ZipEntry(entryName)
        putNextEntry(entry)
        closeEntry()
    } else if (file.isFile) {
        //文件
        val entry = ZipEntry(entryName)
        putNextEntry(entry)
        val inputStream = FileInputStream(file)
        inputStream.writeTo(this)
        inputStream.close()
        closeEntry()
    } else {
        //文件夹
        entryName += File.separator // +/
        val fileList = file.listFiles()
        if (fileList.isNullOrEmpty()) {
            //空文件夹
            val entry = ZipEntry(entryName)
            putNextEntry(entry)
            closeEntry()
        } else {
            //递归加入
            fileList.forEach {
                try {
                    writeEntry(it, entryName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

//---

/**解压zip文件
 * [outputPath] 输出的文件夹
 * @return 成功/失败*/
fun File.unzipFile(outputPath: String = libCacheFolderPath()): String? {
    return try {
        val unzipFolder = outputPath + File.separator + name.noExtName() // outputPath/zip name/xxx
        val outputFile = File(unzipFolder)
        outputFile.mkdirs()
        val zipFile = ZipFile(this)
        val entries = zipFile.entries()

        while (entries.hasMoreElements()) {
            try {
                val zipEntry = entries.nextElement()
                zipEntry.writeTo(unzipFolder, zipFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        unzipFolder
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**[unzipFile]*/
fun String.unzipFile(outputPath: String = libCacheFolderPath()): String? =
    file().unzipFile(outputPath)

/**将[ZipEntry]输出到指定的文件夹
 * 支持文件/文件夹*/
fun ZipEntry.writeTo(outputPath: String, zipFile: ZipFile) {
    var name = name
    if (name.endsWith(File.separator)) {
        name = name.substring(0, name.length - 1) // xxx/ 移除最后的/
    }
    val file = File(outputPath, name)
    if (isDirectory) {
        //文件夹
        file.mkdirs()
    } else {
        //文件
        val inputStream = zipFile.getInputStream(this)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { inputStream.writeTo(it) }
        inputStream.close()
    }
}
