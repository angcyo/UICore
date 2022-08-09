package com.angcyo.pager.dslitem

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.item.DslImageItem
import com.angcyo.item.style.GridMediaItemConfig
import com.angcyo.item.style.IGridMediaItem
import com.angcyo.library.L
import com.angcyo.library.ex.*
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.pager.dslPager
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.clearItemDecoration
import com.angcyo.widget.recycler.initDsl

/**
 * 九宫格图片显示item
 *
 * 如果只有1张图片, 则显示实际尺寸/或者等比缩放.
 *
 * 多张图时, 则使用网格显示
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/04
 */
interface INineMediaItem : IGridMediaItem, IFragmentItem {

    /**一张图片时布局管理*/
    var oneMediaLayoutManager: RecyclerView.LayoutManager

    override fun initMediaItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.initMediaItem(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun onBindGridMediaRecyclerView(
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

            if (gridMediaItemConfig.itemGridMediaList.size() == 1) {
                //单图, 使用线性布局
                if (recyclerView.layoutManager != oneMediaLayoutManager) {
                    recyclerView.layoutManager = oneMediaLayoutManager
                }
                recyclerView.setWidthHeight(-2, -2)
            } else {
                if (recyclerView.layoutManager != gridMediaItemConfig.itemGridMediaLayoutManager) {
                    recyclerView.layoutManager = gridMediaItemConfig.itemGridMediaLayoutManager
                }
                recyclerView.setWidthHeight(-1, -2)
            }

            //关键地方, 如果每次都赋值[adapter], 系统会重置所有缓存.
            if (adapter != gridMediaItemConfig.itemGridMediaAdapter) {
                adapter = gridMediaItemConfig.itemGridMediaAdapter
            }

            //渲染数据
            if (adapter is DslAdapter) {
                val dslAdapter = adapter as DslAdapter
                dslAdapter._recyclerView = this
                onRenderGridMediaAdapter(recyclerView, dslAdapter)
            }
        }
    }

    override fun onRenderGridMediaAdapter(recyclerView: RecyclerView, adapter: DslAdapter) {
        super.onRenderGridMediaAdapter(recyclerView, adapter)
    }

    override fun onInitGridMediaItem(
        recyclerView: RecyclerView,
        adapter: DslAdapter,
        item: DslAdapterItem,
        media: LoaderMedia
    ) {
        super.onInitGridMediaItem(recyclerView, adapter, item, media)

        if (gridMediaItemConfig.itemGridMediaList.size() == 1) {
            //单图, 使用实际宽高
            onParseNineMediaSize(media)

            //图片item
            item.itemWidth = -2
            item.itemHeight = -2

            if (item is DslImageItem) {
                item.imageItemConfig.imageStyleConfig.apply {
                    viewMinWidth = (gridMediaItemConfig as? NineMediaItemConfig)?.itemMediaMinWidth
                        ?: (10 * dpi)
                    viewMinHeight =
                        (gridMediaItemConfig as? NineMediaItemConfig)?.itemMediaMinHeight
                            ?: (10 * dpi)
                    viewWidth = media.width
                    viewHeight = media.height
                    viewDimensionRatio = null
                    imageScaleType = ImageView.ScaleType.FIT_CENTER
                }
            }
        } else {
            //网格布局, 图片item
            item.itemWidth = -1
            item.itemHeight = -2

            if (item is DslImageItem) {
                item.imageItemConfig.imageStyleConfig.apply {
                    viewMinWidth = undefined_size
                    viewMinHeight = undefined_size
                    viewWidth = -1
                    viewHeight = 0
                    viewDimensionRatio = "1:1"
                    imageScaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }

        //大图预览
        if (item is DslImageItem) {
            item.apply {
                itemClick = {
                    itemFragment?.dslPager {
                        fromRecyclerView = adapter._recyclerView
                        startPosition = itemIndexPosition()
                        loaderMediaList = gridMediaItemConfig.itemGridMediaList
                    }.elseNull {
                        L.w("itemFragment is null, cannot show pager.")
                    }
                }
            }
        }
    }

    /**根据图片url, 获取图片的宽高
     * 格式1: https://xxx.png?w=346&h=362&
     * 格式2: https://xxx.png?s=346x362&
     * */
    fun onParseNineMediaSize(media: LoaderMedia) {
        (gridMediaItemConfig as? NineMediaItemConfig)?.apply {
            //解析大小
            itemParseMediaSizeAction.invoke(media)
            //限制大小
            itemMediaSizeLimitAction.invoke(media)
        }
    }
}

class NineMediaItemConfig : GridMediaItemConfig() {

    /**显示单图时的最大宽度, 如果超过了, 定比缩放*/
    var itemMediaMaxWidth: Int = 300 * dpi

    var itemMediaMaxHeight: Int = 400 * dpi

    /**当未指定宽高时, 最小的宽高*/
    var itemMediaMinWidth: Int = 100 * dpi

    var itemMediaMinHeight: Int = 100 * dpi

    /**解析媒体的宽高action*/
    var itemParseMediaSizeAction: (media: LoaderMedia) -> Unit = { media ->
        val uri = media.loadUri()
        uri?.apply {
            try {
                if (media.width == 0 || media.height == 0) {
                    media.width = -2
                    media.height = -2

                    val w = getQueryParameter("w")
                    val h = getQueryParameter("h")

                    if (w.isNullOrEmpty() && h.isNullOrEmpty()) {
                        val s = getQueryParameter("s")
                        if (s.isNullOrEmpty().not()) {
                            s?.split("x")?.apply {
                                media.width = getOrNull(0)?.toIntOrNull() ?: -2
                                media.height = getOrNull(1)?.toIntOrNull() ?: -2
                            }
                        }
                    } else {
                        media.width = w?.toIntOrNull() ?: -2
                        media.height = h?.toIntOrNull() ?: -2
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**宽高限制action*/
    var itemMediaSizeLimitAction: (media: LoaderMedia) -> Unit = { media ->
        media.isLargerBitmap = media.isLargerBitmap || (media.width > 3000 || media.height > 3000)
        if (itemMediaMaxWidth > 0 && media.width > itemMediaMaxWidth) {
            //宽度超限
            val old = media.width
            media.width = itemMediaMaxWidth
            media.height = media.height * itemMediaMaxWidth / old
        }
        if (itemMediaMaxHeight > 0 && media.height > itemMediaMaxHeight) {
            //高度超限
            val old = media.height
            media.height = itemMediaMaxHeight
            media.width = media.width * itemMediaMaxHeight / old
        }
    }

}