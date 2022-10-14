package com.angcyo.library.component

import android.content.Intent
import com.angcyo.library.L
import com.angcyo.library.ex.extName
import com.angcyo.library.ex.lastName
import com.angcyo.library.ex.saveTo
import com.angcyo.library.libCacheFile

/**
 * 从第三方应用 打开文件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/12
 */
object ROpenFileHelper {

    /**
     * 从[intent]中, 解析出需要打开的文件路径
     * 会通过Uri, 进行转存文件, 保存到cache目录
     * [Intent.ACTION_VIEW]
     * [Intent.ACTION_SEND]
     *
     * [ext] 如果[intent]中未包含扩展名, 则需要补充的扩展名,智能识别.号
     * */
    fun parseIntent(intent: Intent?, ext: String? = null): String? {
        intent ?: return null
        val action = intent.action

        val data = when (action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.extras?.getParcelable(Intent.EXTRA_STREAM)
            else -> null
        }
        //android.intent.action.VIEW
        //content://com.estrongs.files/storage/emulated/0/tencent/QQ_Images/ffea464c0cb6e12.jpg
        L.i("解析:$action $data")

        if (data != null) {
            val path = "$data"
            val name = path.lastName()
            var extName = name.extName()

            val newPath = if (extName.isEmpty()) {
                //无扩展名
                extName = ext ?: intent.type?.lastName() ?: ""
                if (extName.startsWith(".")) {
                    libCacheFile("${name}${extName}").absolutePath
                } else {
                    libCacheFile("${name}.${extName}").absolutePath
                }
            } else {
                libCacheFile(name).absolutePath
            }
            data.saveTo(newPath)//转存文件

            ///data/user/0/com.angcyo.uicore.demo/cache/documents/ffea464c0cb6e12(2).jpg
            //->/storage/emulated/0/Android/data/com.angcyo.uicore.demo/cache/ffea464c0cb6e12(2).jpg
            L.i("转存文件:$path ->$newPath")
            return newPath
        }
        return null
    }

}