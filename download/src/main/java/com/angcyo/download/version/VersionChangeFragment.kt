package com.angcyo.download.version

import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.download.R
import com.angcyo.download.dslitem.DslVersionChangeItem
import com.angcyo.library.ex._string

/**
 * 版本更新记录界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/16
 */
open class VersionChangeFragment : BaseDslFragment() {

    companion object {
        var versionChangeList: List<VersionUpdateBean>? = null
    }

    init {
        fragmentTitle = _string(R.string.version_history_title)
        enableRefresh = true
    }

    override fun onLoadData() {
        super.onLoadData()
        loadVersionChangeList { list, error ->
            loadDataEnd(DslVersionChangeItem::class, list, error) { bean ->
                itemVersionBean = bean
            }
        }
    }

    /**加载版本更新记录*/
    open fun loadVersionChangeList(action: (list: List<VersionUpdateBean>?, error: Throwable?) -> Unit) {
        action(versionChangeList, null)
    }

}