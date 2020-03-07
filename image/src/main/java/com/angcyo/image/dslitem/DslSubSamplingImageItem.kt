package com.angcyo.image.dslitem

import android.net.Uri
import com.angcyo.download.dslitem.DslBaseDownloadItem
import com.angcyo.download.isSucceed
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.notNull
import com.angcyo.image.R
import com.angcyo.image.ex._subSampling
import com.angcyo.image.widget.loadImage
import com.angcyo.library.app
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.isFileScheme
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.loadUrl
import com.angcyo.widget.DslViewHolder
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */
open class DslSubSamplingImageItem : DslBaseDownloadItem() {

    /**媒体uri*/
    open var itemLoadUri: Uri? = null

    init {
        itemLayoutId = R.layout.dsl_sub_sampling_image_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemLoadUri?.also { uri ->
            when {
                uri.isHttpScheme() -> download(itemHolder, uri.loadUrl())
                uri.isFileScheme() -> loadSubSampling(itemHolder, uri)
                else -> itemHolder.gone(R.id.lib_loading_view)
            }
        }
    }

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        itemHolder?.visible(R.id.lib_loading_view)
    }

    override fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        super.onDownloadFinish(itemHolder, task, cause, error)
        if (cause.isSucceed()) {
            loadSubSampling(itemHolder, task.file)
        }
    }

    open fun loadSubSampling(itemHolder: DslViewHolder?, file: File?) {
        notNull(itemHolder, file) {
            loadSubSampling(itemHolder, fileUri(app(), file!!))
        }
    }

    open fun loadSubSampling(itemHolder: DslViewHolder?, uri: Uri?) {
        itemHolder?.gone(R.id.lib_loading_view)
        notNull(itemHolder, uri) {
            itemHolder?._subSampling(R.id.lib_image_view)?.loadImage(uri!!)
        }
    }
}