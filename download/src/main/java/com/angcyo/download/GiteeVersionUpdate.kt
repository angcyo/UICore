package com.angcyo.download

import android.content.Context
import com.angcyo.download.version.VersionUpdateBean
import com.angcyo.download.version.versionUpdate
import com.angcyo.http.base.bean
import com.angcyo.http.gitee.Gitee
import com.angcyo.http.toBean

/**
 * Gitee版本更新助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */

/**gitee版本更新检查
 * 在[com.angcyo.http.gitee.Gitee.getBASE]目录下存放一个[version.json]即可获取更新信息
 * */
fun Context.giteeVersionUpdate(versionJson: String = "version.json", debug: Boolean = false) {
    val json = versionJson
    Gitee.get(json) { data, error ->
        data?.toBean<VersionUpdateBean>(bean(VersionUpdateBean::class.java))?.let {
            versionUpdate(it, debug = debug)
        }
    }
}