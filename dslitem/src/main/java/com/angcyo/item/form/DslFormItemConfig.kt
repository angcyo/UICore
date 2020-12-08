package com.angcyo.item.form

/**
 * 表单[IFormItem]配置信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslFormItemConfig {
    /**表单是否必填. 为true, 将会在 label 前面绘制 红色`*` */
    var formRequired: Boolean = false

    /**获取form item对应的表单数据, 附件会自动解析*/
    var formObtain: (params: DslFormParams) -> Unit = {}

    /**获取form item数据之前, 进行的检查. 返回false, 会终止数据获取, 并且提示错误*/
    var formCheck: (params: DslFormParams) -> Boolean = { true }
}