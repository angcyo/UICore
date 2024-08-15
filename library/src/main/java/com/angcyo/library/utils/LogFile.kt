package com.angcyo.library.utils

import java.io.File

/**
 * 日志文件管理
 *
 * /storage/emulated/0/Android/data/com.angcyo.uicore.demo/files/scheme/log/xxx.log
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/26
 */

object LogFile {

    /**控制台输出的日志*/
    const val l = "l.log"

    /**设备信息日志*/
    const val device = "device.log"

    /**webview的日志*/
    const val webview = "webview.log"

    /**雕刻日志*/
    const val engrave = "engrave.log"

    /**配置日志
     * 比如雕刻参数的配置*/
    const val config = "config.log"

    /**蓝牙日志*/
    const val ble = "ble.log"

    /**日志*/
    const val log = "log.log"

    /**错误日志*/
    const val error = "error.log"

    /**http日志*/
    const val http = "http.log"

    /**性能相关的日志*/
    const val perf = "perf.log"
}

/**log文件名转全路径*/
fun String.toLogFilePath(folder: String = Constant.LOG_FOLDER_NAME) =
    File(appFolderPath(folder), this).absolutePath

/**log文件所在的文件夹*/
fun logPath() = appFolderPath(Constant.LOG_FOLDER_NAME)