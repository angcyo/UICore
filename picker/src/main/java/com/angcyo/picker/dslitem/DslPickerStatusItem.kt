package com.angcyo.picker.dslitem

import com.angcyo.dsladapter.DslAdapterStatusItem
import com.angcyo.picker.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

class DslPickerStatusItem : DslAdapterStatusItem() {
    init {
        itemStateLayoutMap[ADAPTER_STATUS_LOADING] = R.layout.picker_loading_layout
        itemStateLayoutMap[ADAPTER_STATUS_EMPTY] = R.layout.picker_empty_layout
    }
}