package com.angcyo.http.form

/**
 * 附件上传管理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/21
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 * attachLocalPath 是否添加本地路径作为参数
 */

abstract class FormAttachManager {

    companion object {
        /**多附件,请使用此符号分割, 用来拼接项*/
        var ATTACH_SPLIT = "|"

        /**这种字符开始的key, 需要对路径进行解析*/
        var ATTACH_PATH_PARSE = "$"

        /**拼接在Url后面的id参数key值*/
        var KEY_FILE_ID = "fileId"

        var HTTP_PREFIX = "http"
    }
}