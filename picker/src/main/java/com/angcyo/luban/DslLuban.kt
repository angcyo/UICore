package com.angcyo.luban

import android.content.Context
import android.text.TextUtils
import com.angcyo.coroutine.launchGlobal
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.library.utils.folderPath
import com.angcyo.loader.LoaderMedia
import kotlinx.coroutines.async
import top.zibin.luban.Luban
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

    var targetMediaList = listOf<LoaderMedia>()

    /**忽略大小小于这个数值的文件[k]*/
    var leastCompressSize = 200

    /**是否要保留透明像素*/
    var enableAlpha = false

    /**回调*/
    var onCompressStart: () -> Unit = {}
    var onCompressEnd: () -> Unit = {}
    var onCompressProgress: (progress: Int) -> Unit = {}

    fun doIt(context: Context) {
        launchGlobal {
            onCompressStart()
            onCompressProgress(0)
            targetMediaList.forEachIndexed { index, loaderMedia ->
                async {
                    _doIt(context, loaderMedia)
                }.await()
                onCompressProgress((index * 1f / max(1, targetMediaList.size) * 100).toInt())
            }
            onCompressEnd()
        }
    }

    private fun _doIt(context: Context, media: LoaderMedia) {
        val path = media.run {
            cutPath?.run {
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
                Luban.with(context)
                    .setTargetDir(folderPath(targetFolderName))
                    .ignoreBy(leastCompressSize)
                    .setFocusAlpha(enableAlpha)
                    .filter {
                        //跳过gif文件压缩
                        !(TextUtils.isEmpty(it) ||
                                it.toLowerCase().endsWith(".gif"))
                    }
                    .setRenameListener {
                        fileNameUUID()
                    }
                    .load(listOf(path))
                    .get().apply {
                        firstOrNull()?.let {
                            media.compressPath = it.absolutePath
                        }
                    }
            } catch (e: Exception) {
                L.w(e)
            }
        }
    }
}

fun dslLuban(context: Context = app(), actions: DslLuban.() -> Unit) {
    DslLuban().apply {
        actions()
        doIt(context)
    }
}