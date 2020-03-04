package com.angcyo.core.component.file

import androidx.collection.ArrayMap
import com.angcyo.coroutine.launchSafe
import com.angcyo.coroutine.onBack
import com.angcyo.coroutine.onMain
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
    var onFileMd5Result: (FileItem) -> Unit = {}

    var showHideFile: Boolean = false

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
                            val fileList: List<File> = if (showHideFile) {
                                this
                            } else {
                                this.filter {
                                    !it.isHidden
                                }
                            }
                            fileList.mapTo(resultList) {
                                FileItem(
                                    it,
                                    md5CacheMap[it.absolutePath],
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
                        if (it.file.isFile) {
                            it.file.md5()?.also { md5 ->
                                it.fileMd5 = md5
                                md5CacheMap[it.file.absolutePath] = md5

                                //通知文件md5获取结束
                                onMain {
                                    onFileMd5Result(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FileItem(
    val file: File,
    var fileMd5: String? = null,
    val mimeType: String? = null
)