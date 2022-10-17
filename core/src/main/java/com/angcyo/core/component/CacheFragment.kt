package com.angcyo.core.component

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.core.component.model.CacheModel
import com.angcyo.core.dslitem.DslCacheModelItem
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.core.vmApp
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.updateItem

/**
 * 缓存管理的界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/17
 */
class CacheFragment : BaseDslFragment() {

    val cacheModel = vmApp<CacheModel>()

    init {
        fragmentTitle = "缓存管理"
    }

    override fun onInitDslLayout(recyclerView: RecyclerView, dslAdapter: DslAdapter) {
        super.onInitDslLayout(recyclerView, dslAdapter)
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        loadDataEnd(DslCacheModelItem::class, cacheModel.cacheInfoListData.value) { bean ->
            itemCacheInfo = bean
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cacheModel.cacheSizeOnceData.observe { cacheInfo ->
            cacheInfo?.let {
                _adapter.updateItem {
                    it is DslCacheModelItem && it.itemCacheInfo == cacheInfo
                }
            }
        }

        cacheModel.computeCacheSize()
    }

}