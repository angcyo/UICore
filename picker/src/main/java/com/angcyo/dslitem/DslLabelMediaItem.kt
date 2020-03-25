package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.filter.AddMediaFilterAfterInterceptor
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

    /**最大显示媒体数*/
    var itemShowMaxMedia: Int = 9

    /**添加媒体过滤按钮*/
    var addMediaFilterAfterInterceptor = AddMediaFilterAfterInterceptor()
    var addMediaItem: DslAddMediaItem? = DslAddMediaItem()

    /**需要显示的媒体列表*/
    var itemMediaList = mutableListOf<LoaderMedia>()

    /**用于预览大图*/
    var itemFragment: Fragment? = null

    /**是否要显示加号*/
    var itemShowAddMediaItem: Boolean = false

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

        checkEmpty(itemHolder)

        itemHolder.rv(R.id.lib_recycler_view)?.apply {
            val recyclerView = this
            clearOnScrollListeners()
            clearItemDecoration()

            noItemAnim()
            initDsl()

            adapter = itemMediaAdapter

            itemMediaAdapter.apply {
                //+号处理
                dslDataFilter?.dataAfterInterceptorList?.apply {
                    addMediaFilterAfterInterceptor.maxMediaCount = itemShowMaxMedia

                    addMediaFilterAfterInterceptor.addMediaDslAdapterItem =
                        if (itemShowAddMediaItem && itemEnable) {
                            addMediaItem?.apply {
                                itemFragment = this@DslLabelMediaItem.itemFragment
                                itemLoaderConfig.maxSelectorLimit = itemShowMaxMedia
                                itemLoaderConfig.selectorMediaList = itemMediaList
                                itemResult = {
                                    //媒体列表
                                    it?.apply {
                                        itemMediaList.clear()
                                        itemMediaList.addAll(this)
                                        loadMediaList(
                                            itemHolder,
                                            recyclerView,
                                            itemMediaAdapter,
                                            it
                                        )
                                        this@DslLabelMediaItem.itemChanging = true
                                    }
                                }
                            }
                        } else {
                            null
                        }

                    if (!contains(addMediaFilterAfterInterceptor)) {
                        add(0, addMediaFilterAfterInterceptor)
                    }
                }

                //媒体列表
                loadMediaList(itemHolder, recyclerView, this, itemMediaList)
            }
        }
    }

    open fun checkEmpty(itemHolder: DslViewHolder) {
        if (itemMediaList.isEmpty() && !itemShowAddMediaItem) {
            itemHolder.visible(R.id.lib_tip_text_view)
            itemHolder.gone(R.id.lib_recycler_view)
        } else {
            itemHolder.visible(R.id.lib_recycler_view)
            itemHolder.gone(R.id.lib_tip_text_view)
        }
    }

    open fun loadMediaList(
        itemHolder: DslViewHolder,
        recyclerView: RecyclerView,
        adapter: DslAdapter,
        mediaList: List<LoaderMedia>
    ) {

        adapter.clearItems()
        mediaList.forEach { media ->
            adapter + DslImageItem().apply {
                itemData = media
                itemLoadUri = media.loadUri()
                itemMimeType = media.mimeType()
                itemMediaDuration = media.duration
                itemShowTip = false

                itemShowDeleteView = itemShowAddMediaItem
                itemDeleteClick = {
                    itemMediaList.remove(media)
                    adapter.changeDataItems {
                        it.remove(this)
                    }
                    checkEmpty(itemHolder)
                    this@DslLabelMediaItem.itemChanging = true
                }

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