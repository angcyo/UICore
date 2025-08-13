package com.angcyo.library.ex

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Build
import android.os.SystemClock
import com.angcyo.library.component.hawk.LibHawkKeys

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**从整型数中取第[bit]位的数
 * [bit] 从右往左, 第几位, 1开始
 * */
fun Int.bit(bit: Int): Int = (this shr (max(bit, 1) - 1)) and 0x1

/**整型数中, 是否包含另一个整数*/
fun Int?.have(value: Int): Boolean {
    this ?: return false
    return if (this == 0 && value == 0) {
        true
    } else if (this == 0 || value == 0) {
        false
    } else if ((this > 0 && value < 0) || (this < 0 && value > 0)) {
        false
    } else {
        this and value == value
    }
}

/**and 逻辑与, x */
fun Int.remove(value: Int): Int = this and value.inv()

/**and 逻辑或, + */
fun Int.add(value: Int): Int = this or value

/**限制当前的值范围*/
fun Int.clamp(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) = clamp(this, min, max)

/**第4位的最高字节  0x8000 = 32,768, 未定义的资源. 0默认资源*/
val undefined_res = -32_768

/**未定义的整数*/
val undefined_int = -1

/**未定义的大小*/
val undefined_size = Int.MIN_VALUE

/**未定义的浮点数*/
val undefined_float = -1f

/**未定义的颜色*/
val undefined_color = -32_768

fun Int?.or(default: Int) = if ((this ?: 0) > 0) this else default

fun Long?.or(default: Long) = if ((this ?: 0) > 0) this else default

fun Int?.orDef(default: Int) = this ?: default

fun Long?.orDef(default: Long) = this ?: default

/**目标值的最小值是自己,  目标值必须超过自己*/
fun Float.withMinValue(value: Float /*允许的最小值*/) = max(value, this)

fun Float.withMinValue(value: Int) = max(value.toFloat(), this)

fun Int.withMinValue(value: Int) = max(value, this)

/**目标值的最大值是自己,  目标值不能超过自己*/
fun Float.withMaxValue(value: Float /*允许的最大值*/) = min(value, this)

fun Int.withMaxValue(value: Int) = min(value, this)

/**左右对齐指定个数的0*/
fun Int.toZero(leftCount: Int = 2, rightCount: Int = 0): String {
    val b = StringBuffer()
    for (i in 0 until leftCount) {
        b.append("0")
    }
    if (rightCount > 0) {
        b.append(".")
        for (i in 0 until rightCount) {
            b.append("0")
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val df = DecimalFormat("$b")
        return df.format(this)
    } else {
        val df = java.text.DecimalFormat("$b")
        return df.format(this)
    }
}

/**
 * 很大的数, 折叠展示成 xx.xxw xx.xxk xx
 * 转换成短数字
 * */
fun Int.toShortString() = this.toLong().toShortString()

fun Long.toShortString(
    bases: Array<Long> = arrayOf(10000, 1000), //设定的基数
    units: Array<String> = arrayOf("w", "kw"), //基数对应的单位
    digits: Array<Int> = arrayOf(2, 2) //每个单位, 需要保留的小数点位数
): String {
    val builder = StringBuilder()

    //基数, 对应的单位
    val levelNumList = mutableListOf<Long>()
    var num: Long = this
    var level = 0

    while (true) {
        if (num != 0L) {

            //基数
            val base: Long = bases.getOrNull(level) ?: Long.MAX_VALUE //最后一级结束控制

            //基数计算后的余数
            val levelNum = num % base

            levelNumList.add(levelNum)

            level++
            //下一个参与计算的数
            num /= base
        } else if (num == 0L && levelNumList.isEmpty()) {
            levelNumList.add(num)
            break
        } else {
            break
        }
    }

    levelNumList.lastOrNull()?.apply {
        builder.append(this)

        val lastIndex = levelNumList.lastIndex - 1
        val dec = digits.getOrNull(lastIndex) ?: -1
        if (dec > 0) {
            //需要小数

            levelNumList.getOrNull(lastIndex)?.apply {
                //有小数
                val numString = this.toString()

                builder.append(('.'))
                builder.append(numString.subSequence(0, min(dec, numString.length)))
            }
        }

        //单位
        units.getOrNull(lastIndex)?.apply {
            builder.append(this)
        }
    }

    return builder.toString()
}

/**构建一个16位的整数*/
fun generateInt(): Int = SystemClock.uptimeMillis().toInt() and 0xFFFF

/**转换成 100% 80% 的形式*/
fun Number.toRatioStr(sum: Number): String = "${(this.toFloat() / sum.toFloat() * 100).toInt()}%"

/**科学计数转成普通数字
 * 8.64e+07
 * 5344.34234e3
 * */
fun String?.toBigLongOrNull(): Long? {
    return this?.toLongOrNull() ?: try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            BigDecimal(this).toLong()
        } else {
            java.math.BigDecimal(this).toLong()
        }
    } catch (e: Exception) {
        null
    }
}

/**确保当前的浮点对象返回一个有效的值*/
fun Float.ensure(def: Float = 0f): Float {
    if (isNaN()) {
        //不是一个数字
        return def
    }
    if (isInfinite()) {
        //无穷大
        return def
    }
    if (isFinite()) {
        //有效的数值
        return this
    }
    return this
}

fun Double.ensure(def: Double = 1.0): Double {
    if (isNaN()) {
        //不是一个数字
        return def
    }
    if (isInfinite()) {
        //无穷大
        return def
    }
    return this
}

/**在指定误差范围[acceptableError]内, 是否相等*/
fun Float.equalError(
    other: Float,
    acceptableError: Float = LibHawkKeys.floatAcceptableError
): Boolean = (this - other).absoluteValue < acceptableError

fun Double.equalError(
    other: Double,
    acceptableError: Double = LibHawkKeys.doubleAcceptableError
): Boolean = (this - other).absoluteValue < acceptableError

/**判断2个浮点数是否相等, 数值全等*/
fun Float.eq(other: Float): Boolean =
    java.lang.Float.floatToIntBits(this) == java.lang.Float.floatToIntBits(other)

/**判断2个浮点数是否相等, 数值全等*/
fun Double.eq(other: Double): Boolean =
    java.lang.Double.doubleToLongBits(this) == java.lang.Double.doubleToLongBits(other)

/**判断2个浮点数是否相等, 数值全等*/
fun Float.equal2(value: Float): Boolean = toDouble().equal2(value.toDouble())

/**判断2个浮点数是否相等, 数值全等*/
fun Double.equal2(value: Double): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val data1 = BigDecimal(this)
        val data2 = BigDecimal(value)
        return data1.compareTo(data2) == 0
    } else {
        val data1 = java.math.BigDecimal(this)
        val data2 = java.math.BigDecimal(value)
        return data1.compareTo(data2) == 0
    }
}

/**有损转成[Float]类型
 * 0.00001 -> 1.0E-5
 * 0.0001 -> 1.0E-4
 * 0.001 -> 0.001
 * */
fun Double.toLossyFloat(threshold: Double = 0.001): Float = if (this.absoluteValue <= threshold) {
    0.0
} else {
    this
}.toFloat()

/**保留小数点多少位*/
fun Float.formatShow(n: Int = 2): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val instance = NumberFormat.getInstance()
        instance.isGroupingUsed = false //设置不使用科学计数器
        instance.maximumFractionDigits = n //小数点最大位数
        return instance.format(this)
    } else {
        val instance = java.text.NumberFormat.getInstance()
        instance.isGroupingUsed = false //设置不使用科学计数器
        instance.maximumFractionDigits = n //小数点最大位数
        return instance.format(this)
    }
}

/**保留小数点多少位*/
fun Double.formatShow(n: Int = 2): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val instance = NumberFormat.getInstance()
        instance.isGroupingUsed = false //设置不使用科学计数器
        instance.maximumFractionDigits = n //小数点最大位数
        return instance.format(this)
    } else {
        val instance = java.text.NumberFormat.getInstance()
        instance.isGroupingUsed = false //设置不使用科学计数器
        instance.maximumFractionDigits = n //小数点最大位数
        return instance.format(this)
    }
}

/**优先确保1.0 返回 1
 * 其他情况1.3 返回 1.3*/
fun Float.ensureInt(): String {
    val f = this
    val int = this.toInt()
    return if (f == int.toFloat()) {
        "$int"
    } else {
        "$f"
    }
}

fun Double.ensureInt(): String {
    val d = this
    val int = this.toInt()
    return if (d == int.toDouble()) {
        "$int"
    } else {
        "$d"
    }
}

/**当前的值, 是否是旋转值, 就是有旋转角度*/
fun Float.isRotated(): Boolean = this != 0f && this != 360f

/**索引从一个值到另一个值计算出映射的value值
 *  [index] 需要计算的索引值
 *  [fromIndex] [fromValue] 索引和值的开始映射对应值
 *  [valueStep] 值的增量步长
 *  */
fun calcIncrementValue(index: Int, fromIndex: Int, fromValue: Float, valueStep: Float): Float =
    fromValue + (index - fromIndex) * valueStep

/** 2116779516 -> 7E2B7DFC
 * 将一个int用ascii字符表示出来
 * [this] 输入的值
 * [length] 需要输出多少个ascii字符, 4位一个ascii字符
 * */
fun Int.toAsciiString(length: Int = 32 / 4): String {
    val list = mutableListOf<Char>()
    for (b in 0 until length) {
        //每4位取一次值
        val char = (this shr (b * 4)) and 0xF
        //再转成十六进制, 这样就可以限定值为[0~F]
        val hex = char.toHexString(1)
        list.add(hex.first())
    }
    return list.reversed().connect("")
}

/**将ascii对应的int值解析出来*/
fun String.toAsciiInt(): Int {
    var result = 0
    val length = length * 4
    forEachIndexed { index, char ->
        //7e2b7dfc
        val hex = char.toString()
        val int = hex.toHexInt()
        result = result or (int shl (length - (index + 1) * 4))
    }
    return result
}

/**将ARGB [0~255]颜色,转换成OpenGL RGBA [0~1]的颜色*/
fun Int.toOpenGLColor(): FloatArray = floatArrayOf(
    ((this shr 16) and 0xFF) / 255f,
    ((this shr 8) and 0xFF) / 255f,
    (this and 0xFF) / 255f,
    ((this shr 24) and 0xFF) / 255f
)

fun Int.toOpenGLColorList(): List<Float> = listOf(
    ((this shr 16) and 0xFF) / 255f,
    ((this shr 8) and 0xFF) / 255f,
    (this and 0xFF) / 255f,
    ((this shr 24) and 0xFF) / 255f
)