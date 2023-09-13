package com.angcyo.dialog2.dslitem

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog2.R
import com.angcyo.dialog2.WheelDateDialogConfig
import com.angcyo.dialog2.wheelDateDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.DslBaseLabelItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.item.style.itemText
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.toCalendar
import com.angcyo.library.ex.addYear
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.toCalendar
import com.angcyo.library.ex.toTime
import com.angcyo.widget.DslViewHolder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelWheelDateItem : DslBaseLabelItem(), ITextItem {

    companion object {

        /**选择 年月日 时分秒*/
        const val TYPE_ALL = 0

        /**选择 年月日*/
        const val TYPE_DATE = 1

        /**时分秒*/
        const val TYPE_TIME = 2

        /**时分*/
        const val TYPE_TIME_2 = 3

        /**默认结束时间*/
        val dateEndTime: Long
            get() = Calendar.getInstance().addYear(10).timeInMillis
    }

    var itemDateTypeArray = booleanArrayOf(true, true, true, false, false, false)

    /**快速type设置*/
    var itemDateType = TYPE_DATE
        set(value) {
            field = value
            itemDateTypeArray = when (value) {
                TYPE_ALL -> {
                    itemPattern = "$itemDatePattern $itemTimePattern"
                    booleanArrayOf(true, true, true, true, true, true)
                }

                TYPE_TIME -> {
                    itemPattern = itemTimePattern
                    booleanArrayOf(false, false, false, true, true, true)
                }

                TYPE_TIME_2 -> {
                    itemPattern = itemTimePattern
                    booleanArrayOf(false, false, false, true, true, false)
                }

                else -> {
                    itemPattern = itemDatePattern
                    booleanArrayOf(true, true, true, false, false, false)
                }
            }
        }

    /**分区配置时间格式*/
    var itemDatePattern = "yyyy-MM-dd"
    var itemTimePattern = "HH:mm:ss"

    /**日期 时间 全格式, 最终使用格式*/
    var itemPattern = itemDatePattern

    /**设置显示文本时使用的格式, 默认是[itemPattern]*/
    var itemShowTextPattern: String? = null

    /**开始和结束时间, 毫秒*/
    var itemDateStartTime = 0L
    var itemDateEndTime = nowTime()

    /**当前选中的时间, 毫秒. 默认是当前的时间*/
    var itemCurrentTime = nowTime()

    //选中的[Date]对象
    var _itemDateSelectDate: Date? = null

    /**选中的时间*/
    val itemDateSelectedTime: Long
        get() = _itemDateSelectDate?.time ?: itemCurrentTime

    /**点击确定后回调*/
    var itemDateSelectListener: (dialog: Dialog, date: Date) -> Boolean = { _, _ ->
        false
    }

    var itemConfigDialog: (WheelDateDialogConfig) -> Unit = {

    }

    override var textItemConfig: TextItemConfig = TextItemConfig()

    init {
        itemLayoutId = R.layout.dsl_wheel_date_item

        itemClick = {
            if (itemEnable) {
                showItemWheelDateDialog(it.context)
            }
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.visible(R.id.lib_right_ico_view, itemEnable)
    }

    @CallPoint
    fun showItemWheelDateDialog(context: Context) {
        context.wheelDateDialog {
            dialogTitle = labelItemConfig.itemLabelText

            dateType = itemDateTypeArray

            //开始时间设置
            if (itemDateStartTime > 0) {
                dateStartDate =
                    itemDateStartTime.toTime(itemPattern).toCalendar(itemPattern)
            }
            //结束时间设置
            if (itemDateEndTime > 0) {
                dateEndDate = itemDateEndTime.toTime(itemPattern).toCalendar(itemPattern)
            }

            //当前时间设置
            dateCurrent =
                _itemDateSelectDate?.toCalendar() ?: itemCurrentTime.toTime(itemPattern)
                    .toCalendar(itemPattern)

            //选中回调
            dateSelectAction = { dialog, date ->
                if (itemDateSelectListener(dialog, date)) {
                    //拦截了
                    true
                } else {
                    onSelfItemSelectDate(date)
                    false
                }
            }

            itemConfigDialog(this)
        }
    }

    /**[updateSelectWheelDate]*/
    fun updateSelectWheelDate(millis: String?) {
        millis ?: return
        updateSelectWheelDate(millis.toLongOrNull() ?: nowTime())
    }

    /**[millis] 13位毫秒时间*/
    fun updateSelectWheelDate(millis: Long) {
        updateSelectWheelDate(Date(millis))
    }

    fun updateSelectWheelDate(date: Date) {
        _itemDateSelectDate = date
        val dateFormat: DateFormat = SimpleDateFormat(itemShowTextPattern ?: itemPattern)
        itemText = dateFormat.format(date)
    }

    /**选中[Date]回调*/
    open fun onSelfItemSelectDate(date: Date) {
        updateSelectWheelDate(date)
        itemChanging = true
    }
}