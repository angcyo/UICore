package com.angcyo.dialog2.dslitem

/**
 * 时间选择器item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/06
 */
open class LPTimeWheelItem : LPDateWheelItem() {

    init {
        itemDateType = TYPE_TIME
    }

}