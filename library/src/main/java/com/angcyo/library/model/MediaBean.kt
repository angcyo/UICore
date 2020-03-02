package com.angcyo.library.model

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/02
 */
data class MediaBean(
    //数据库中的id
    var id: Long = -1,

    var localPath: String? = null,

    //angcyo
    var width: Int = -1,
    var height: Int = -1,

    /** 1558921509 秒 */
    var addTime: Long = -1,

    /** 文件大小, b->kb */
    var fileSize: Long = -1,

    var displayName: String? = null,

    var mimeType: String? = null
)