package com.angcyo.item

import android.widget.TextView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.keyboard.NumberKeyboardPopupConfig
import com.angcyo.item.keyboard.keyboardNumberWindow
import com.angcyo.library.utils.isChildClassOf
import com.angcyo.widget.DslViewHolder

/**
 * 数字键盘属性
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-18
 */
open class DslPropertyNumberItem : DslBasePropertyItem() {

    companion object {

        fun isNumber(cls: Class<*>): Boolean = isShort(cls) ||
                isByte(cls) ||
                isInt(cls) ||
                isLong(cls) ||
                isFloat(cls) ||
                isDouble(cls) ||
                cls.isChildClassOf(Number::class.java)

        fun isShort(cls: Class<*>): Boolean =
            cls.name == "short" || cls.isChildClassOf(Short::class.java)

        fun isByte(cls: Class<*>): Boolean =
            cls.name == "byte" || cls.isChildClassOf(Byte::class.java)

        fun isInt(cls: Class<*>): Boolean =
            cls.name == "int" || cls.isChildClassOf(Int::class.java)

        fun isLong(cls: Class<*>): Boolean =
            cls.name == "long" || cls.isChildClassOf(Long::class.java)

        fun isFloat(cls: Class<*>): Boolean =
            cls.name == "float" || cls.isChildClassOf(Float::class.java)

        fun isDouble(cls: Class<*>): Boolean =
            cls.name == "double" || cls.isChildClassOf(Double::class.java)
    }

    /**属性数值*/
    var itemPropertyNumber: Number? = null

    init {
        itemLayoutId = R.layout.dsl_property_number_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(R.id.lib_text_view)?.text = "${itemPropertyNumber ?: ""}"

        //
        itemHolder.click(R.id.lib_text_view) {
            it.context.keyboardNumberWindow(it) {
                keyboardBindTextView = it as? TextView
                bindPendingDelay = -1 //关闭限流输入
                if (itemPropertyNumber is Float || itemPropertyNumber is Double) {
                    //小数
                } else {
                    //整数
                    removeKeyboardStyle(NumberKeyboardPopupConfig.STYLE_DECIMAL)
                }
                onNumberResultAction = { number ->
                    itemPropertyNumber = when (itemPropertyNumber) {
                        is Short -> number.toInt().toShort()
                        is Byte -> number.toInt().toByte()
                        is Int -> number.toInt()
                        is Long -> number.toLong()
                        is Float -> number.toFloat()
                        else -> number
                    }
                    itemChanging = true
                }
            }
        }
    }

}