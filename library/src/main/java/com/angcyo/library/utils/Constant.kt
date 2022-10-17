package com.angcyo.library.utils

/**
 * 存储一些常量
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/20
 */

object Constant {

    const val UA =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36 chrome-extension"

    /**常用日志文件夹*/
    var LOG_FOLDER_NAME = "log"

    /**[com.angcyo.core.component.interceptor.LogFileInterceptor]*/
    var HTTP_FOLDER_NAME = "http"
    var UI_FOLDER_NAME = "ui"

    /**[com.angcyo.core.component.DslCrashHandler]*/
    var CRASH_FOLDER_NAME = "crash"
    var DOWN_FOLDER_NAME = "down"
    var OTHER_FOLDER_NAME = "other"
    var ERROR_FOLDER_NAME = "error"
    var CAMERA_FOLDER_NAME = "camera"

    /**[com.angcyo.ucrop.DslCrop]*/
    var CROP_FOLDER_NAME = "crop"

    /**[com.angcyo.component.luban.DslLuban]*/
    var LUBAN_FOLDER_NAME = "luban"
    var PICTURE_FOLDER_NAME = "picture"

    /**[com.angcyo.jpush.core.JPushReceiver]*/
    var PUSH_FOLDER_NAME = "push"
}