package com.angcyo.dsladapter.filter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseFilterInterceptor : IFilterInterceptor {

    /**是否激活组件*/
    override var isEnable: Boolean = true
}