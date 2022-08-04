package com.angcyo.pager.dslitem

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.item.DslGridMediaItem
import com.angcyo.item.style.GridMediaItemConfig
import com.angcyo.library.app
import com.angcyo.widget.recycler.LinearLayoutManagerWrap

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/04
 */
open class DslNineMediaItem : DslGridMediaItem(), INineMediaItem {

    override var itemFragment: Fragment? = null

    override var oneMediaLayoutManager: RecyclerView.LayoutManager =
        LinearLayoutManagerWrap(app(), RecyclerView.VERTICAL)

    override var gridMediaItemConfig: GridMediaItemConfig = NineMediaItemConfig()

}