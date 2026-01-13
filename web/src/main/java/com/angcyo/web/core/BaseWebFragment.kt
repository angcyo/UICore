package com.angcyo.web.core

import android.app.Dialog
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.component.fileSelector
import com.angcyo.core.fragment.BaseTitleFragment
import com.angcyo.dialog.configBottomDialog
import com.angcyo.dialog.dslDialog
import com.angcyo.download.download
import com.angcyo.download.downloadNotify
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.renderItemList
import com.angcyo.image.dslitem.DslSubSamplingImageItem
import com.angcyo.library.L
import com.angcyo.library.component.DslIntent
import com.angcyo.library.component.Web
import com.angcyo.library.component.dslIntentShare
import com.angcyo.library.ex._string
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.copy
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.ext
import com.angcyo.library.ex.file
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.find
import com.angcyo.library.ex.inflate
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isFileScheme
import com.angcyo.library.ex.isHttpMimeType
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.isImageMimeType
import com.angcyo.library.ex.isTextMimeType
import com.angcyo.library.ex.isVideoMimeType
import com.angcyo.library.ex.loadDrawable
import com.angcyo.library.ex.loadUrl
import com.angcyo.library.ex.mimeType
import com.angcyo.library.ex.setWidth
import com.angcyo.library.ex.toUri
import com.angcyo.library.ex.urlIntent
import com.angcyo.library.model.AppBean
import com.angcyo.library.model.FileChooserParam
import com.angcyo.library.model.WebConfig
import com.angcyo.library.model.loadUri
import com.angcyo.library.toastQQ
import com.angcyo.loader.singleImage
import com.angcyo.loader.singleVideo
import com.angcyo.media.dslitem.DslTextureVideoItem
import com.angcyo.picker.dslPicker
import com.angcyo.tablayout.screenWidth
import com.angcyo.web.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.bar
import com.angcyo.widget.base.appendDslItem
import com.angcyo.widget.base.setSingleLineMode
import com.angcyo.widget.base.updateMarginParams
import com.angcyo.widget.span.span

/**
 * WebView基类
 * [com.angcyo.web.WebFragment]
 * [com.angcyo.tbs.core.TbsWebFragment]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/08
 */
abstract class BaseWebFragment : BaseTitleFragment() {

    /**Web的一些配置项*/
    var webConfig = WebConfig()

    /**动态内容的容器*/
    val wrapLayout: ViewGroup?
        get() = _vh.group(R.id.lib_wrap_layout)

    init {
        contentLayoutId = R.layout.layout_web_content
        contentOverlayLayoutId = R.layout.layout_web_content_overlay
        fragmentTitle = null
        enableTitleBarHideBehavior = webConfig.enableTitleBarHideBehavior
    }

    //region ---基础页面---

    override fun onCreateBackItem(): View? {
        return super.onCreateBackItem()?.apply {
            find<TextView>(R.id.lib_text_view)?.run {
                text = span {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(R.drawable.lib_back)
                                .colorFilter(fragmentConfig.titleItemIconColor)
                    }
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (canGoBack()) {
            goBack()
            checkCloseView()
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getParcelable<WebConfig>(WebConfig.KEY_CONFIG)?.run {
            webConfig = this
            if (title != null) {
                fragmentTitle = title
            }
            this@BaseWebFragment.enableTitleBarHideBehavior = enableTitleBarHideBehavior
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //开启硬件加速
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }

    override fun onCreateViewAfter(savedInstanceState: Bundle?) {
        super.onCreateViewAfter(savedInstanceState)
        initWebLayout(fromInitialize = true)
    }

    /**核心初始化*/
    open fun initWebLayout(fromInitialize: Boolean = true) {
        _vh.tv(R.id.lib_title_text_view)?.run {
            setWidth(width = screenWidth - 180 * dpi)
            setSingleLineMode()
        }

        val uri = webConfig.uri
        val data = webConfig.data

        if (data != null) {
            //加载数据
            attachWebView(null, data, fromInitialize = fromInitialize)
        } else if (uri == null) {
            if (fromInitialize) {
                toastQQ("Uri error", fContext(), R.drawable.lib_ic_error)
            }
        } else if (wrapLayout == null) {
            if (fromInitialize) {
                toastQQ("Layout error", fContext(), R.drawable.lib_ic_error)
            }
        } else {
            val loadUrl = uri.loadUrl()
            val mimeType = loadUrl.mimeType() ?: webConfig.mimeType

            L.d("Web:$uri $loadUrl $mimeType")

            if (mimeType.isVideoMimeType()) {
                try {
                    attachVideoView(uri, fromInitialize = fromInitialize)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (uri.isHttpScheme() || mimeType.isHttpMimeType()) {
                //打开网页
                loadWebTitleLayout()
                attachWebView(loadUrl, fromInitialize = fromInitialize)
            } else if (uri.isFileScheme()) {
                val fileExt = loadUrl!!.ext()
                if (webConfig.autoUpdateTitle && webConfig.title.isNullOrEmpty()) {
                    fragmentTitle = loadUrl.file().name
                }

                if (mimeType.isImageMimeType()) {
                    try {
                        attachImageView(uri, fromInitialize = fromInitialize)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (attachFileReaderView(loadUrl, fromInitialize = fromInitialize)) {
                        //支持文件打开
                    } else {
                        showLoadingView("无法打开文件\n$uri")
                    }
                }
            } else if (mimeType.isTextMimeType()) {
                attachTextView(uri, fromInitialize = fromInitialize)
            } else {
                //其他类型
                showLoadingView("不支持的类型\n$uri")
            }
        }
    }

    //region ---根据不同的类型, 填充不同的布局---

    /**加载其他文件, office等*/
    open fun attachFileReaderView(
        path: String,
        parent: ViewGroup? = wrapLayout,
        fromInitialize: Boolean = true
    ): Boolean = false

    var _dslVideoHolder: DslViewHolder? = null
    var _dslVideoItem: DslTextureVideoItem? = null

    /**加载视频*/
    open fun attachVideoView(
        uri: Uri,
        parent: ViewGroup? = wrapLayout,
        fromInitialize: Boolean = true
    ) {
        parent?.setBackgroundColor(Color.BLACK)
        hideLoadingView()

        val dslVideoItem = DslTextureVideoItem().apply {
            _dslVideoItem = this

            itemData = uri
            itemVideoUri = uri

            itemDownloadStart = { itemHolder, task ->
                onDownloadStart(itemHolder, task)
                showLoadingView("下载中...")
            }

            itemDownloadFinish = { itemHolder, task, cause, error ->
                onDownloadFinish(itemHolder, task, cause, error)
                hideLoadingView()
            }
        }
        _dslVideoHolder = parent?.appendDslItem(dslVideoItem)
    }

    var _dslSubSamplingItem: DslSubSamplingImageItem? = null

    /**加载大图*/
    open fun attachImageView(
        uri: Uri,
        parent: ViewGroup? = wrapLayout,
        fromInitialize: Boolean = true
    ) {
        parent?.setBackgroundColor(Color.BLACK)
        hideLoadingView()

        val dslSubSamplingItem = DslSubSamplingImageItem().apply {
            _dslSubSamplingItem = this

            itemData = uri
            itemLoadUri = uri

            itemDownloadStart = { itemHolder, task ->
                onDownloadStart(itemHolder, task)
                showLoadingView("下载中...")
            }

            itemDownloadFinish = { itemHolder, task, cause, error ->
                onDownloadFinish(itemHolder, task, cause, error)
                hideLoadingView()
            }
        }
        _dslVideoHolder = parent?.appendDslItem(dslSubSamplingItem)
    }

    /**加载文本*/
    open fun attachTextView(
        uri: Uri,
        parent: ViewGroup? = wrapLayout,
        fromInitialize: Boolean = true
    ) {
        hideLoadingView()
        parent?.inflate(R.layout.web_text_layout).apply {
            find<TextView>(R.id.lib_text_view)?.text = uri.toString()
        }
    }

    //endregion ---根据不同的类型, 填充不同的布局---

    open fun updateHost(url: String?) {
        val host = url?.toUri()?.host

        _vh.tv(R.id.lib_host_tip_view)?.text = span {
            if (!host.isNullOrEmpty()) {
                append("网页由 $host 提供")
            }
        }
    }

    /**加载网页类型的标题栏*/
    open fun loadWebTitleLayout() {
        //有些网页, 无法回退. 添加强制关闭按钮
        if (webConfig.showCloseButton) {
            appendLeftItem(ico = R.drawable.web_ic_close, action = {
                id = R.id.lib_close_view
                visibility = View.GONE
                updateMarginParams {
                    leftMargin = -6 * dpi
                }
            }) {
                close()
            }
        }

        if (webConfig.showRightMenu) {
            //更多
            appendRightItem(ico = R.drawable.web_ic_more) {
                showWebMenuDialog()
            }
        }
    }

    var _configMenuDialogAdapter: DslAdapter.(dialog: Dialog?) -> Unit = {

    }

    /**显示网页菜单对话框*/
    open fun showWebMenuDialog() {
        fContext().webMenuDialog {
            val url = getLoadUrl()
            webHost = url?.toUri()?.host

            if (isDebug()) {
                webDes = span {
                    append(getUserAgentString())
                    appendln()
                    append(url)
                }
            }

            line1Items = renderItemList {
                DslBaseWebMenuItem()() {
                    menuText = "刷新"
                    menuIcon = R.drawable.web_ic_refresh
                    itemClick = {
                        _dialog?.dismiss()
                        loadUrl(url)
                    }
                }
                DslBaseWebMenuItem()() {
                    menuText = "复制链接"
                    menuIcon = R.drawable.web_ic_copy
                    itemClick = {
                        _dialog?.dismiss()
                        url?.copy()
                    }
                }
                DslBaseWebMenuItem()() {
                    menuText = "分享"
                    menuIcon = R.drawable.web_ic_share
                    itemClick = {
                        _dialog?.dismiss()
                        dslIntentShare {
                            shareTitle = getWebTitle()
                            shareText = url
                        }
                    }
                }
                DslBaseWebMenuItem()() {
                    menuText = "浏览器打开"
                    menuIcon = R.drawable.web_ic_browser
                    itemClick = {
                        _dialog?.dismiss()
                        DslIntent.openUrl(fContext(), url)
                    }
                }

                //config
                _configMenuDialogAdapter(this, _dialog)
            }
        }
    }

    //endregion ---基础页面---

    //<editor-fold desc="其他操作">

    /**动态判断是否要显示强制关闭按钮*/
    open fun checkCloseView() {
        if (webConfig.showCloseButton) {
            if (canGoBack()) {
                leftControl()?.run {
                    visible(R.id.lib_close_view)
                }
            } else {
                leftControl()?.run {
                    gone(R.id.lib_close_view)
                }
            }
        }
    }

    open fun close() {
        dslFHelper {
            remove(this@BaseWebFragment)
        }
    }

    fun showLoadingView(tip: CharSequence? = null) {
        if (webConfig.showLoading) {
            _vh.visible(R.id.lib_arc_loading_view)
            _vh.visible(R.id.lib_tip_view, tip != null)
        }
        _vh.tv(R.id.lib_tip_view)?.text = tip
    }

    fun hideLoadingView() {
        _vh.gone(R.id.lib_arc_loading_view)
        _vh.gone(R.id.lib_tip_view)
    }

    //</editor-fold desc="其他操作">

    //region ---WebView---

    abstract fun canGoBack(): Boolean

    abstract fun goBack()

    abstract fun getWebTitle(): CharSequence?

    abstract fun getLoadUrl(): String?

    abstract fun getUserAgentString(): String?

    abstract fun loadUrl(url: String?)

    open fun attachWebView(
        url: String?,
        data: String? = null,
        parent: ViewGroup? = wrapLayout,
        fromInitialize: Boolean = true
    ) {
        //host提示
        if (_vh.view(R.id.lib_host_tip_view) == null) {
            rootControl().group(R.id.lib_coordinator_wrap_layout)?.apply {
                addView(inflate(R.layout.layout_web_host_tip, false), 0)
            }
        }
    }

    var _receivedTitle: CharSequence? = null

    /**接收到标题*/
    open fun receivedTitle(title: CharSequence?) {
        _receivedTitle = title
        if (webConfig.autoUpdateTitle && webConfig.title.isNullOrEmpty()) {
            fragmentTitle = title
        }
    }

    /**页面进度回调*/
    open fun progressChanged(url: String?, progress: Int) {
        // L.d("$url $progress")
        if (webConfig.showLoading) {
            _vh.bar(R.id.lib_progress_bar)?.setProgress(progress)
        }
        //加载框

        if (progress == 0) {
            if (webConfig.autoUpdateTitle && fragmentTitle.isNullOrEmpty()) {
                fragmentTitle = _string(R.string.adapter_loading)
            }
        } else if (progress == 100) {
            if (_receivedTitle.isNullOrEmpty()) {
                receivedTitle(getWebTitle())
            }
        }

        checkCloseView()

        if (progress >= 80) {
            hideLoadingView()
        }
    }

    /**页面下载回调*/
    open fun download(
        url: String /*下载地址*/,
        userAgent: String,
        contentDisposition: String,
        mime: String /*文件mime application/zip*/,
        length: Long /*文件大小 b*/
    ) {
        fContext().dslDialog {
            configBottomDialog()
            dialogLayoutId = R.layout.dialog_web_file_download
            onDialogInitListener = { dialog, dialogViewHolder ->
                val fileName = Web.getFileName(url, contentDisposition)
                dialogViewHolder.tv(R.id.target_url_view)?.text = url
                dialogViewHolder.tv(R.id.file_name_view)?.text = fileName
                dialogViewHolder.tv(R.id.file_size_view)?.text =
                    if (length > 0) length.fileSizeString() else "未知大小"
                dialogViewHolder.tv(R.id.file_type_view)?.text = mime

                dialogViewHolder.longClick(R.id.target_url_view) {
                    url.copy()
                    toastQQ("下载地址已复制")
                }

                dialogViewHolder.click(R.id.download_button) {
                    dialog.dismiss()
                    try {
                        url.downloadNotify()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    url.download {
                        onConfigTask = {
                            it.setFilename(fileName)
                        }
                    }
                }
            }
        }
    }

    /**打开对应app回调*/
    open fun openApp(url: String, activityInfo: ActivityInfo, appBean: AppBean) {
        fContext().dslDialog {
            configBottomDialog()
            dialogLayoutId = R.layout.dialog_web_open_app
            onDialogInitListener = { dialog, dialogViewHolder ->
                dialogViewHolder.tv(R.id.lib_text_view)?.text = appBean.appName
                dialogViewHolder.tv(R.id.lib_sub_text_view)?.text = url
                dialogViewHolder.img(R.id.lib_image_view)?.setImageDrawable(appBean.appIcon)
                dialogViewHolder.click(R.id.lib_reject_button) {
                    dialog.dismiss()
                }
                dialogViewHolder.click(R.id.lib_open_button) {
                    dialog.dismiss()
                    dslAHelper {
                        val componentName =
                            ComponentName(activityInfo.packageName, activityInfo.name)
                        start(url.urlIntent(componentName))
                    }
                }
            }
        }
    }

    /**文件选择回调*/
    open fun fileChoose(param: FileChooserParam) {
        when {
            param.mimeType.isNullOrEmpty() -> {
                //选择文件
                dslFHelper {
                    fileSelector {
                        it?.run {
                            fileChooseResult(arrayOf(fileUri))
                        } ?: fileChooseResult(null)
                    }
                }
            }

            param.mimeType.isImageMimeType() -> {
                //image
                try {
                    dslPicker({
                        singleImage()
                        maxSelectorLimit = param._selectorLimit
                    }) {
                        it?.let {
                            fileChooseResultList(it.mapTo(ArrayList()) { it.loadUri() })
                        } ?: fileChooseResult(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            param.mimeType.isVideoMimeType() -> {
                //video
                try {
                    dslPicker({
                        singleVideo()
                        maxSelectorLimit = param._selectorLimit
                    }) {
                        it?.let {
                            fileChooseResultList(it.mapTo(ArrayList()) { it.loadUri() })
                        } ?: fileChooseResult(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //endregion ---WebView---

    //region ---文件---

    open fun fileChooseResult(files: Array<Uri?>?) {
        //onReceiveValue(files)
    }

    open fun fileChooseResultList(files: List<Uri?>?) {
        fileChooseResult(files?.toTypedArray())
    }

    //endregion ---文件---

    //region ---生命周期操作---

    override fun onFragmentHide() {
        super.onFragmentHide()
        _dslVideoItem?.itemViewDetachedToWindow?.invoke(_dslVideoHolder!!, 0)
        _dslSubSamplingItem?.itemViewDetachedToWindow?.invoke(_dslVideoHolder!!, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _dslVideoItem?.itemViewRecycled?.invoke(_dslVideoHolder!!, 0)
        _dslSubSamplingItem?.itemViewRecycled?.invoke(_dslVideoHolder!!, 0)
    }

    //endregion ---生命周期操作---

}