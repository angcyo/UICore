package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.item.DslBaseLabelItem
import com.angcyo.item.DslImageItem
import com.angcyo.library.L
import com.angcyo.library.ex.elseNull
import com.angcyo.loader.LoaderMedia
import com.angcyo.loader.loadUri
import com.angcyo.loader.mimeType
import com.angcyo.pager.dslPager
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.clearItemDecoration
import com.angcyo.widget.recycler.initDsl
import com.angcyo.widget.recycler.noItemAnim

/**
 * 带label, 媒体列表展示的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelMediaItem : DslBaseLabelItem() {

    /**内部adapter*/
    var itemMediaAdapter: DslAdapter = DslAdapter()

    /**需要显示的媒体列表*/
    var itemMediaList = mutableListOf<LoaderMedia>()

    /**用于预览大图*/
    var itemFragment: Fragment? = null

    init {
        itemLayoutId = R.layout.dsl_label_media_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.visible(R.id.lib_tip_text_view, itemMediaList.isEmpty())

        itemHolder.rv(R.id.lib_recycler_view)?.apply {
            val recyclerView = this
            clearOnScrollListeners()
            clearItemDecoration()

            noItemAnim()
            initDsl()

            adapter = itemMediaAdapter

            itemMediaAdapter.apply {
                clearItems()
                itemMediaList.forEach { media ->
                    DslImageItem()() {
                        itemData = media
                        itemLoadUri = media.loadUri()
                        itemMimeType = media.mimeType()
                        itemMediaDuration = media.duration
                        itemShowTip = false

                        //大图预览
                        itemClick = {
                            itemFragment?.dslPager {
                                fromRecyclerView = recyclerView
                                startPosition = itemIndexPosition()
                                loaderMediaList = itemMediaList
                            }.elseNull {
                                L.w("itemFragment is null, cannot show pager.")
                            }
                        }
                    }
                }
            }
        }
    }
}