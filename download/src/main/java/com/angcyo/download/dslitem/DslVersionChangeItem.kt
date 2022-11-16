package com.angcyo.download.dslitem

import com.angcyo.download.R
import com.angcyo.download.version.VersionUpdateBean
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.openUrl
import com.angcyo.widget.DslViewHolder

/**
 * 版本更新记录item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/16
 */
class DslVersionChangeItem : DslAdapterItem() {

    /**数据*/
    var itemVersionBean: VersionUpdateBean? = null

    init {
        itemLayoutId = R.layout.dsl_version_change_item

        itemClick = {
            if (itemVersionBean?.link == true) {
                itemVersionBean?.versionUrl?.let { url ->
                    if (url.isHttpScheme()) {
                        it.context.openUrl(url)
                    }
                }
            }
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.version_date_view)?.text = buildString {
            append(itemVersionBean?.versionDate ?: "")
            //appendLine()
            itemVersionBean?.versionName?.let {
                append("  V$it")
            }
        }

        itemHolder.tv(R.id.version_des_view)?.text = itemVersionBean?.versionDes
    }
}