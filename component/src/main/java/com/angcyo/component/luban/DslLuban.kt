package com.angcyo.component.luban

import android.content.Context
import android.text.TextUtils
import com.angcyo.coroutine.launchGlobal
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.app
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.bitmapSize
import com.angcyo.library.ex.fileSize
import com.angcyo.library.ex.save
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.libCacheFile
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.isImage
import com.angcyo.library.model.toLoaderMedia
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.Media
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.library.utils.folderPath
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import top.zibin.luban.Luban
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/20
 */
class DslLuban {

    /**压缩存在文件的文件夹名称*/
    var targetFolderName = "luban"

    /**需要压缩的媒体*/
    var targetMediaList = mutableListOf<LoaderMedia>()

    /**忽略大小小于这个数值的文件[k]*/
    var leastCompressSize = LibHawkKeys.minKeepSize

    /**是否要保留透明像素, 否则透明像素会变成黑色*/
    var enableAlpha = true

    /**是否异步调用执行*/
    var async: Boolean = true

    /**自动旋转图片*/
    var isAutoRotating: Boolean = true

    /**回调*/
    //@WorkerThread
    var onCompressStart: () -> Unit = {}

    //@WorkerThread
    var onCompressEnd: () -> Unit = {}

    //@WorkerThread
    var onCompressProgress: (progress: Int) -> Unit = {}

    var _job: Job? = null
    fun doIt(context: Context): Job? {
        cancel()
        if (async) {
            //异步
            _job = launchGlobal {
                async {
                    _compress(context)
                }.await()
            }
        } else {
            //同步
            _compress(context)
        }
        return _job
    }

    /**添加一个需要压缩的图片路径*/
    fun addPath(path: String) {
        targetMediaList.add(path.toLoaderMedia())
    }

    /**取消*/
    fun cancel() {
        _job?.cancel(CancellationException("用户取消!"))
        _job = null
    }

    private fun _compress(context: Context) {
        LTime.tick()
        onCompressStart()
        onCompressProgress(0)
        targetMediaList.forEachIndexed { index, loaderMedia ->
            if (loaderMedia.isImage()) {
                _doIt(context, loaderMedia)
            }
            onCompressProgress((index * 1f / max(1, targetMediaList.size) * 100).toInt())
        }
        onCompressEnd()
        L.i("压缩耗时:${LTime.time()}")
    }

    private fun _doIt(context: Context, media: LoaderMedia) {
        var path = media.run {
            cropPath?.run {
                //优先使用剪切后的文件路径
                this
            } ?: localUri?.run {
                //其次使用本地文件的uri
                this
            } ?: localPath?.run {
                //再次使用本地文件的path
                this
            }
        }

        if (path != null) {
            try {
                if (isAutoRotating) {
                    if (path is String) {
                        path.toBitmap()?.let {
                            val cacheFilePath = libCacheFile().absolutePath
                            it.save(cacheFilePath)
                            path = cacheFilePath
                        }
                    }
                }

                Luban.with(context)
                    .setTargetDir(folderPath(targetFolderName))
                    .ignoreBy(leastCompressSize)
                    .setFocusAlpha(enableAlpha)
                    .filter {
                        //跳过gif文件压缩
                        !(TextUtils.isEmpty(it) || it.lowercase(Locale.getDefault())
                            .endsWith(".gif"))
                    }
                    .setRenameListener {
                        fileNameUUID()
                    }
                    .load(listOf(path))
                    .get().apply {
                        firstOrNull()?.let {
                            val size = it.bitmapSize()
                            val w = size[0]
                            val h = size[1]
                            val targetFile = Media.copyFrom(
                                it,
                                folderPath(Constant.LUBAN_FOLDER_NAME), w, h
                            )
                            media.compressPath = targetFile.absolutePath
                            media.fileSize = media.compressPath.fileSize()
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**直接压缩图片
 * [keepMinSize] 图片已经小于这个大小时, 不压缩
 *
 * @return 同步返回压缩后的图片路径
 * */
fun String.luban(
    keepMinSize: Int = LibHawkKeys.minKeepSize,
    actions: DslLuban.() -> Unit = {}
): String {
    val path = this
    val result = dslLuban {
        async = false //是否异步
        leastCompressSize = keepMinSize
        addPath(path)
        actions()
    }.targetMediaList.firstOrNull()?.compressPath ?: path
    return result
}

/**压缩图片, 会改变图片的尺寸*/
fun dslLuban(context: Context = app(), actions: DslLuban.() -> Unit): DslLuban {
    return DslLuban().apply {
        //addPath()
        actions()
        doIt(context)
    }
}