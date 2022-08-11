package com.angcyo.doodle.data

import android.graphics.Path

/**
 * [ZenPathBrush]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/11
 */
class BrushPath : Path() {

    /**当前路径, 需要使用的描边宽度*/
    var strokeWidth: Float = -1f

    override fun set(src: Path) {
        super.set(src)
    }

}