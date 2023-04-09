package com.angcyo.library.component

import android.content.Intent
import android.net.Uri
import com.angcyo.library.L
import com.angcyo.library.ex.*
import com.angcyo.library.libCacheFile
import java.io.File

/**
 * 从第三方应用 打开文件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/12
 */
object ROpenFileHelper {

    /**从[Intent]中获取对应的资源*/
    fun parseIntentUri(intent: Intent?): ArrayList<Uri>? {
        intent ?: return null
        val action = intent.action
        val dataList: ArrayList<Uri>? = when (action) {
            //使用app打开
            //android.intent.action.VIEW
            //content://com.estrongs.files/storage/emulated/0/tencent/QQ_Images/ffea464c0cb6e12.jpg
            //Intent.ACTION_VIEW -> intent.data

            //发送至app
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                ?.toArrayListOf()
            //android.intent.action.SEND_MULTIPLE
            //content://media/external/images/media/6238

            //多选发送/分享至app
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            else -> intent.data?.toArrayListOf()
        }
        L.i("解析:$action $dataList")
        return dataList
    }

    /**
     * 从[intent]中, 解析出需要打开的文件路径
     * 会通过Uri, 进行转存文件, 保存到cache目录
     * [Intent.ACTION_VIEW]
     * [Intent.ACTION_SEND]
     *
     * [ext] 如果[intent]中未包含扩展名, 则需要补充的扩展名,智能识别.号
     *
     * [savePath] 强制指定文件的全路径, 此时[ext]参数无效
     * [folderPath] 单独指定转存的文件目录, 自动补齐文件名
     * @return 返回转存后的文件路径
     * */
    fun parseIntent(
        intent: Intent?,
        ext: String? = null,
        savePath: String? = null,
        folderPath: String? = null
    ): String? {
        intent ?: return null
        val dataList = parseIntentUri(intent)
        val data = dataList?.firstOrNull() ?: return null
        return parseData(
            data,
            ext ?: intent.type?.mimeTypeToExtName() ?: intent.type?.lastName(),
            savePath,
            folderPath
        )
    }

    /**
     * 转存[data]数据
     * [parseIntent]
     * [savePath] 指定转存的文件全路径,不指定时自动获取
     * [folderPath] 单独指定路径, 并且自动获取文件名和扩展
     * */
    fun parseData(
        data: Uri?,
        ext: String? = null, //单独指定文件的扩展名
        savePath: String? = null, //强制指定文件的全路径, 此时[ext]参数无效
        folderPath: String? = null //单独指定文件的存储目录
    ): String? {
        data ?: return null
        val filePath: String
        val path = "$data"
        if (savePath == null) {
            //%E9%87%91%E9%97%A8%E5%A4%A7%E6%A1%A5-GCODE.dxf
            //decode->金门大桥-GCODE.dxf

            val name = data.getShowName()
            var extName = name.extName()

            val fileName = if (extName.isEmpty() && !ext.isNullOrEmpty()) {
                //无扩展名
                extName = ext
                if (extName.startsWith(".")) {
                    "${name}${extName}"
                } else {
                    "${name}.${extName}"
                }
            } else {
                name
            }

            val newPath = if (folderPath == null) {
                libCacheFile(fileName)
            } else {
                File(folderPath, fileName)
            }.absolutePath

            filePath = newPath
        } else {
            filePath = savePath
        }
        data.saveTo(filePath)//转存文件
        ///data/user/0/com.angcyo.uicore.demo/cache/documents/ffea464c0cb6e12(2).jpg
        //->/storage/emulated/0/Android/data/com.angcyo.uicore.demo/cache/ffea464c0cb6e12(2).jpg
        L.i("转存文件:$path ->$filePath")
        return filePath
    }

}