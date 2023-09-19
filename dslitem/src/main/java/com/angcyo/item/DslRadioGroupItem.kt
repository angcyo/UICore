package com.angcyo.item

import com.angcyo.item.style.itemCheckLayoutId
import com.angcyo.item.style.itemMultiMode
import com.angcyo.library.ex._dimen

/**
 * 带Label的单选选择item
 * [DslCheckGroupItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/06
 */
open class DslRadioGroupItem : DslCheckGroupItem() {

    init {
        itemLayoutId = R.layout.dsl_radio_group_item

        R.layout.layout_check
        R.layout.layout_check_lp
        itemCheckLayoutId = R.layout.layout_check_lp

        itemFlowHorizontalSpace = _dimen(R.dimen.lib_xhdpi)
        itemFlowVerticalSpace = itemFlowHorizontalSpace

        //单选
        itemMultiMode = false
    }

}