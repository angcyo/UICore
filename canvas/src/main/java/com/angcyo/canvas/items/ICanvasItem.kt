package com.angcyo.canvas.items

import android.graphics.drawable.Drawable

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/10
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ICanvasItem {

    /**获取在图层中的名字*/
    var itemName: CharSequence?

    /**获取在图层中预览的图形*/
    var itemDrawable: Drawable?

}