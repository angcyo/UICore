package com.angcyo.dsladapter

import com.angcyo.widget.DslViewHolder

/**
 * Adapter情感图扩展
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/15
 */

/**自定义渲染任意情感图状态
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_EMPTY]
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_LOADING]
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_ERROR]
 * */
fun DslAdapter.renderAdapterStatus(
    adapterState: Int,
    layoutId: Int,
    bindAction: (itemHolder: DslViewHolder, state: Int) -> Unit = { _, _ -> }
) {
    dslAdapterStatusItem.itemStateLayoutMap[adapterState] = layoutId
    val oldBindAction = dslAdapterStatusItem.onBindStateLayout
    dslAdapterStatusItem.onBindStateLayout = { itemHolder, state ->
        oldBindAction(itemHolder, state)
        if (adapterState == state) {
            bindAction(itemHolder, state)
        }
    }
}

/**渲染情感图空状态时的布局
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_EMPTY]*/
fun DslAdapter.renderAdapterEmptyStatus(
    layoutId: Int = dslAdapterStatusItem.itemStateLayoutMap[DslAdapterStatusItem.ADAPTER_STATUS_EMPTY]!!,
    bindAction: (itemHolder: DslViewHolder, state: Int) -> Unit = { _, _ -> }
) {
    renderAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY, layoutId, bindAction)
}

fun DslAdapter.renderAdapterErrorStatus(
    layoutId: Int = dslAdapterStatusItem.itemStateLayoutMap[DslAdapterStatusItem.ADAPTER_STATUS_ERROR]!!,
    bindAction: (itemHolder: DslViewHolder, state: Int) -> Unit = { _, _ -> }
) {
    renderAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_ERROR, layoutId, bindAction)
}

fun DslAdapter.renderAdapterLoadingStatus(
    layoutId: Int = dslAdapterStatusItem.itemStateLayoutMap[DslAdapterStatusItem.ADAPTER_STATUS_LOADING]!!,
    bindAction: (itemHolder: DslViewHolder, state: Int) -> Unit = { _, _ -> }
) {
    renderAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING, layoutId, bindAction)
}

/**监听适配器的情感图状态切换, 状态切换回调会很快, 可能需要延迟操作
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_EMPTY]
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_LOADING]
 * [com.angcyo.dsladapter.DslAdapterStatusItem.ADAPTER_STATUS_ERROR]
 * */
fun DslAdapter.observeAdapterStatusChange(action: (from: Int, to: Int) -> Unit) {
    dslAdapterStatusItem.onItemStateChange = action
}