package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.ViewGroup
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.library.L
import com.angcyo.library.ex.copy
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.toBitmapDrawable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.motionEvent
import com.skydoves.colorpickerview.AlphaTileView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

/**
 * 颜色选择Dialog, 圆饼颜色选择
 * https://github.com/LaserPeckerIst/ColorPickerView
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
class ColorPickerDialogConfig : BaseDialogConfig() {

    /**默认的颜色*/
    var initialColor: Int = Color.WHITE

    /**选中的颜色*/
    var selectedColor: Int = Color.TRANSPARENT

    /**[setHsvPaletteDrawable]
     * 最后也是走[setPaletteDrawable]
     * [com.skydoves.colorpickerview.ColorHsvPalette]
     * */
    var hsvPaletteDrawable: Boolean = false

    /**调色板, 从此[Drawable]中获取颜色, 需要是[BitmapDrawable]
     * [setPaletteDrawable]
     * [com.angcyo.github.widget.drawable.ColorPaletteDrawable]
     * */
    var colorPaletteDrawable: Drawable? = null

    /**是否激活颜色的透明度*/
    var enableAlpha = true

    /**是否激活颜色的亮度*/
    var enableBrightness = true

    /**选中回调, 返回true拦截默认操作*/
    var colorPickerAction: (dialog: Dialog, color: Int) -> Boolean =
        { dialog, color ->
            L.i("选中颜色->$color")
            false
        }

    init {
        dialogTitle = "请选择颜色"
        dialogLayoutId = R.layout.lib_dialog_color_picker_layout

        positiveButtonListener = { dialog, _ ->

            if (colorPickerAction.invoke(dialog, selectedColor)) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        val colorPickerView = dialogViewHolder.v<ColorPickerView>(R.id.lib_color_picker_view)
        val alphaSlideBar = dialogViewHolder.v<AlphaSlideBar>(R.id.lib_alpha_slide_bar)
        val brightnessSlideBar =
            dialogViewHolder.v<BrightnessSlideBar>(R.id.lib_brightness_slide_bar)
        val alphaTileView =
            dialogViewHolder.v<AlphaTileView>(R.id.lib_alpha_tile_view)
        val textView = dialogViewHolder.tv(R.id.lib_text_view)

        dialogViewHolder.visible(R.id.lib_alpha_slide_bar, enableAlpha)
        dialogViewHolder.visible(R.id.lib_brightness_slide_bar, enableBrightness)

        textView?.setOnClickListener {
            textView.text?.copy(it.context)
        }

        colorPickerView?.apply {
            //slider
            if (enableAlpha) {
                attachAlphaSlider(alphaSlideBar!!)
            }
            if (enableBrightness) {
                attachBrightnessSlider(brightnessSlideBar!!)
            }

            //回调
            setColorListener(object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                    selectedColor = envelope.color
                    alphaTileView?.setPaintColor(selectedColor)
                    textView?.text = "#${envelope.hexCode}"
                }
            })

            //发送一个手势事件, 触发颜色回调
            fun touchUp() {
                post {
                    val event = motionEvent(
                        MotionEvent.ACTION_UP,
                        measuredWidth / 2f,
                        measuredHeight / 2f
                    )
                    dispatchTouchEvent(event)
                    event.recycle()

                    //flag
                    val bubbleFlag = BubbleFlag(context)
                    bubbleFlag.flagMode = FlagMode.FADE
                    colorPickerView.flagView = bubbleFlag
                }
            }

            //set
            colorPaletteDrawable?.let {
                if (it is BitmapDrawable) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    setPaletteDrawable(it)
                } else {
                    layoutParams.height = 100 * dpi
                }
                post {
                    if (it !is BitmapDrawable) {
                        setPaletteDrawable(it.toBitmapDrawable(measuredWidth, measuredHeight))
                    }

                    touchUp()
                }
            }.elseNull {
                //flag
                if (hsvPaletteDrawable) {
                    post {
                        setHsvPaletteDrawable()
                        touchUp()
                    }
                } else {
                    val bubbleFlag = BubbleFlag(context)
                    bubbleFlag.flagMode = FlagMode.FADE
                    colorPickerView.flagView = bubbleFlag
                    setInitialColor(initialColor)
                }
            }
        }
    }
}


/**
 * 圆盘颜色选择对话框
 * */
fun Context.colorPickerDialog(config: ColorPickerDialogConfig.() -> Unit): Dialog {
    return ColorPickerDialogConfig().run {
        configBottomDialog(this@colorPickerDialog)
        //initialColor
        //paletteDrawable
        config()
        show()
    }
}

