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
}

/**共享的用户id*/
val _coreUserId: String?
    get() = ViewModelHelper.coreUserId.value

/**是否是欧洲环境*/
val _IsEuropeEnv: Boolean
    get() = ViewModelHelper.getIsEuropeEnv()