package com.angcyo.core.component.file

import android.net.Uri
import androidx.collection.ArrayMap
import com.angcyo.coroutine.launchSafe
import com.angcyo.coroutine.onBack
import com.angcyo.coroutine.onMain
import com.angcyo.library.ex.isFileScheme
import com.angcyo.library.ex.md5
import com.angcyo.library.ex.mimeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.util.*

/**
 * 文件loader
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/04
 */
class DslFileLoader {

    companion object {
        private val md5CacheMap = ArrayMap<String, String>()
    }

    /**加载返回*/
    var onLoaderResult: (List<FileItem>) -> Unit = {}
    /**异步获取文件md5返回*/
    var onLoaderDelayResult: (FileItem) -> Unit = {}

    var loadHideFile: Boolean = false

    var _loadPath: String? = null

    /**开始加载*/
    fun load(path: String, scope: CoroutineScope = GlobalScope) {
        _loadPath = path
        scope.launchSafe {
            onBack {
                val file = File(path)
                val result: List<FileItem> =
                    if (file.exists() && file.isDirectory && file.canRead()) {
                        val list = file.listFiles()?.asList() ?: emptyList()
                        val resultList = mutableListOf<FileItem>()

                        list.sortedWith(Comparator<File> { file1, file2 ->
                            when {
                                (file1.isDirectory && file2.isDirectory) || (file1.isFile && file2.isFile) -> file1.name.toLowerCase().compareTo(
                                    file2.name.toLowerCase()
                                )
                                file2.isDirectory -> 1
                                file1.isDirectory -> -1
                                else -> file1.name.toLowerCase().compareTo(file2.name.toLowerCase())
                            }
                        }).apply {
                            val fileList: List<File> = if (loadHideFile) {
                                this
                            } else {
                                this.filter {
                                    !it.isHidden
                                }
                            }
                            fileList.mapTo(resultList) {
                                FileItem(
                                    Uri.fromFile(it),
                                    md5CacheMap[it.absolutePath],
                                    0L,
                                    if (it.isFile) it.length() else 0L,
                                    it.absolutePath.mimeType()
                                )
                            }
                        }
                        resultList
                    } else {
                        emptyList()
                    }
                result
            }.await().apply {
                //通知文件load结束
                onLoaderResult(this)

                onBack {
                    this@apply.forEach {
                        val file: File? = it.file()

                        if (file?.isDirectory == true) {
                            //如果文件夹文件过多, 这个list也是很耗时的.
                            it.fileCount = file.list()?.size?.toLong() ?: 0L
                        } else if (file?.isFile == true) {
                            file.md5()?.also { md5 ->
                                it.fileMd5 = md5
                                md5CacheMap[file.absolutePath] = md5
                            }
                        }

                        //通知文件md5获取结束
                        onMain {
                            onLoaderDelayResult(it)
                        }
                    }
                }
            }
        }
    }
}

data class FileItem(
    //文件对象
    val fileUri: Uri,
    //文件 md5值, 文件夹除外.
    var fileMd5: String? = null,
    //文件夹内的子文件数量, 文件除外.
    var fileCount: Long = 0,
    //文件大小, 文件夹除外.
    var fileLength: Long = 0,
    //文件 mimeType, 文件夹除外
    val mimeType: String? = null
) {
    override fun hashCode(): Int {
        return fileUri.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is FileItem) {
            return fileUri == other.fileUri
        }
        return super.equals(other)
    }
}

fun FileItem?.file(): File? {
    return if (this?.fileUri.isFileScheme()) {
        File(this!!.fileUri.path!!)
    } else {
        null
    }
}