package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.data.LabelDesData
import com.angcyo.library.annotation.DSL
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.dslViewHolder
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.flow

/**
 * 存放一堆 [label:des] 描述信息的item
 * [com.angcyo.item.DslSolidTagItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/10
 */
open class DslTagGroupItem : DslAdapterItem() {

    /**数据存放*/
    var itemLabelDesList: List<LabelDesData>? = null

    /**填充的布局id*/
    var itemTagLayoutId = R.layout.dsl_solid_tag_item

    init {
        itemLayoutId = R.layout.dsl_tag_group_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        initLabelDesList()
        itemHolder.flow(R.id.lib_flow_layout)
            ?.resetChild(itemLabelDesList, itemTagLayoutId) { itemView, item, itemIndex ->
                val viewHolder = itemView.dslViewHolder()
                viewHolder.tv(R.id.lib_label_view)?.text = item.label
                viewHolder.tv(R.id.lib_des_view)?.text = item.des
            }
    }

    /**初始化数据*/
    open fun initLabelDesList() {
    }

    @DSL
    fun renderLabelDesList(action: MutableList<LabelDesData>.() -> Unit) {
        val list = mutableListOf<LabelDesData>()
        list.action()
        itemLabelDesList = list
    }

    /**创建一个[LabelDesData]*/
    fun labelDes(label: Any?, des: Any?) = LabelDesData("$label", "$des")

    /**在label后面加:号*/
    fun formatLabelDes(label: CharSequence?, des: CharSequence?) = LabelDesData("${label}:", des)
}