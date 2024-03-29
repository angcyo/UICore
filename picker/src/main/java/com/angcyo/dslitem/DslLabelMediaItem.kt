package com.angcyo.dslitem

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.data.updateSingleData
import com.angcyo.dsladapter.filter.AddMediaFilterAfterInterceptor
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.dsladapter.margin
import com.angcyo.item.DslBaseLabelItem
import com.angcyo.item.DslImageItem
import com.angcyo.library.L
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.elseNull
import com.angcyo.library.model.*
import com.angcyo.loader.LoaderConfig
import com.angcyo.pager.dslPager
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.*

/**
 * 带label, 媒体列表展示的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelMediaItem : DslBaseLabelItem(), IFragmentItem {

    /**内部adapter*/
    var itemMediaAdapter: DslAdapter = DslAdapter()

    /**最大显示媒体数*/
    var itemShowMaxMediaCount: Int = 9

    /**添加媒体过滤按钮*/
    var addMediaFilterAfterInterceptor = AddMediaFilterAfterInterceptor()

    /**添加媒体, 加号逻辑*/
    var addMediaItem: DslAddMediaItem? = DslAddMediaItem().apply {
        margin(1 * dpi)

        //视频时长, 秒
        itemMinRecordTime
        itemMaxRecordTime
    }

    /**媒体Loader配置*/
    var itemUpdateLoaderConfig: (LoaderConfig) -> Unit = {
        addMediaItem?.apply {
            pickerMediaItemConfig.itemPickerMediaList.clear()
            pickerMediaItemConfig.itemPickerMediaList.addAll(itemMediaList)
        }
    }

    /**配置[DslRecyclerView]*/
    var itemRecyclerConfig: (RecyclerView) -> Unit = {
        it.noItemChangeAnim()
    }

    /**需要显示的媒体列表*/
    var itemMediaList = mutableListOf<LoaderMedia>()

    /**用于预览大图*/
    override var itemFragment: Fragment? = null

    /**是否要显示加号*/
    var itemShowAddMediaItem: Boolean = false

    /**网格列数*/
    var itemGridSpanCount = 3

    /**是否需要显示空布局, 如果数据为空时*/
    var itemShowEmptyLayout: Boolean = true

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

            initDsl()

            resetLayoutManager("GV$itemGridSpanCount")

            //关键地方, 如果每次都赋值[adapter], 系统会重置所有缓存.
            if (adapter != itemMediaAdapter) {
                adapter = itemMediaAdapter
            }

            itemMediaAdapter.apply {
                //关闭默认的状态切换
                dslAdapterStatusItem.itemEnable = !itemShowAddMediaItem

                //+号处理
                dslDataFilter?.dataAfterInterceptorList?.apply {
                    addMediaFilterAfterInterceptor.maxMediaCount = itemShowMaxMediaCount

                    addMediaFilterAfterInterceptor.addMediaDslAdapterItem =
                        if (itemShowAddMediaItem && itemEnable) {
                            //如果需要显示添加媒体的item
                            addMediaItem?.apply {
                                itemFragment = this@DslLabelMediaItem.itemFragment

                                pickerMediaItemConfig.itemLoaderConfig.maxSelectorLimit =
                                    itemShowMaxMediaCount
                                pickerMediaItemConfig.itemLoaderConfig.selectorMediaList =
                                    itemMediaList
                                pickerMediaItemConfig.itemUpdateLoaderConfig =
                                    this@DslLabelMediaItem.itemUpdateLoaderConfig

                                pickerMediaItemConfig.itemTakeResult = {
                                    it?.apply {
                                        itemMediaList.add(this)
                                        loadMediaList(
                                            itemHolder,
                                            recyclerView,
                                            itemMediaAdapter,
                                            itemMediaList
                                        )
                                        this@DslLabelMediaItem.itemChanging = true
                                    }
                                }

                                pickerMediaItemConfig.itemPickerResult = {
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
                            //不需要添加媒体item
                            null
                        }

                    //使用过滤的方式追加item
                    if (!contains(addMediaFilterAfterInterceptor)) {
                        add(0, addMediaFilterAfterInterceptor)
                    }
                }

                //config
                itemRecyclerConfig(recyclerView)

                //媒体列表
                loadMediaList(itemHolder, recyclerView, this, itemMediaList)
            }
        }
    }

    open fun checkEmpty(itemHolder: DslViewHolder) {
        if (itemMediaList.isEmpty() && !itemShowAddMediaItem) {
            itemHolder.visible(R.id.lib_tip_text_view, itemShowEmptyLayout)
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
        adapter.updateSingleData<DslImageItem>(mediaList) { data ->
            val media = data as LoaderMedia

            margin(2 * dpi)

            itemData = media
            imageItemConfig.itemLoadUri = media.loadUri()
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

        /*if (itemParentRef?.get() == null) {
            adapter.updateNow()
        } else {
            adapter.notifyDataChanged()
        }*/
    }

    /**获取媒体路径列表*/
    open fun getMediaPathList(): List<String> = itemMediaList.toUrlList()

    /** [url] 支持http/file路径 */
    open fun addMediaUrl(url: String) {
        itemMediaList.add(url.toLoaderMedia())
    }
}