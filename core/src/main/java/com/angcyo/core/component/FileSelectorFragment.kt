package com.angcyo.core.component

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import androidx.fragment.app.Fragment
import com.angcyo.DslFHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.component.file.DslFileLoader
import com.angcyo.core.component.file.FileItem
import com.angcyo.core.component.file.file
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.core.fragment.BaseFragment
import com.angcyo.coroutine.onBack
import com.angcyo.dialog.dslLoading
import com.angcyo.dialog.hideLoading
import com.angcyo.dialog.itemsDialog
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.data.loadSingleData2
import com.angcyo.fragment.requestPermissions
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.*
import com.angcyo.library.toastWX
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.appFolderPath
import com.angcyo.library.utils.storage.haveSdCardPermission
import com.angcyo.library.utils.storage.sdCardManagePermission
import com.angcyo.widget._rv
import com.angcyo.widget.layout.touch.TouchBackLayout
import com.angcyo.widget.progress.HSProgressView
import com.angcyo.widget.recycler.initDslAdapter
import com.angcyo.widget.span.span
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/29
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class FileSelectorFragment : BaseFragment() {

    private var fileSelectorConfig = FileSelectorConfig()

    /**获取上一层路径*/
    private fun getPrePath(): String =
        fileSelectorConfig.targetPath.substring(0, fileSelectorConfig.targetPath.lastIndexOf("/"))

    private var scrollView: HorizontalScrollView? = null

    /**选中的文件item*/
    private var selectorFileItem: FileItem? = null

    init {
        fragmentLayoutId = R.layout.lib_file_selector_fragment
    }

    override fun canSwipeBack(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        if (fileSelectorConfig.targetPath == fileSelectorConfig.storageDirectory) {
            //已经是根目录了, 再次返回就是关闭界面
            send(null)
        } else {
            //否则返回上一级
            resetPath(getPrePath())
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //默认选中
        fileSelectorConfig.selectorFileUri?.run {
            selectorFileItem = FileItem(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _vh.click(R.id.lib_touch_back_layout) {
            send(null)
        }
        doShowAnimator()
    }

    /**
     * 调用此方法, 配置参数
     * */
    fun fileSelectorConfig(config: FileSelectorConfig.() -> Unit): FileSelectorFragment {
        this.fileSelectorConfig.config()
        return this
    }

    lateinit var _adapter: DslAdapter

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        //半屏效果
        _vh.v<TouchBackLayout>(R.id.lib_touch_back_layout)?.apply {
            enableTouchBack = true
            offsetScrollTop = (resources.displayMetrics.heightPixels) / 2

            onTouchBackListener = object : TouchBackLayout.OnTouchBackListener {
                override fun onTouchBackListener(
                    layout: TouchBackLayout,
                    oldScrollY: Int,
                    scrollY: Int,
                    maxScrollY: Int
                ) {
                    if (scrollY >= maxScrollY) {
                        removeFragment()
                    }
                }
            }
        }

        _vh.tv(R.id.current_file_path_view)?.text = fileSelectorConfig.targetPath
        _vh.view(R.id.file_selector_button)?.isEnabled = false

        scrollView = _vh.v(R.id.current_file_path_layout)

        /*上一个路径*/
        _vh.click(R.id.current_file_path_layout) {
            resetPath(getPrePath())
        }
        /*回到app根目录*/
        _vh.click(R.id.file_go_home_view) {
            resetPath(
                FileUtils.appRootExternalFolder()?.absolutePath?.prePath()
                    ?: fileSelectorConfig.targetPath
            )
        }
        //选择按钮
        _vh.click(R.id.file_selector_button) {
            //T_.show(selectorFilePath)
            send(selectorFileItem)
        }

        _vh.rv(R.id.lib_recycler_view)?.apply {
            _adapter = initDslAdapter()
            _adapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
            _adapter.singleModel()

            _adapter.selector().observer {
                onItemChange = { selectorItems, selectorIndexList, _, _ ->
                    selectorFileItem =
                        (selectorItems.firstOrNull() as? DslFileSelectorItem)?.itemFile
                    _vh.enable(R.id.file_selector_button, selectorIndexList.isNotEmpty())
                }
            }
        }

        //文件列表加载返回
        dslFileLoader.onLoaderResult = {
            _vh.gone(R.id.lib_progress_view)
            _adapter.loadSingleData2<DslFileSelectorItem>(it, 1, Int.MAX_VALUE) { data ->
                itemShowFileMd5 = fileSelectorConfig.showFileMd5
                itemFile = data as? FileItem
                itemIsSelected = selectorFileItem == itemFile

                itemClick = {
                    itemFile?.apply {
                        if (file().isFile()) {
                            _adapter.select {
                                it == this@loadSingleData2
                            }
                        } else if (file().isFolder()) {
                            resetPath(file()!!.absolutePath)
                        }
                    }
                }

                if (fileSelectorConfig.showFileMenu) {
                    itemLongClick = {
                        fContext().itemsDialog {
                            canceledOnTouchOutside = true
                            dialogBottomCancelItem = null

                            addDialogItem {
                                itemTextGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                itemText = span {
                                    drawable {
                                        backgroundDrawable = _drawable(R.drawable.ic_file_open)
                                    }
                                    append(" 打开")
                                }
                                itemClick = {
                                    _dialog?.dismiss()
                                    itemFile?.file()?.open()
                                }
                            }

                            addDialogItem {
                                itemTextGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                itemText = span {
                                    drawable {
                                        backgroundDrawable = _drawable(R.drawable.ic_file_delete)
                                    }
                                    append(" 删除")
                                }
                                itemClick = {
                                    _dialog?.dismiss()
                                    dslLoading(true) {
                                        loadingConfig = {
                                            cancelable = false
                                        }
                                        loadingShowCloseView = false
                                    }
                                    launchLifecycle {
                                        onBack {
                                            itemFile?.file()?.deleteRecursively() == true
                                        }.await().apply {
                                            hideLoading()
                                            if (this) {
                                                _adapter.render {
                                                    removeItem(this@loadSingleData2)
                                                }
                                            } else {
                                                toastWX("删除失败", fContext(), R.drawable.lib_ic_error)
                                            }
                                        }
                                    }
                                }
                            }

                            addDialogItem {
                                itemTextGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                                itemText = span {
                                    drawable {
                                        backgroundDrawable = _drawable(R.drawable.ic_file_share)
                                    }
                                    append(" 分享")
                                }
                                itemClick = {
                                    _dialog?.dismiss()
                                    itemFile?.file()?.shareFile(it.context)
                                }
                            }
                        }
                        true
                    }
                }
            }

            _adapter.onDispatchUpdatesOnce {
                _vh._rv(R.id.lib_recycler_view)?.scrollHelper?.lockScrollToFirst {
                    scrollAnim = false
                }
            }
        }

        //文件耗时操作返回
        dslFileLoader.onLoaderDelayResult = { fileItem ->
            _adapter.updateItem {
                (it as? DslFileSelectorItem)?.itemFile == fileItem
            }
        }
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        _vh.post {
            if (haveSdCardPermission()) {
                //具有SD卡权限
                loadPath(fileSelectorConfig.targetPath, 360)
            } else if (fileSelectorConfig.targetPath.contains(fContext().packageName)) {
                //不具有SD卡权限, 但是路径包含包名, 说明是app内部路径
                loadPath(fileSelectorConfig.targetPath, 360)
            } else {
                fContext().requestPermissions(sdCardManagePermission()) {
                    if (it) {
                        loadPath(fileSelectorConfig.targetPath, 360)
                    } else {
                        toastWX(_string(R.string.permission_disabled))
                    }
                }
            }
        }
    }

    private fun setSelectorFilePath(uri: Uri) {
        fileSelectorConfig.selectorFileUri = uri
        _vh.view(R.id.file_selector_button)?.isEnabled = uri.isFileScheme()
    }

    private fun resetPath(path: String, force: Boolean = false) {
        //L.e("call: resetPath -> $path")
        fileSelectorConfig.targetPath = path
        if (!force && _vh.tv(R.id.current_file_path_view)?.text.toString() == fileSelectorConfig.targetPath) {
            return
        }
        loadPath(path)
    }

    private fun loadPath(path: String, delay: Long = 0L) {
        fileSelectorConfig.targetPath = path
        //_vh.view(R.id.base_selector_button).isEnabled = false
        _vh.tv(R.id.current_file_path_view)?.text = fileSelectorConfig.targetPath

        scrollView?.let {
            it.post {
                val x = it.getChildAt(0).measuredWidth - it.drawWidth
                it.scrollTo(x.withMinValue(0), 0)
            }
        }

        loadFileList(fileSelectorConfig.targetPath, delay)
    }

    //文件loader
    val dslFileLoader = DslFileLoader()

    private fun loadFileList(path: String, delay: Long = 0L) {
        _vh.v<HSProgressView>(R.id.lib_progress_view)?.apply {
            visibility = View.VISIBLE
            startAnimator()
        }
        _vh.postDelay(delay) {
            dslFileLoader.loadHideFile = fileSelectorConfig.showHideFile
            dslFileLoader.load(path)
        }
    }

    private fun doShowAnimator() {
        _vh.view(R.id.lib_touch_back_layout)?.run {
            doAnimate {
                translationY = this.measuredHeight.toFloat()
                animate().translationY(0f).setDuration(Anim.ANIM_DURATION).start()
            }
        }
    }

    private fun doHideAnimator(onEnd: () -> Unit) {
        _vh.view(R.id.lib_touch_back_layout)?.run {
            if (hasTransientState()) {
                return
            }
            animate()
                .translationY(this.measuredHeight.toFloat())
                .setDuration(Anim.ANIM_DURATION)
                .withEndAction(onEnd)
                .start()
        }
    }

    /**移除界面*/
    private fun removeFragment() {
        dslFHelper {
            noAnim()
            remove(this@FileSelectorFragment)
        }
    }

    /**发送返回结果*/
    private fun send(fileItem: FileItem? = null) {
        doHideAnimator {
            removeFragment()
            fileSelectorConfig.onFileSelector?.invoke(fileItem)
            fileSelectorConfig = FileSelectorConfig()
        }
    }
}

open class FileSelectorConfig {

    /**是否显示隐藏文件*/
    var showHideFile = false

    /**是否显示文件MD5值*/
    var showFileMd5 = false

    /**是否长按显示文件菜单*/
    var showFileMenu = false

    /**最根的目录*/
    var storageDirectory: String = Environment.getExternalStorageDirectory().absolutePath
        set(value) {
            if (File(value).exists()) {
                field = value
                targetPath = value
            }
        }

    /**目标路径*/
    var targetPath: String = storageDirectory
        set(value) {
            if (value.isNotEmpty() && value.startsWith(storageDirectory)) {
                val file = File(value)
                if (file.isDirectory) {
                    field = value
                } else if (file.isFile) {
                    field = file.parent
                }
            } else {
                field = storageDirectory
            }
        }

    /*默认选中的文件*/
    var selectorFileUri: Uri? = null

    var onFileSelector: ((FileItem?) -> Unit)? = null
}

/**DSL
 * [com.angcyo.component.ResultKtx.getFile]
 * */
@DSL
fun Fragment.fileSelector(
    config: FileSelectorConfig.() -> Unit = {},
    onResult: (FileItem?) -> Unit = {}
) {
    dslFHelper {
        fileSelector(config, onResult)
    }
}

/**文件选择
 * [com.angcyo.component.ResultKtx.getFile]
 * */
@DSL
fun DslFHelper.fileSelector(
    config: FileSelectorConfig.() -> Unit = {},
    onResult: (FileItem?) -> Unit = {}
) {
    noAnim()
    show(FileSelectorFragment().apply {
        fileSelectorConfig {
            targetPath = appFolderPath()
            config()
            onFileSelector = onResult
        }
    })
}

/**获取上一层路径*/
fun String.prePath(): String? {
    val index = lastIndexOf("/")
    if (index == -1) {
        return null
    }
    return substring(0, index)
}