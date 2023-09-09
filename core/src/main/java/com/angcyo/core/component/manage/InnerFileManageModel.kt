package com.angcyo.core.component.manage

import androidx.lifecycle.ViewModel
import com.angcyo.core.R
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.file
import com.angcyo.library.ex.size
import com.angcyo.library.model.Page
import com.angcyo.library.utils.folderPath
import com.angcyo.viewmodel._coreUserId
import com.angcyo.viewmodel.updateValue
import com.angcyo.viewmodel.vmDataOnce
import java.io.File

/**
 * 内部文件管理界面的数据模型
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
class InnerFileManageModel : ViewModel() {

    companion object {
        var DEFAULT_INNER_FILE_FOLDER_NAME = "innerFile"

        /**文件扩展名*/
        const val EXT_EXCEL = "xls"

        /**文件扩展名*/
        const val EXT_TXT = "txt"

        /**图标*/
        val innerFileIconMap = hashMapOf(
            EXT_EXCEL to R.drawable.core_file_excel_svg,
            EXT_TXT to R.drawable.core_file_txt_svg,
        )
    }

    /**支持的扩展名列表*/
    var supportExtList = listOf(EXT_EXCEL, EXT_TXT)

    /**国内文件导入app包名列表*/
    var zhImportFilePackageNameList =
        listOf("com.tencent.mm", "com.tencent.mobileqq", "com.tencent.tim")

    /**国外文件导入app包名列表*/
    var importFilePackageNameList =
        listOf("com.facebook.katana", "com.whatsapp", "com.twitter.android")

    /**文件所在的路径*/
    val innerFilePath: String
        get() {
            val userId = _coreUserId
            return if (userId.isNullOrEmpty()) {
                folderPath(DEFAULT_INNER_FILE_FOLDER_NAME)
            } else {
                folderPath("${DEFAULT_INNER_FILE_FOLDER_NAME}/$userId")
            }
        }

    /**导入状态通知*/
    val importStateOnceData = vmDataOnce<Boolean>()

    /**选择文件通知*/
    val innerSelectedFileOnceData = vmDataOnce<List<File>>()

    /**加载内部文件列表, 支持翻页
     * [filterExtList] 需要过滤的扩展名列表, 不指定则不过滤. 扩展名不包含.
     * */
    @CallPoint
    fun loadInnerFile(page: Page, filterExtList: List<String>? = null): List<File> {
        val result = mutableListOf<File>()
        innerFilePath.file().listFiles()
            ?.filter { filterExtList.isNullOrEmpty() || filterExtList.contains(it.extension.lowercase()) }
            ?.sortedByDescending { it.lastModified() }
            ?.apply {
                val startIndex = page.requestPageIndex * page.requestPageSize
                for ((index, file) in this.withIndex()) {
                    if (index >= startIndex) {
                        result.add(file)
                    }
                    if (result.size() >= page.requestPageSize) {
                        break
                    }
                }
            }
        return result
    }

    /**是否支持导入文件*/
    fun isSupportImportFile(file: File?): Boolean {
        return file != null && supportExtList.contains(file.extension)
    }

    /**导入文件*/
    fun importFile(file: File?): Boolean {
        val result = file?.copyTo(innerFilePath.file(file.name), true) != null
        if (result) {
            //通知刷新
            importStateOnceData.updateValue(true)
        }
        return result
    }
}