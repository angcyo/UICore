package com.angcyo.dialog2

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dialog2.widget.ArrayWheelAdapter
import com.angcyo.library.L
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toStr
import com.angcyo.library.extend.IToDrawable
import com.angcyo.library.extend.IToRightDrawable
import com.angcyo.library.extend.IToText
import com.angcyo.widget.DslViewHolder
import com.contrarywind.view.WheelView
import kotlin.math.max
import kotlin.math.min

/**
 * 3D滚轮选择对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class WheelDialogConfig : BaseDialogConfig() {

    /**数据集合*/
    var wheelItems: List<Any>? = null

    /**是否无限循环*/
    var wheelCyclic = false

    /**设置选中项, -1不设置*/
    var wheelSelectedIndex = -1

    /**单位设置*/
    var wheelUnit: CharSequence? = null

    /**选中回调, 返回true拦截默认操作*/
    var wheelItemSelectorAction: (dialog: Dialog, index: Int, item: Any) -> Boolean =
        { dialog, index, item ->
            L.i("选中->$index:${wheelItemToStringAction(item)}")
            false
        }

    /**上屏显示转换回调*/
    var wheelItemToStringAction: (item: Any) -> CharSequence? = {
        when (it) {
            is CharSequence -> it
            is IToText -> it.toText()
            else -> it.toStr()
        }
    }

    //内部变量
    var _selectedIndex = -1

    var _wheelView: WheelView? = null

    init {
        dialogTitle = "请选择"
        dialogLayoutId = R.layout.lib_dialog_wheel_layout

        positiveButtonListener = { dialog, _ ->
            _wheelView?.apply {
                if (isScrollSetting) {
                    _selectedIndex = currentItem
                }
            }

            if (_selectedIndex in 0 until (wheelItems?.size ?: 0) &&
                wheelItemSelectorAction.invoke(dialog, _selectedIndex, wheelItems!![_selectedIndex])
            ) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.enable(R.id.dialog_positive_button, !wheelItems.isNullOrEmpty())

        dialogViewHolder.v<WheelView>(R.id.lib_wheel_view)?.apply {
            _wheelView = this

            val stringList = mutableListOf<String>()

            for (item in wheelItems ?: emptyList()) {
                stringList.add("${wheelItemToStringAction(item)}")
            }

            setOnItemSelectedListener {
                _selectedIndex = it
                L.v("wheel selected $it")
            }

            adapter = object : ArrayWheelAdapter<String>(stringList) {
                override fun onDrawOnText(
                    wheelView: WheelView,
                    canvas: Canvas,
                    text: String?,
                    textDrawX: Float,
                    textDrawY: Float,
                    textDrawPaint: Paint,
                    index: Int,
                    textBounds: Rect
                ) {
                    if (!text.isNullOrBlank()) {
                        wheelItems?.get(index)?.let { item ->

                            val textHeight = textBounds.height()

                            //draw
                            fun drawDrawable(drawable: Drawable, left: Float) {
                                val height = min(textHeight, drawable.minimumHeight)
                                val width = min(height, drawable.minimumWidth)
                                drawable.setBounds(
                                    left.toInt(),
                                    textHeight / 2 - height / 2,
                                    (left + width).toInt(),
                                    textHeight / 2 + height / 2
                                )
                                drawable.draw(canvas)
                            }

                            if (item is IToDrawable) {
                                item.toDrawable()?.let { drawable ->
                                    val f = textHeight * 1f / drawable.minimumHeight
                                    val width = max(textHeight, (drawable.minimumWidth * f).toInt())
                                    val offset = 4 * dp
                                    val left = textDrawX - width - offset
                                    drawDrawable(drawable, left)
                                }
                            }
                            if (item is IToRightDrawable) {
                                item.toRightDrawable()?.let { drawable ->
                                    val width = textBounds.width()
                                    val offset = 4 * dp
                                    val left = textDrawX + width + offset
                                    drawDrawable(drawable, left)
                                }
                            }
                        }
                    }
                }
            }

            setCyclic(wheelCyclic)

            //wheel 在没有滑动的时候, 是不会触发[SelectedListener]的
            currentItem = if (wheelSelectedIndex in 0 until (wheelItems?.size ?: 0)) {
                wheelSelectedIndex
            } else {
                0
            }
            _selectedIndex = currentItem
        }

        //空数据提示
        dialogViewHolder.visible(R.id.lib_empty_view, wheelItems.isNullOrEmpty())

        //单位
        dialogViewHolder.tv(R.id.lib_unit_view)?.text = wheelUnit
    }

    /**添加Item*/
    fun addDialogItem(any: Any) {
        if (wheelItems is MutableList) {
        } else {
            wheelItems = mutableListOf()
        }
        val list = wheelItems
        if (list is MutableList) {
            list.add(any)
        }
    }
}

/**
 * 3D滚轮选择对话框
 * */
fun Context.wheelDialog(config: WheelDialogConfig.() -> Unit): Dialog {
    return WheelDialogConfig().run {
        configBottomDialog(this@wheelDialog)
        //wheelItems
        //wheelItemSelectorAction
        config()
        show()
    }
}

