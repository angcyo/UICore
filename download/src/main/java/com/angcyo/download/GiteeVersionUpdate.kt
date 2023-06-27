package com.angcyo.download

import android.content.Context
import com.angcyo.download.version.VersionUpdateBean
import com.angcyo.download.version.versionUpdate
import com.angcyo.http.base.bean
import com.angcyo.http.base.listType
import com.angcyo.http.gitee.Gitee
import com.angcyo.http.toBean
import com.angcyo.library.annotation.CallComplianceAfter

/**
 * Gitee版本更新助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */

/**gitee版本更新检查
 * 在[com.angcyo.http.gitee.Gitee.BASE]目录下存放一个[versionJson]json文件即可获取更新信息
 * [version.json]
 * [debug] 是否强制显示版本更新界面
 * */
@CallComplianceAfter
fun Context.giteeVersionUpdate(versionJson: String = "version.json", debug: Boolean = false) {
    Gitee.get(versionJson) { data, error ->
        data?.toBean<VersionUpdateBean>(bean(VersionUpdateBean::class.java))?.let {
            versionUpdate(it, debug = debug)
        }
        error?.printStackTrace()
    }
}

/**版本更新记录*/
fun giteeVersionChange(
    changeJson: String = "change.json",
    action: (list: List<VersionUpdateBean>?, error: Throwable?) -> Unit
) {
    Gitee.get(changeJson) { data, error ->
        data?.toBean<List<VersionUpdateBean>>(listType(VersionUpdateBean::class))?.let {
            action(it, error)
        }
        error?.let {
            action(null, it)
        }
    }
}