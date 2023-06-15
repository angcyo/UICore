package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import androidx.annotation.Px
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.library.L
import com.angcyo.library.ex._color
import com.angcyo.library.ex._dimen
import com.angcyo.widget.DslViewHolder
import com.bigkoo.pickerview.view.WheelTime
import com.bigkoo.pickerview.view.WheelTime.dateFormat
import com.contrarywind.view.WheelView
import java.util.Calendar
import java.util.Date

/**
 * 日期时间选择对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/12
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class WheelDateDialogConfig : BaseDialogConfig() {

    //time picker 年月日 时分秒
    var dateType = booleanArrayOf(true, true, true, false, false, false)//显示类型，默认显示： 年月日
    var dateTextGravity = Gravity.CENTER

    /**px 单位*/
    @Px
    var dateTextSize: Float = _dimen(R.dimen.wheel_text_size).toFloat()

    var isLunarCalendar = false//是否显示农历

    var dateStartYear: Int = 0//开始年份
    var dateEndYear: Int = 0//结尾年份

    var dateCurrent: Calendar? = null//当前选中时间
    var dateStartDate: Calendar? = null//开始时间
    var dateEndDate: Calendar? = null//终止时间

    var labelYear: String? = null
    var labelMonth: String? = null
    var labelDay: String? = null
    var labelHours: String? = null
    var labelMinutes: String? = null
    var labelSeconds: String? = null

    var x_offset_year: Int = 0
    var x_offset_month: Int = 0
    var x_offset_day: Int = 0
    var x_offset_hours: Int = 0
    var x_offset_minutes: Int = 0
    var x_offset_seconds: Int = 0

    var dateCyclic = false//是否循环

    var dividerColor = -0x2a2a2b //分割线的颜色
    var textColorOut = -0x575758 //分割线以外的文字颜色
    var textColorCenter = -0xd5d5d6 //分割线之间的文字颜色
    var textColorCenterNight = _color(R.color.text_general_color)
    var isCenterLabel = false//是否只显示中间的label,默认每个item都显示
    var isDrawLabelOnTextBehind = true

    var dividerType: WheelView.DividerType = WheelView.DividerType.FILL//分隔线类型

    var lineSpacingMultiplier = 1.6f // 条目间距倍数 默认1.6

    /**点击确定后回调*/
    var dateSelectAction: (dialog: Dialog, date: Date) -> Boolean = { _, _ ->
        false
    }

    /**
     * 滚动的时候回调
     * */
    var dateChangedAction: (dialog: Dialog, date: Date) -> Unit = { _, _ ->

    }

    var _wheelTime: WheelTime? = null

    init {
        dialogLayoutId = R.layout.lib_dialog_date_wheel_layout

        positiveButtonListener = { dialog, _ ->
            _wheelTime?.apply {
                try {
                    val date = dateFormat.parse(time)
                    if (date != null && dateSelectAction.invoke(dialog, date)) {

                    } else {
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    L.e(e)
                    dialog.dismiss()
                }
            } ?: dialog.dismiss()
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val wheelTime = WheelTime(
            dialogViewHolder.view(R.id.wheel_wrap_layout),
            dateType,
            dateTextGravity,
            dateTextSize
        )

        _wheelTime = wheelTime

        wheelTime.apply {
            setSelectChangeCallback {
                try {
                    val date = dateFormat.parse(time)
                    dateChangedAction(dialog, date!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            isLunarMode = isLunarCalendar

            if (dateStartYear != 0 &&
                dateEndYear != 0 &&
                dateStartYear <= dateEndYear
            ) {
                setRange()
            }

            //若手动设置了时间范围限制
            if (dateStartDate != null && dateEndDate != null) {
                if (dateStartDate!!.timeInMillis > dateEndDate!!.timeInMillis) {
                    throw IllegalArgumentException("startDate can't be later than endDate")
                } else {
                    setRangDate()
                }
            } else if (dateStartDate != null) {
                if (dateStartDate!!.get(Calendar.YEAR) < 1900) {
                    throw IllegalArgumentException("The startDate can not as early as 1900")
                } else {
                    setRangDate()
                }
            } else if (dateEndDate != null) {
                if (dateEndDate!!.get(Calendar.YEAR) > 2100) {
                    throw IllegalArgumentException("The endDate should not be later than 2100")
                } else {
                    setRangDate()
                }
            } else {//没有设置时间范围限制，则会使用默认范围。
                setRangDate()
            }

            setTime()
            setLabels(
                labelYear,
                labelMonth,
                labelDay,
                labelHours,
                labelMinutes,
                labelSeconds
            )
            setTextXOffset(
                x_offset_year, x_offset_month, x_offset_day,
                x_offset_hours, x_offset_minutes, x_offset_seconds
            )

            setCyclic(dateCyclic)
            setDividerColor(dividerColor)
            setDividerType(dividerType)
            setLineSpacingMultiplier(lineSpacingMultiplier)
            setTextColorOut(textColorOut)
            if (vmApp<NightModel>().isDarkMode) {
                setTextColorCenter(textColorCenterNight)
            } else {
                setTextColorCenter(textColorCenter)
            }
            isCenterLabel(isCenterLabel)

            dialogViewHolder.v<WheelView>(R.id.year)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
            dialogViewHolder.v<WheelView>(R.id.month)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
            dialogViewHolder.v<WheelView>(R.id.day)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
            dialogViewHolder.v<WheelView>(R.id.hour)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
            dialogViewHolder.v<WheelView>(R.id.min)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
            dialogViewHolder.v<WheelView>(R.id.second)?.isDrawLabelOnTextBehind =
                isDrawLabelOnTextBehind
        }
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private fun setRange() {
        _wheelTime?.let {
            it.startYear = dateStartYear
            it.endYear = dateEndYear
        }
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private fun setRangDate() {
        _wheelTime?.setRangDate(dateStartDate, dateEndDate)
        initDefaultSelectedDate()
    }

    private fun initDefaultSelectedDate() {
        if (dateCurrent == null) {
            //如果手动设置了时间范围
            if (dateStartDate != null && dateEndDate != null) {
                //若默认时间未设置，或者设置的默认时间越界了，则设置默认选中时间为开始时间。
                if (dateCurrent == null || dateCurrent!!.timeInMillis < dateStartDate!!.timeInMillis
                    || dateCurrent!!.timeInMillis > dateEndDate!!.timeInMillis
                ) {
                    dateCurrent = dateStartDate
                }
            } else if (dateStartDate != null) {
                //没有设置默认选中时间,那就拿开始时间当默认时间
                dateCurrent = dateStartDate
            } else if (dateEndDate != null) {
                dateCurrent = dateEndDate
            }
        }
    }

    /**
     * 设置选中时间,默认选中当前时间
     */
    private fun setTime() {
        val year: Int
        val month: Int
        val day: Int
        val hours: Int
        val minute: Int
        val seconds: Int
        val calendar = Calendar.getInstance()

        if (dateCurrent == null) {
            calendar.timeInMillis = System.currentTimeMillis()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
            hours = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            seconds = calendar.get(Calendar.SECOND)
        } else {
            year = dateCurrent!!.get(Calendar.YEAR)
            month = dateCurrent!!.get(Calendar.MONTH)
            day = dateCurrent!!.get(Calendar.DAY_OF_MONTH)
            hours = dateCurrent!!.get(Calendar.HOUR_OF_DAY)
            minute = dateCurrent!!.get(Calendar.MINUTE)
            seconds = dateCurrent!!.get(Calendar.SECOND)
        }

        _wheelTime?.setPicker(year, month, day, hours, minute, seconds)
    }
}

/**
 * 3D滚轮日期选择/时间
 * */
fun Context.wheelDateDialog(config: WheelDateDialogConfig.() -> Unit): Dialog {
    return WheelDateDialogConfig().run {
        configBottomDialog(this@wheelDateDialog)
        config()
        show()
    }
}

