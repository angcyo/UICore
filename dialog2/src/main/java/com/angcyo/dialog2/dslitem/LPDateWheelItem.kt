package com.angcyo.dialog2.dslitem

import com.angcyo.dialog2.R

/**
 * 日期选择器item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/06
 */
open class LPDateWheelItem : DslLabelWheelDateItem() {

    init {
        itemLayoutId = R.layout.lp_date_wheel_item
        itemDateType = TYPE_DATE
    }

}