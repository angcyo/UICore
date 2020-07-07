package com.angcyo.core.component.accessibility.parse

import com.angcyo.library.ex.isListEmpty

/**
 * 包含界面解析需要的参数信息, 和执行的操作信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ParseParams {

    /**可以是 完整的id, 也可以是 gj4.
     *
     * ids 列表中, 只要满足任意一个约束条件, 即视为发现目标
     * */
    var ids: List<ParamConstraint>? = null //完整的id应该是: com.ss.android.ugc.aweme:id/gj4

    /**node中需要包含的文本
     * 支持的格式同ids
     * */
    var texts: List<ParamConstraint>? = null //包含的文本
}

/**是否是空数据*/
fun ParseParams?.isEmpty() = this != null && ids.isListEmpty() && texts.isListEmpty()