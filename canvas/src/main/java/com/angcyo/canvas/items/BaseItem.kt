package com.angcyo.canvas.items

import android.graphics.Color
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.dp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
abstract class BaseItem : ICanvasItem {

    var paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }

}