package com.angcyo.item.style

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.annotation.ItemConfig
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.data.updateSingleData
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.DslImageItem
import com.angcyo.item.R
import com.angcyo.library.app
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.gone
import com.angcyo.library.ex.visible
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.library.model.mimeType
import com.angcyo.library.model.toLoaderMedia
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.*
import java.lang.ref.WeakReference

/**
 * 媒体图片数据显示Item, 不支持大图预览
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/04
 */
interface IGridMediaItem : IAutoInitItem {

    @ItemConfig
    var gridMediaItemConfig: GridMediaItemConfig

    @ItemInitEntryPoint
    fun initMediaItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.rv(gridMediaItemConfig.itemGridMediaRecyclerViewId)?.apply {
            if (gridMediaItemConfig.itemGridMediaList.isEmpty() && gridMediaItemConfig.itemGoneOnMediaEmpty) {
                gone()
            } else {
                visible()
                onBindGridMediaRecyclerView(this, itemHolder, itemPosition, adapterItem, payloads)
            }
        }
    }

    /**回收*/
    fun onGridMediaRecyclerViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        itemHolder.rv(gridMediaItemConfig.itemGridMediaRecyclerViewId)?.apply {
            layoutManager = null
            adapter = null
        }
    }

    /**绑定[RecyclerView]*/
    fun onBindGridMediaRecyclerView(
        recyclerView: RecyclerView,
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //列表
        recyclerView.apply {
            //优先清空[OnScrollListener]
            clearOnScrollListeners()
            clearItemDecoration()
            initDsl()

            if (layoutManager != gridMediaItemConfig.itemGridMediaLayoutManager) {
                layoutManager = gridMediaItemConfig.itemGridMediaLayoutManager
            }

            //关键地方, 如果每次都赋值[adapter], 系统会重置所有缓存.
            if (adapter != gridMediaItemConfig.itemGridMediaAdapter) {
                adapter = gridMediaItemConfig.itemGridMediaAdapter
            }

            //渲染数据
            if (adapter is DslAdapter) {
                val dslAdapter = adapter as DslAdapter
                dslAdapter._recyclerView = this
                onRenderGridMediaAdapter(dslAdapter)
            }
        }
    }

    /**渲染[DslAdapter]*/
    fun onRenderGridMediaAdapter(adapter: DslAdapter) {
        adapter.updateSingleData<DslImageItem>(gridMediaItemConfig.itemGridMediaList) { data ->
            val media = data as LoaderMedia
            onInitGridMediaItem(adapter, this, media)
        }

        /*if (this is DslAdapterItem && itemParentRef?.get() == null) {
            adapter.updateNow()
        } else {
            adapter.notifyDataChanged()
        }*/
    }

    /**初始化用于展示的[DslImageItem]*/
    fun onInitGridMediaItem(adapter: DslAdapter, item: DslAdapterItem, media: LoaderMedia) {
        val parentItem = this
        if (item is DslImageItem) {
            item.apply {
                itemLayoutId = R.layout.dsl_single_image_item
                margin(2 * dpi)

                itemData = media
                imageItemConfig.itemLoadUri = media.loadUri()
                itemMimeType = media.mimeType()
                itemMediaDuration = media.duration
                itemShowTip = false
                itemShowDeleteView = false
                itemShowCover = false

                if (parentItem is DslAdapterItem) {
                    if (itemParentRef?.get() == parentItem) {
                        //一样的对象
                    } else {
                        itemParentRef = WeakReference(parentItem)
                    }
                }
            }
        }
    }
}

/** [url] 支持http/file路径
 * 请注意先清空[itemGridMediaList]数据
 * */
fun IGridMediaItem.addGridMedia(url: String) {
    gridMediaItemConfig.itemGridMediaList.add(url.toLoaderMedia())
}

/** [GridLayoutManager] span Count */
var IGridMediaItem.gridMediaSpanCount: Int
    get() = (gridMediaItemConfig.itemGridMediaLayoutManager as? GridLayoutManager)?.spanCount ?: -1
    set(value) {
        gridMediaItemConfig.itemGridMediaLayoutManager = GridLayoutManagerWrap(app(), value).apply {
            recycleChildrenOnDetach = true
        }
    }

@ItemConfig
open class GridMediaItemConfig : IDslItemConfig {

    /**控件id*/
    var itemGridMediaRecyclerViewId: Int = R.id.lib_nested_recycler_view

    /**媒体控制器*/
    var itemGridMediaAdapter: DslAdapter = DslAdapter().apply {
        //关闭内部情感图状态
        dslAdapterStatusItem.itemEnable = false
    }

    /**布局管理,
     * 请注意使用属性:[recycleChildrenOnDetach]*/
    var itemGridMediaLayoutManager: RecyclerView.LayoutManager =
        GridLayoutManagerWrap(app(), 3).apply {
            recycleChildrenOnDetach = true
        }

    /**媒体数据*/
    var itemGridMediaList: MutableList<LoaderMedia> = mutableListOf()

    /**[itemGridMediaList]数据为空时, 是否隐藏[RecyclerView]*/
    var itemGoneOnMediaEmpty: Boolean = true

}