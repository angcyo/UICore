package com.angcyo.library.component

import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.angcyo.library.L
import com.angcyo.library.ex.*
import com.angcyo.library.model.TypefaceInfo
import com.angcyo.library.utils.filePath
import com.angcyo.library.utils.folderPath
import java.io.File

/**
 * 字体管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/17
 */
object FontManager {

    /**默认的字体文件夹名称*/
    const val DEFAULT_FONT_FOLDER_NAME = "fonts"

    /**自定义字体的其他文件夹路径*/
    val customFontFolderList = mutableListOf<String>()

    //

    /**文件路径转字体对象*/
    fun String.toTypeface() = Typeface.createFromFile(file())

    /**默认的字体列表*/
    val DEFAULT_TYPEFACE_LIST = mutableListOf<TypefaceInfo>().apply {
        //系统默认字体
        //typefaceItem("normal", Typeface.DEFAULT)
        //typefaceItem("sans", Typeface.SANS_SERIF)
        //add(TypefaceInfo("serif", Typeface.SERIF))
        add(TypefaceInfo("Default-Normal", Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)))
        add(TypefaceInfo("Default-Bold", Typeface.create(Typeface.DEFAULT, Typeface.BOLD)))
        add(TypefaceInfo("Default-Italic", Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)))
        add(
            TypefaceInfo(
                "Default-Bold-Italic",
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            )
        )
    }

    fun _addFont(list: MutableList<TypefaceInfo>, file: File): TypefaceInfo? {
        return try {
            if (file.name.isFontType()) {
                val typeface = Typeface.createFromFile(file)
                val typefaceInfo = TypefaceInfo(file.name.noExtName(), typeface, file.absolutePath)
                if (!list.contains(typefaceInfo)) {
                    list.add(typefaceInfo)
                    typefaceInfo
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**通过字体名称, 获取一个字体信息, 如果不存在则使用默认字体, 但是不应该修改需要存储的数据
     * [FontManager.getSystemFontList().firstOrNull()?.typeface]
     * */
    fun loadTypefaceInfo(name: String?): TypefaceInfo? {
        var result = getCustomFontList().find { it.name == name }
        if (result != null) {
            return result
        }
        result = getSystemFontList().find { it.name == name }
        if (result != null) {
            return result
        }
        result = getPrimaryFontList().find { it.name == name }
        if (result != null) {
            return result
        }
        return null
    }

    /**通过内存字体对象, 获取字体描述信息*/
    fun loadTypefaceInfo(typeface: Typeface): TypefaceInfo? {
        var result = getCustomFontList().find { it.typeface == typeface }
        if (result != null) {
            return result
        }
        result = getSystemFontList().find { it.typeface == typeface }
        if (result != null) {
            return result
        }
        result = getPrimaryFontList().find { it.typeface == typeface }
        if (result != null) {
            return result
        }
        return null
    }

    //region ---导入自定义的字体---

    /**导入字体*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun importCustomFont(uri: Uri?): TypefaceInfo? {
        if (uri == null) {
            return null
        }
        return importCustomFont(uri.saveToFolder())
    }

    /**导入字体*/
    fun importCustomFont(path: String?): TypefaceInfo? {
        try {
            val fontList = getCustomFontList()
            if (path.isFontType()) {
                val file = File(path!!)

                val typeface = Typeface.createFromFile(file)
                file.copyTo(filePath(DEFAULT_FONT_FOLDER_NAME, file.name))

                val typefaceInfo =
                    TypefaceInfo(file.name.noExtName(), typeface, file.absolutePath)
                typefaceInfo.isCustom = true
                val find = fontList.find { it.name == typefaceInfo.name }
                if (find == null) {
                    _customFontList.add(0, typefaceInfo)
                } else {
                    //字体已存在
                    typefaceInfo.isRepeat = true
                }

                return typefaceInfo
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    //endregion ---导入自定义的字体---

    //region ---系统字体---

    /**系统字体加载列表*/
    val systemFontPath = listOf("/system/fonts", "/system/font", "/data/fonts")

    val _systemFontList = mutableListOf<TypefaceInfo>()

    /**获取系统字体列表*/
    fun getSystemFontList(): List<TypefaceInfo> {
        if (_systemFontList.isEmpty()) {
            //
            _systemFontList.addAll(DEFAULT_TYPEFACE_LIST)
            //
            systemFontPath.forEach { path ->
                path.file().eachFile { file ->
                    _addFont(_systemFontList, file)
                }
            }
        }
        return _systemFontList
    }

    //endregion ---系统字体---

    //region ---在线/推荐字体---

    val _primaryFontList = mutableListOf<TypefaceInfo>()

    /**获取主要的字体*/
    fun getPrimaryFontList(): List<TypefaceInfo> {
        if (_primaryFontList.isEmpty()) {

        }
        return _primaryFontList
    }

    //endregion ---在线/推荐字体---

    //region ---自定义的字体---

    val _customFontList = mutableListOf<TypefaceInfo>()

    /**获取自定义的字体列表*/
    fun getCustomFontList(): List<TypefaceInfo> {
        if (_customFontList.isEmpty()) {
            //字体文件夹
            val fontFolder = folderPath(DEFAULT_FONT_FOLDER_NAME)

            //自定义的字体
            loadCustomFont(fontFolder)
            customFontFolderList.forEach {
                loadCustomFont(it)
            }
        }
        return _customFontList
    }

    /**备份内部字体到指定目录
     * [targetFolder] 目标文件夹*/
    fun backupFontTo(targetFolder: String) {
        for (typeInfo in _customFontList) {
            typeInfo.filePath?.let { path ->
                if (path.startsWith(targetFolder)) {
                    L.w("忽略备份文件:${path}")
                } else {
                    val targetFile = File(targetFolder, path.fileName()!!)
                    //val mk = targetFile.parentFile?.mkdirs()
                    path.copyFileTo(targetFile, true)
                }
            }
        }
    }

    fun loadCustomFont(folder: String) {
        folder.file().eachFile { file ->
            _addFont(_customFontList, file)?.isCustom = true
        }
    }

    /**删除自定义的字体, 从硬盘上删除*/
    fun deleteCustomFont(info: TypefaceInfo): Boolean {
        val bool = info.filePath?.file()?.delete() == true
        if (bool) {
            _customFontList.remove(info)
        }
        return bool
    }

    //endregion ---自定义的字体---

}