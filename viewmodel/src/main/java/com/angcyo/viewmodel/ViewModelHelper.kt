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

}

/**共享的用户id*/
val _coreUserId: String?
    get() = ViewModelHelper.coreUserId.value