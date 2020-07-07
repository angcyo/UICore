package com.angcyo.core.component.accessibility.parse

import com.angcyo.library.ex.isListEmpty

/**
 * 包含界面解析需要的参数信息, 和执行的操作信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ParseBean(

    /**可以是 完整的id, 也可以是 gj4.
     *
     * ids 列表中, 只要满足任意一个约束条件, 即视为发现目标
     * */
    var ids: List<ConstraintBean>? = null, //完整的id应该是: com.ss.android.ugc.aweme:id/gj4

    /**node中需要包含的文本
     * 支持的格式同ids
     * */
    var texts: List<ConstraintBean>? = null //包含的文本
)

/**是否是空数据*/
fun ParseBean?.isEmpty() = this != null && ids.isListEmpty() && texts.isListEmpty()

fun dslParseParams(action: ParseBean.() -> Unit): ParseBean {
    return ParseBean().apply(action)
}

/**添加id过滤约束, 多个id使用[or]的关系*/
fun ParseBean.idOr(vararg id: String) {
    if (ids == null) {
        ids = mutableListOf()
    }
    if (ids is MutableList<ConstraintBean>) {
        (ids as MutableList<ConstraintBean>).apply {
            id.forEach { subId ->
                add(ConstraintBean().apply {
                    text = mutableListOf(subId)
                })
            }
        }
    }
}

fun ParseBean.textOr(vararg text: String) {
    if (texts == null) {
        texts = mutableListOf()
    }
    if (texts is MutableList<ConstraintBean>) {
        (texts as MutableList<ConstraintBean>).apply {
            text.forEach { subId ->
                add(ConstraintBean().apply {
                    this.text = mutableListOf(subId)
                })
            }
        }
    }
}

/**添加id过滤约束, 同时满足多个id , id使用[and]的关系*/
fun ParseBean.idAnd(vararg id: String) {
    if (ids == null) {
        ids = mutableListOf()
    }
    if (ids is MutableList<ConstraintBean>) {
        (ids as MutableList<ConstraintBean>).apply {
            add(ConstraintBean().apply {
                text = id.toList()
            })
        }
    }
}

fun ParseBean.textAnd(vararg text: String) {
    if (texts == null) {
        texts = mutableListOf()
    }
    if (texts is MutableList<ConstraintBean>) {
        (texts as MutableList<ConstraintBean>).apply {
            add(ConstraintBean().apply {
                this.text = text.toList()
            })
        }
    }
}
