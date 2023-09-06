package com.angcyo.item.style

/**
 * 加减自增item
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
interface IIncrementItem : IStepAdjustItem

/**当前的值
 * [com.angcyo.item.style.IStepAdjustItem.itemStepAdjustValue]*/
var IIncrementItem.itemIncrementValue: CharSequence?
    get() = itemStepAdjustValue
    set(value) {
        itemStepAdjustValue = value
    }

var IIncrementItem.itemIncrementMinValue: Any?
    get() = itemStepAdjustMinValue
    set(value) {
        itemStepAdjustMinValue = value
    }

var IIncrementItem.itemIncrementMaxValue: Any?
    get() = itemStepAdjustMaxValue
    set(value) {
        itemStepAdjustMaxValue = value
    }

