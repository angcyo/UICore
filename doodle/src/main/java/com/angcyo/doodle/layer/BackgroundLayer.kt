package com.angcyo.doodle.layer

import com.angcyo.doodle.DoodleDelegate
import com.angcyo.doodle.element.BackgroundElement

/**
 * 背景层, 用来绘制指定颜色的背景或者透明背景
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BackgroundLayer(doodleDelegate: DoodleDelegate) : BaseLayer(doodleDelegate) {

    var backgroundElement: BackgroundElement? = null
        set(value) {
            val old = field
            field = value

            if (old != null) {
                elementList.remove(old)
            }
            if (value != null) {
                elementList.add(value)
            }
            doodleDelegate.refresh()
        }

    init {
        backgroundElement = BackgroundElement()
    }
}