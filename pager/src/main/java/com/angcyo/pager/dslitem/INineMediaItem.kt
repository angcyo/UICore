package com.angcyo.pager.dslitem

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IFragmentItem
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.item.DslImageItem
import com.angcyo.item.style.IGridMediaItem
import com.angcyo.library.L
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.size
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.pager.dslPager
import com.angcyo.widget.DslViewHolder

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
        super.onBindGridMediaRecyclerView(
            recyclerView,
            itemHolder,
            itemPosition,
            adapterItem,
            payloads
        )
        if (gridMediaItemConfig.itemGridMediaList.size() == 1) {
            //单图, 使用线性布局
            if (recyclerView.layoutManager != oneMediaLayoutManager) {
                recyclerView.layoutManager = oneMediaLayoutManager
            }
        } else {
            if (recyclerView.layoutManager != gridMediaItemConfig.itemGridMediaLayoutManager) {
                recyclerView.layoutManager = gridMediaItemConfig.itemGridMediaLayoutManager
            }
        }
    }

    override fun onRenderGridMediaAdapter(adapter: DslAdapter) {
        super.onRenderGridMediaAdapter(adapter)
    }

    override fun onInitGridMediaItem(
        adapter: DslAdapter,
        item: DslAdapterItem,
        media: LoaderMedia
    ) {
        super.onInitGridMediaItem(adapter, item, media)

        if (gridMediaItemConfig.itemGridMediaList.size() == 1) {
            //单图, 使用实际宽高
            onParseMediaSize(media)

            item.itemWidth = -2
            item.itemHeight = -2

            if (item is DslImageItem) {
                item.imageItemConfig.imageStyleConfig.apply {
                    viewMinWidth = 10 * dpi
                    viewMinHeight = 10 * dpi
                    viewWidth = media.width
                    viewHeight = media.height
                    imageScaleType = ImageView.ScaleType.FIT_CENTER
                }
            }
        } else {
            item.itemWidth = -1
            item.itemHeight = -2

            if (item is DslImageItem) {
                item.imageItemConfig.imageStyleConfig.apply {
                    viewWidth = -1
                    viewHeight = 0
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
    fun onParseMediaSize(media: LoaderMedia) {
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
}