package com.angcyo.item

import androidx.annotation.LayoutRes
import androidx.core.view.isNotEmpty
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.inflate
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder

/**
 * 显示在RV最下面的item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/11/03
 *
 * - [RecyclerBottomLayout]
 */
open class DslLastWrapItem : DslAdapterItem() {

    /**真实的布局信息*/
    @LayoutRes
    var itemContentLayoutId: Int = undefined_res

    init {
        itemLayoutId = R.layout.lib_item_last_wrap
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun _initItemConfig(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //内容布局
        if (itemContentLayoutId != undefined_res) {
            var inflateLayoutId = undefined_res //已经inflate的布局id
            itemHolder.group(R.id.lib_item_root_layout)?.apply {
                if (isNotEmpty()) {
                    inflateLayoutId = (getChildAt(0).getTag(R.id.tag) as? Int) ?: undefined_res
                }

                if (itemContentLayoutId != inflateLayoutId) {
                    //两次inflate的布局不同
                    itemHolder.clear()
                    inflate(itemContentLayoutId, true)
                    val view = getChildAt(0)
                    view.setTag(R.id.tag, itemContentLayoutId)
                }
            }
        } else {
            itemHolder.group(R.id.lib_item_root_layout)?.removeAllViews()
        }
        super._initItemConfig(itemHolder, itemPosition, adapterItem, payloads)
    }
}