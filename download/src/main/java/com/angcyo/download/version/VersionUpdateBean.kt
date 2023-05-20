package com.angcyo.download.version

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class VersionUpdateBean(
    /**版本时间*/
    var versionDate: String? = null,
    /**需要更新的版本名称*/
    var versionName: String? = null,
    /**需要更新的版本号*/
    var versionCode: Long = 0,
    /**更新提示:,比如 更新内容
     * [versionDes]*/
    var versionDesTip: String? = null,
    /**更新描述*/
    var versionDes: String? = null,
    /**强制更新*/
    var versionForce: Boolean = false,
    /**下载地址*/
    var versionUrl: String? = null,
    /**[versionUrl]是外链, 而非下载地址*/
    var link: Boolean = false,
    /**是否要跳转到应用市场, 优先级最高*/
    var toMarketDetails: Boolean = false,
    /**版本更新类型
     * >=0 普通用户版本更新
     * <0 Debug用户版本更新
     * */
    var versionType: Int = 1,

    /**需要更新的包名, 包名不匹配则不需要更新*/
    var packageList: List<String>? = null,

    /**指定设备才能收到更新[androidId]*/
    var deviceList: List<String>? = null,
    /**当前版本信息, 是否只在debug模式下生效*/
    var debug: Boolean = false,

    //---

    /**禁用的版本范围
     * [com.angcyo.library.component.VersionMatcher.parseRange]*/
    var forbiddenVersionRange: String? = null,

    /**需要强制禁止使用的版本[versionCode]*/
    var forbiddenVersionList: List<Long>? = null,

    /**禁止使用的原因*/
    var forbiddenReason: String? = null
)