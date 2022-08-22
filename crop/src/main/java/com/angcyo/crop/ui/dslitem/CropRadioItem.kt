package com.angcyo.crop.ui.dslitem

/**
 * 裁剪比例切换互斥item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/22
 */
class CropRadioItem : CropIconItem() {

    init {
        itemSingleSelectMutex = true
    }
}