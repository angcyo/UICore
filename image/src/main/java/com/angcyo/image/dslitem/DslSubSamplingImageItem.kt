package com.angcyo.image.dslitem

import android.graphics.Bitmap
import android.net.Uri
import com.angcyo.download.dslitem.DslBaseDownloadItem
import com.angcyo.download.isSucceed
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.dsladapter.notNull
import com.angcyo.image.R
import com.angcyo.image.ex._subSampling
import com.angcyo.image.widget.loadImage
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import java.io.File

/**
 * 支持大图加载
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */
open class DslSubSamplingImageItem : DslBaseDownloadItem(),
    SubsamplingScaleImageView.OnImageEventListener {

    /**媒体uri*/
    open var itemLoadUri: Uri? = null

    /**占位图获取*/
    var drawableProvider: IDrawableProvider? = null

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

        //listener
        itemHolder._subSampling(R.id.lib_image_view)?.setOnImageEventListener(this)

        itemLoadUri?.also { uri ->

            //预览/占位图
            if (isDownloadFinish()) {
                itemHolder.gone(R.id.lib_preview_view)
            } else {
                itemHolder.visible(R.id.lib_preview_view)
                itemHolder.img(R.id.lib_preview_view)
                    ?.setImageDrawable(drawableProvider?.getPlaceholderDrawable(uri))
            }

            //大图/长图 先下载后加载
            when {
                uri.isHttpScheme() -> download(itemHolder, uri.loadUrl())
                uri.isFileScheme() -> loadSubSampling(itemHolder, uri)
                else -> itemHolder.gone(R.id.lib_transition_overlay_view)
            }
        }
    }

    //region ---download---

    override fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        super.onDownloadStart(itemHolder, task)
        itemHolder?.visible(R.id.lib_transition_overlay_view)
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

    //endregion ---download---

    //region ---load---

    /**加载图片[Bitmap]*/
    open fun loadSubSampling(itemHolder: DslViewHolder?, bitmap: Bitmap?) {
        itemHolder?._subSampling(R.id.lib_image_view)?.apply {
            bitmap?.let {
                setImage(ImageSource.bitmap(bitmap))
            }
        }
    }

    open fun loadSubSampling(itemHolder: DslViewHolder?, path: String?) {
        notNull(itemHolder, path) {
            loadSubSampling(itemHolder, fileUri(app(), File(path!!)))
        }
    }

    open fun loadSubSampling(itemHolder: DslViewHolder?, file: File?) {
        notNull(itemHolder, file) {
            loadSubSampling(itemHolder, fileUri(app(), file!!))
        }
    }

    open fun loadSubSampling(itemHolder: DslViewHolder?, uri: Uri?) {
        //加载完成之后再隐藏
        itemHolder?.view(R.id.lib_transition_overlay_view)?.apply {
            animate().alpha(0f).setDuration(1000).withEndAction {
                alpha = 1f
                gone()
            }.start()
        }
        notNull(itemHolder, uri) {
            itemHolder?._subSampling(R.id.lib_image_view)?.loadImage(uri!!)
        }
    }

    //endregion ---load---

    //region ---OnImageEventListener---

    override fun onReady() {
        L.d("SubsamplingScaleImageView onReady")
    }

    override fun onImageLoaded() {
        //图片加载成功
        if (itemDslAdapter?._recyclerView != null) {
            itemViewHolder()?.view(R.id.lib_transition_overlay_view)?.apply {
                gone()
            }
        }
    }

    override fun onPreviewLoadError(e: java.lang.Exception?) {
        e?.printStackTrace()
    }

    override fun onImageLoadError(e: java.lang.Exception?) {
        e?.printStackTrace()
    }

    override fun onTileLoadError(e: java.lang.Exception?) {
        e?.printStackTrace()
    }

    override fun onPreviewReleased() {

    }

    //endregion ---OnImageEventListener---
}