package com.angcyo.viewmodel

/**
 * 一些助手, 一些共享的数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
object ViewModelHelper {

    /**共享的用户id
     * [com.angcyo.core.component.manage.InnerFileManageModel.innerFilePath]*/
    var coreUserId = vmDataNull<String>(null)

    /**获取当前用户登录的ip, 是否是欧洲环境*/
    var getIsEuropeEnv: () -> Boolean = { false }

    /**获取当前用户登录的ip, 是否是安全合规环境
     * 决定保护罩预览的安全级别
     * */
    var getIsSafeEnv: () -> Boolean = { false }

    /**获取当前登录的用户是否要锁定激光亮度调节*/
    var getIsLaserBrightnessLock: () -> Boolean = { false }
}

/**共享的用户id*/
val _coreUserId: String?
    get() = ViewModelHelper.coreUserId.value

/**是否是欧洲环境*/
val _isEuropeEnv: Boolean
    get() = ViewModelHelper.getIsEuropeEnv()

/**是否是安全合规环境*/
val _isSafeEnv: Boolean
    get() = ViewModelHelper.getIsSafeEnv()

/**是否是欧洲ip且要求安全合规*/
val _isIpEuropeSafe get() = (_isEuropeEnv && _isSafeEnv) /*|| isDebugType()*/

/**是否要锁定激光亮度调节*/
val _isLaserBrightnessLock: Boolean
    get() = ViewModelHelper.getIsLaserBrightnessLock()