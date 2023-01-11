package com.angcyo.core

import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.angcyo.base.dslFHelper
import com.angcyo.core.CoreApplication.Companion.DEFAULT_FILE_PRINT_PATH
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.fileSelector
import com.angcyo.library.Library
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.appFolderPath
import com.angcyo.library.utils.logFileName

/**
 * 输入框调试模式指令
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object Debug {

    /**开启调试模式*/
    var onChangedToDebug: MutableList<Action> = mutableListOf()

    /**显示界面回调*/
    var onShowFragmentAction: ((FragmentActivity, show: String) -> Unit)? = null

    /**调试:文本输入框文本改变时
     * [com.angcyo.widget.edit.BaseEditDelegate.Companion.textChangedActionList]*/
    fun onDebugTextChanged(
        editText: EditText?,
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        val inputText = text?.toString()
        if (inputText.isNullOrBlank()) {
            return
        }
        when (inputText.lowercase()) {
            //开启调试模式
            "@cmd#debug", "@9.999999" -> {
                Library.isDebugTypeVal = true
                onChangedToDebug.forEach {
                    it()
                }
                editText?._feedback()
            }
            //分享http日志文件
            "@cmd#share=http", "@9.777777" -> {
                val file = FileUtils.appRootExternalFolderFile(
                    Constant.HTTP_FOLDER_NAME,
                    logFileName()
                )
                file.shareFile()
                editText?._feedback()
            }
            //分享L.log
            "@cmd#share=l", "@9.111111" -> {
                val file = DEFAULT_FILE_PRINT_PATH?.file()
                file?.shareFile()
                editText?._feedback()
            }
            //分享crash.log
            "@cmd#share=crash", "@9.333333" -> {
                val file = DslCrashHandler.KEY_CRASH_FILE.hawkGet()?.file()
                file?.shareFile()
                editText?._feedback()
            }
            "@cmd#open=file", "@9.555555" -> {
                //打开文件预览对话框
                editText?.context?.apply {
                    if (this is FragmentActivity) {
                        dslFHelper {
                            fileSelector({
                                showFileMd5 = true
                                showFileMenu = true
                                showHideFile = true
                                targetPath = appFolderPath()
                            }) {
                                //no op
                            }
                        }
                        editText._feedback()
                    }
                }
            }
            else -> {
                //@key#int=value 此指令用来设置hawk key value
                if (inputText.contains("@") &&
                    inputText.contains("#") &&
                    inputText.contains("=")
                ) {
                    val keyBuilder = StringBuilder()
                    val typeBuilder = StringBuilder()
                    val valueBuilder = StringBuilder()

                    var operate: StringBuilder? = null
                    inputText.forEach {
                        when (it) {
                            '@' -> operate = keyBuilder
                            '#' -> operate = typeBuilder
                            '=' -> operate = valueBuilder
                            else -> operate?.append(it)
                        }
                    }

                    //@key#int=value
                    val key = keyBuilder.toString()
                    val type = typeBuilder.toString().lowercase()
                    val valueString = valueBuilder.toString()
                    if (key.isNotBlank()) {
                        when (key.lowercase()) {
                            "cmd" -> {
                                when (type) {
                                    "open" -> {
                                        //打开Fragment界面
                                        //@cmd#open=value
                                        lastContext.apply {
                                            if (this is FragmentActivity) {
                                                onShowFragmentAction?.invoke(this, valueString)
                                                editText?._feedback()
                                            }
                                        }
                                    }
                                    "hawk" -> {
                                        //显示hawk的值
                                        //@cmd#hawk=key
                                        val hawkKey = valueString.lowercase()
                                        toastQQ("${hawkKey.hawkGet<Any>()}")
                                        editText?._feedback()
                                    }
                                }
                            }
                            else -> {
                                //@key#int=value
                                when (type) {
                                    "bool", "boolean" -> {
                                        val value = valueString.toBoolean()
                                        key.hawkPut(value)
                                        editText?._feedback()
                                    }
                                    "int", "i" -> {
                                        val value = valueString.toIntOrNull()
                                        if (value == null) {
                                            key.hawkDelete()
                                        } else {
                                            key.hawkPut(value)
                                        }
                                        editText?._feedback()
                                    }
                                    "long", "l" -> {
                                        val value = valueString.toLongOrNull()
                                        if (value == null) {
                                            key.hawkDelete()
                                        } else {
                                            key.hawkPut(value)
                                        }
                                        editText?._feedback()
                                    }
                                    "float", "f" -> {
                                        val value = valueString.toFloatOrNull()
                                        if (value == null) {
                                            key.hawkDelete()
                                        } else {
                                            key.hawkPut(value)
                                        }
                                        editText?._feedback()
                                    }
                                    "double", "d" -> {
                                        val value = valueString.toDoubleOrNull()
                                        if (value == null) {
                                            key.hawkDelete()
                                        } else {
                                            key.hawkPut(value)
                                        }
                                        editText?._feedback()
                                    }
                                    "string", "s" -> {
                                        key.hawkPut(valueString)
                                        editText?._feedback()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**反馈*/
    fun EditText._feedback() {
        selectAll()
        longFeedback()
    }
}