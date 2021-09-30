package com.angcyo.core

import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.angcyo.base.dslFHelper
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.fileSelector
import com.angcyo.library.L.DEFAULT_FILE_PRINT_PATH
import com.angcyo.library.app
import com.angcyo.library.ex.*
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.logFileName

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object Debug {

    /**开启调试模式*/
    var onChangedToDebug: MutableList<Action> = mutableListOf()

    /**调试:文本输入框文本改变时*/
    fun debugTextChanged(editText: EditText?) {
        val text = editText?.text
        when (text?.toString()?.lowercase()) {
            //开启调试模式
            "cmd:debug", "9.999999" -> {
                LibEx.isDebugTypeVal = true
                onChangedToDebug.forEach {
                    it()
                }
                app().vibrate()
            }
            //分享http日志文件
            "cmd:share:http", "9.777777" -> {
                val file = FileUtils.appRootExternalFolderFile(
                    DslFileHelper.appContext,
                    Constant.HTTP_FOLDER_NAME,
                    logFileName()
                )
                file?.shareFile()
            }
            "cmd:share:l", "9.111111" -> {
                val file = DEFAULT_FILE_PRINT_PATH?.file()
                file?.shareFile()
            }
            "cmd:open:file", "9.555555" -> {
                //打开文件预览对话框
                editText.context?.apply {
                    if (this is FragmentActivity) {
                        dslFHelper {
                            fileSelector({
                                showFileMd5 = true
                                showFileMenu = true
                                showHideFile = true
                                targetPath =
                                    FileUtils.appRootExternalFolder()?.absolutePath?.prePath()
                                        ?: targetPath
                            }) {
                                //no op
                            }
                        }
                    }
                }
            }
        }
    }
}