package com.angcyo.widget.base

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
@ColorInt
fun String.toColorInt(): Int = Color.parseColor(this)
