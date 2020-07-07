package com.angcyo.core.component.accessibility.parse

/**
 * 参数严格的约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ParamConstraint {

    /**约束的文本, 这个文本可以是对应的id, 或者node上的文本内容
     * 文本需要全部命中
     * */
    var text: List<String>? = null

    /**类名约束, 和[text]为一一对应的关系.为空, 表示不约束类名
     * 匹配规则时包含, 只要当前设置的cls包含视图中的cls就算命中
     * */
    var cls: List<String>? = null
}

