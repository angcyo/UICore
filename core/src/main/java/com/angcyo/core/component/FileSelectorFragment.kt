package com.angcyo.core.component

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.HorizontalScrollView
import com.angcyo.DslFHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.component.file.DslFileLoader
import com.angcyo.core.component.file.FileItem
import com.angcyo.core.dslitem.DslFileSelectorItem
import com.angcyo.core.fragment.BaseFragment
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.library.ex.isFileScheme
import com.angcyo.library.ex.withMinValue
import com.angcyo.widget.layout.RLinearLayout
import com.angcyo.widget.layout.touch.TouchBackLayout
import com.angcyo.widget.progress.HSProgressView
import com.angcyo.widget.recycler.initDslAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    private var selectorItemView: RLinearLayout? = null

    init {
        fragmentLayoutId = R.layout.lib_file_selector_fragment
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
                        dslFHelper {
                            noAnim()
                            remove(this@FileSelectorFragment)
                        }
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
        //选择按钮
        _vh.click(R.id.file_selector_button) {
            //T_.show(selectorFilePath)

            dslFHelper {
                remove(this@FileSelectorFragment)
            }

            fileSelectorConfig.onFileSelector?.invoke(fileSelectorConfig.selectorFileUri)
        }

        _vh.rv(R.id.lib_recycler_view)?.apply {
            _adapter = initDslAdapter()
        }

        dslFileLoader.onLoaderResult = {
            _vh.gone(R.id.lib_progress_view)
            _adapter.loadSingleData<DslFileSelectorItem>(it, 1, Int.MAX_VALUE) { oldItem, data ->
                (oldItem ?: DslFileSelectorItem()).apply {
                    itemShowFileMd5 = true
                    itemFile = data as FileItem
                }
            }
        }
        dslFileLoader.onFileMd5Result = {

        }
    }

    override fun onFragmentFirstShow(bundle: Bundle?) {
        super.onFragmentFirstShow(bundle)
        loadPath(fileSelectorConfig.targetPath, 360)
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
                it.scrollTo((it.getChildAt(0).measuredWidth - it.measuredWidth).withMinValue(0), 0)
            }
        }

        loadFileList(fileSelectorConfig.targetPath, delay)
    }

    val dslFileLoader = DslFileLoader()
    private fun loadFileList(path: String, delay: Long = 0L) {
        _vh.v<HSProgressView>(R.id.lib_progress_view)?.apply {
            visibility = View.VISIBLE
            startAnimator()
        }
        dslFileLoader.load(path)
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
    var storageDirectory = Environment.getExternalStorageDirectory().absolutePath
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

    val simpleFormat by lazy {
        SimpleDateFormat("yyyy/MM/dd", Locale.CHINA)
    }

    /*选中的文件*/
    var selectorFileUri: Uri? = null

    var onFileSelector: ((Uri?) -> Unit)? = null
}

/**文件选择*/
fun DslFHelper.fileSelector(onResult: (Uri?) -> Unit) {
    noAnim()
    show(FileSelectorFragment().apply {
        fileSelectorConfig {
            onFileSelector = onResult
        }
    })
}