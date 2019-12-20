package com.angcyo.drawable

import android.graphics.Paint

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**文本的宽度*/
fun Paint.textWidth(text: String?): Float {
    return measureText(text ?: "")
}

/**文本的高度*/
fun Paint.textHeight(): Float {
    return descent() - ascent()
}