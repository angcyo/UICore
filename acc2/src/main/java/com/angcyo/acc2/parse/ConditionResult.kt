package com.angcyo.acc2.parse

import com.angcyo.acc2.bean.ConditionBean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ConditionResult : BaseResult() {

    /**返回成功时, 满足的条件数据结构
     * 也有可能, 处理成功. 但是无数据结构
     * */
    var conditionBean: ConditionBean? = null
}