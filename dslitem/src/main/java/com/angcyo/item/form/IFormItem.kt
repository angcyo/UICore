package com.angcyo.item.form

import com.angcyo.dsladapter.item.IDslItem

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IFormItem : IDslItem {

    /**表单相关信息, 在此对象中配置*/
    var itemFormConfig: DslFormItemConfig
}