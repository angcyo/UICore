package com.angcyo.library.ex

import com.angcyo.library.libCacheFile
import com.angcyo.library.libCacheFolderPath
import com.angcyo.library.utils.fileNameUUID
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 压缩/解压文件工具
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/06
 */

/**快速创作一个zip文件, 并且写入数据
 * [zipFilePath] zip文件输出全路径
 * @return 返回成功与失败*/
fun zipFileWrite(zipFilePath: String, writeAction: ZipOutputStream.() -> Unit): String? {
    return try {
        val file = File(zipFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        val zip = ZipOutputStream(FileOutputStream(file, false))
        //zip.writeEntry() //使用这个方法, 写入数据到zip文件
        zip.writeAction()
        zip.finish()
        zip.close()
        zipFilePath //成功返回路径, 失败返回null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**压缩包文件的读取, 不解压直接读流
 * [zipFilePath] zip文件的路径
 * */
fun zipFileRead(zipFilePath: String, readAction: ZipFile.() -> Unit) {
    val file = File(zipFilePath)
    if (!file.exists()) {
        return
    }
    val zipFile = ZipFile(file)
    //zipFile.readEntry() //使用这个方法, 读取zip文件中的数据
    zipFile.readAction()
    zipFile.close()
}

//---

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

/**写入一个实体
 * [entryName] 实体的名称, 可以是 xxx 也可以是 xxx/xxx.xxx
 * [inputStream] 输入流, 数据的来源
 * */
fun ZipOutputStream.writeEntry(entryName: String, inputStream: InputStream) {
    val entry = ZipEntry(entryName)
    putNextEntry(entry)
    inputStream.writeTo(this)
    inputStream.close()
    closeEntry()
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
        writeEntry(entryName, FileInputStream(file))
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

        zipFile.close()
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

fun ZipFile.readEntry(entryName: String): ByteArray? {
    return try {
        val bytes = getEntry(entryName)?.run {
            getInputStream(this).use {
                it.readBytes()
            }
        }
        bytes
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun ZipFile.readEntryStream(entryName: String): InputStream? {
    return try {
        getEntry(entryName)?.run {
            getInputStream(this)
            //请主动调用 inputStream.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**直接从zip文件中, 读取指定[ZipEntry]的流
 *
 * [entryName] [ZipEntry]实体的名称, 需要带上路径 [name] [folder/folder/name]*/
fun File.readEntryStream(entryName: String): InputStream? {
    return try {
        val zipFile = ZipFile(this)
        //zipFile.close()
        zipFile.getEntry(entryName)?.let {
            return zipFile.getInputStream(it)
            //请主动调用 inputStream.close()
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}