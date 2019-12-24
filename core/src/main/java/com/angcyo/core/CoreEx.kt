package com.angcyo.core

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.angcyo.base.getDimen
import com.angcyo.library.app

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
@ColorInt
fun getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(app(), id)
}

@Px
fun getDimen(@DimenRes id: Int): Int {
    return app().getDimen(id)
}
