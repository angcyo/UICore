package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.keyboard.NumberKeyboardDialogConfig
import com.angcyo.item.keyboard.numberKeyboardDialog
import com.angcyo.item.style.itemInfoText
import com.angcyo.library.component.RegionTouchDetector
import com.angcyo.library.ex.decimal
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.progressValueFraction
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.progress.DslSeekBar
import kotlin.math.roundToInt

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2024/04/24
 *
 * 上下结构
 * 上: label    value
 * 下: seekBar
 *
 * ```
 * open class CanvasSeekBarItem : DslSeekBarItem() {
 *
 *     init {
 *         val color = _color(R.color.canvas_primary)
 *         itemSeekBarColor = color
 *         itemSeekBgColors = "${_color(R.color.canvas_line)}"
 *
 *         itemSeekProgressHeight = 4 * dpi
 *     }
 *
 * }
 * ```
 */
open class DslSeekBarItem : DslSeekBarInfoItem() {

    /**当前的值*/
    var itemNumberValue: Any? = null

    /**[itemNumberValue]前面的文本*/
    var itemNumberLeading: CharSequence? = null

    //---

    /**键盘输入相关属性*/

    /**如果是小数,则需要保留的小数点位数*/
    var itemDecimalCount: Int = NumberKeyboardDialogConfig.numberDecimalCount

    /**进度值的类型, 具有类型的数值*/
    var itemSeekProgressType: Any? = null

    /**键盘输入的最小值*/
    var itemNumberMinValue: Any? = null

    /**键盘输入的最大值*/
    var itemNumberMaxValue: Any? = null

    //---

    /**seek bar 的颜色*/
    var itemSeekBarColor: Int? = null

    /**[itemSeekBarColor]渐变色, 优先使用*/
    var itemSeekBarColors: String? = null

    /**seek bar 的背景渐变颜色, 支持多个颜色
     * ```
     *  "#34FF00,#F1E804,#FF8E00,#FF0108"
     * ```*/
    var itemSeekBgColors: String? = null

    /**seek bar的高度*/
    var itemSeekProgressHeight: Int = 8 * dpi

    /**是否要显示滑块*/
    var itemShowSeekBar: Boolean = true

    /**[itemNumberValue]转换成对应类型的数值*/
    val _itemTypeValue: Any?
        get() {
            if (itemSeekProgressType is Float) {
                if (itemNumberValue is Float) {
                    return itemNumberValue
                }
                return itemNumberValue?.toString()?.toFloatOrNull()
            }
            if (itemSeekProgressType is Double) {
                if (itemNumberValue is Double) {
                    return itemNumberValue
                }
                return itemNumberValue?.toString()?.toFloatOrNull()
            }
            if (itemSeekProgressType is Int) {
                if (itemNumberValue is Int) {
                    return itemNumberValue
                }
                return itemNumberValue?.toString()?.toFloatOrNull()?.toInt()
            }
            if (itemSeekProgressType is Long) {
                if (itemNumberValue is Long) {
                    return itemNumberValue
                }
                return itemNumberValue?.toString()?.toFloatOrNull()?.toInt()
            }
            return itemNumberValue
        }

    /**[itemNumberValue]对应显示的字符串*/
    val _itemValueString: String?
        get() {
            if (itemSeekProgressType is Float || itemSeekProgressType is Double) {
                return (_itemTypeValue as? Float)?.decimal(itemDecimalCount, fadedUp = true)
            }
            if (itemSeekProgressType is Int || itemSeekProgressType is Long) {
                return (_itemTypeValue as? Int)?.toString()
            }
            return itemNumberValue?.toString()
        }

    /**比例 0~1 的值*/
    val _itemProgressFraction: Float
        get() = progressValueFraction(
            itemNumberValue, itemNumberMinValue, itemNumberMaxValue
        )!!

    init {
        itemLayoutId = R.layout.dsl_seek_bar_item

        itemProgressTextFormatAction = {
            _itemValueString ?: ""
        }
    }

    /**使用[number]初始化
     * [number] in [minValue]~[maxValue]*/
    fun initItemValue(number: Any?, minValue: Any? = null, maxValue: Any? = null) {
        if (number is Float || number is Double) {
            itemSeekProgressType = 0f
        } else {
            itemSeekProgressType = 0
        }
        if (itemNumberValue == null) {
            itemNumberValue = itemSeekProgressType
        }
        itemNumberMinValue = minValue
        itemNumberMaxValue = maxValue

        updateItemNumberValue(number)
    }

    /**仅更新[itemNumberValue]的同时, 更新[itemSeekProgress]*/
    fun updateItemNumberValue(number: Any?) {
        if (itemNumberValue is Int || itemNumberValue is Long) {
            itemNumberValue = number?.toString()?.toFloatOrNull()?.roundToInt() ?: itemNumberValue
        } else {
            itemNumberValue = number
        }

        val num = number?.toString()?.toFloatOrNull() ?: 0f
        itemSeekProgress = num
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.gone(R.id.value_leading_view, itemNumberLeading == null)
        itemHolder.tv(R.id.value_leading_view)?.text = itemNumberLeading

        itemHolder.tv(R.id.lib_value_view)?.apply {
            isEnabled = itemEnable
            text = _itemValueString
            clickIt {
                showKeyboardInputDialog(itemHolder)
            }
        }
    }

    override fun initSeekBarView(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<DslSeekBar>(R.id.lib_seek_view)?.apply {
            if (itemSeekBarColors != null) {
                val colors = setTrackGradientColors(itemSeekBarColors)
                if (colors != null) {
                    updateThumbColor(
                        DslProgressBar.getGradientColor(
                            _itemProgressFraction, colors.toList()
                        )
                    )
                } else {
                    val color = itemSeekBarColor
                    if (color != null) {
                        updateThumbColor(color)
                    }
                }
            } else {
                val color = itemSeekBarColor
                if (color != null) {
                    setTrackGradientColors("$color")
                    updateThumbColor(color)
                }
            }

            if (itemSeekBgColors != null) {
                val colors = setBgGradientColors(itemSeekBgColors)
                if (colors != null) {
                    updateThumbColor(
                        DslProgressBar.getGradientColor(
                            _itemProgressFraction, colors.toList()
                        )
                    )
                }
            }

            progressMinValue = itemNumberMinValue?.toString()?.toFloatOrNull() ?: 0f
            progressMaxValue = itemNumberMaxValue?.toString()?.toFloatOrNull() ?: 100f
            progressHeight = itemSeekProgressHeight

            if (itemSeekProgressType != null) {
                progressValueTouchAction = { touchType ->
                    if (touchType == RegionTouchDetector.TOUCH_TYPE_CLICK) {
                        //点击事件类型
                        showKeyboardInputDialog(itemHolder)
                    }
                }
            }
            visible(itemShowSeekBar)
        }
        super.initSeekBarView(itemHolder, itemPosition, adapterItem, payloads)
    }

    /**显示键盘输入对话框*/
    open fun showKeyboardInputDialog(itemHolder: DslViewHolder) {
        itemHolder.context.numberKeyboardDialog {
            dialogTitle = itemInfoText
            decimalCount = itemDecimalCount
            numberValueType = itemSeekProgressType
            numberMinValue = itemNumberMinValue
            numberMaxValue = itemNumberMaxValue
            numberValue = itemNumberValue ?: updateProgressValue(itemSeekProgress)
            onNumberResultAction = { number ->
                number?.let {
                    updateItemNumberValue(number)

                    val fraction = _itemProgressFraction
                    onItemSeekChanged(itemSeekProgress, fraction, true)
                    itemSeekTouchEnd(itemSeekProgress, fraction)
                    updateAdapterItem()
                }
                false
            }
        }
    }

    override fun onItemSeekChanged(value: Float, fraction: Float, fromUser: Boolean) {
        val min = itemNumberMinValue?.toString()?.toFloatOrNull() ?: 0f
        val max = itemNumberMaxValue?.toString()?.toFloatOrNull() ?: 100f
        val number = min + (max - min) * fraction
        updateItemNumberValue(number)
        super.onItemSeekChanged(value, fraction, fromUser)
    }
}