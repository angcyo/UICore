package com.angcyo.crop.ui

import com.angcyo.crop.CropDelegate
import com.angcyo.crop.CropOverlay
import com.angcyo.crop.CropView
import com.angcyo.crop.R
import com.angcyo.crop.ui.dslitem.CropIconItem
import com.angcyo.crop.ui.dslitem.CropRadioItem
import com.angcyo.dsladapter.drawRight
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.toColor
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * 剪切布局助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/22
 */
class CropLayoutHelper {

    /**裁剪控件*/
    var cropView: CropView? = null

    /**裁剪功能实现*/
    val cropDelegate: CropDelegate?
        get() = cropView?.cropDelegate

    @CallPoint
    fun initLayout(viewHolder: DslViewHolder) {
        cropView = viewHolder.v(R.id.lib_crop_view)
        cropDelegate?.overlay?.enableClipMoveMode = true //mode
        viewHolder.rv(R.id.crop_item_view)?.renderDslAdapter {
            //
            CropIconItem()() {
                itemIco = R.drawable.crop_rotate_icon
                itemText = "旋转"
                itemClick = {
                    cropDelegate?.updateRotate(cropDelegate!!.rotate + 90)
                }
            }
            CropIconItem()() {
                itemIco = R.drawable.crop_horizontal_flip_icon
                itemText = "水平翻转"
                itemClick = {
                    itemIsSelected = !itemIsSelected
                    cropDelegate?.flipHorizontal = itemIsSelected
                    cropDelegate?.refresh()
                    updateAdapterItem()
                }
            }
            CropIconItem()() {
                itemIco = R.drawable.crop_horizontal_flip_icon
                itemText = "垂直翻转"
                itemIcoRotate = 90f
                val offset = 10 * dpi
                drawRight(
                    1 * dpi,
                    offsetTop = offset,
                    offsetBottom = offset,
                    color = "#D9D9D9".toColor()
                )
                itemClick = {
                    itemIsSelected = !itemIsSelected
                    cropDelegate?.flipVertical = itemIsSelected
                    cropDelegate?.refresh()
                    updateAdapterItem()
                }
            }

            //
            CropRadioItem()() {
                itemIco = R.drawable.crop_ratio_free_icon
                itemText = "自由比例"
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()

                        cropDelegate?.overlay?.updateClipRatio()
                    }
                }
            }
            CropRadioItem()() {
                itemIco = R.drawable.crop_ratio_1_1_icon
                itemText = "1:1"
                itemClick = {
                    if (!itemIsSelected) {
                        itemIsSelected = true
                        updateAdapterItem()

                        cropDelegate?.overlay?.updateClipRatio(1f)
                    }
                }
            }

            //
            CropIconItem()() {
                itemIco = R.drawable.crop_type_circle_icon
                itemText = "圆形"
                itemClick = {
                    itemIsSelected = !itemIsSelected
                    updateAdapterItem()

                    if (itemIsSelected) {
                        cropDelegate?.overlay?.updateClipType(CropOverlay.TYPE_CIRCLE)
                    } else {
                        cropDelegate?.overlay?.updateClipType(CropOverlay.TYPE_ROUND)
                    }
                }
            }
        }
    }

}