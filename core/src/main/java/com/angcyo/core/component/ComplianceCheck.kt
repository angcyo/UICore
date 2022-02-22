package com.angcyo.core.component

import android.app.Activity
import com.angcyo.core.vmApp
import com.angcyo.library.ex.Action
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.getAppVersionCode

/**
 * 合规检查
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/02/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object ComplianceCheck {

    /**合规状态通知的类型*/
    const val TYPE_COMPLIANCE_STATE = "TYPE_COMPLIANCE_STATE"

    /**合规初始化之后的通知类型*/
    const val TYPE_COMPLIANCE_INIT_AFTER = "TYPE_COMPLIANCE_INIT_AFTER"

    /**合规key, 用来保存当前版本的合规状态*/
    const val KEY_COMPLIANCE_STATE = "KEY_COMPLIANCE_STATE"

    /**是否合规*/
    fun isCompliance() = "${KEY_COMPLIANCE_STATE}_${getAppVersionCode()}".hawkGet() == "true"

    /**检查当前版本是否同意了合规请求*/
    fun check(complianceAction: Action): Boolean {
        return if (isCompliance()) {
            //已同意合规
            agree()
            true
        } else {
            //未合规
            complianceAction()
            false
        }
    }

    /**合规同意后调用此方法*/
    fun agree() {
        "${KEY_COMPLIANCE_STATE}_${getAppVersionCode()}".hawkPut("true")
        vmApp<StateModel>().updateState(TYPE_COMPLIANCE_STATE, true)
    }

    /**合规拒绝后调用此方法*/
    fun reject(activity: Activity) {
        "${KEY_COMPLIANCE_STATE}_${getAppVersionCode()}".hawkPut(null)
        activity.finish()
    }

}