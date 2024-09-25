package com.angcyo.core.component.manage

import android.content.Context
import android.os.Bundle
import com.angcyo.base.dslAHelper
import com.angcyo.base.removeThis
import com.angcyo.core.CoreApplication
import com.angcyo.core.R
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.core.vmApp
import com.angcyo.dialog.itemsDialog
import com.angcyo.dsladapter.allSelectedItem
import com.angcyo.getData
import com.angcyo.http.rx.doBack
import com.angcyo.library.annotation.DSL
import com.angcyo.library.component.appBean
import com.angcyo.library.ex._string
import com.angcyo.library.ex.openApp
import com.angcyo.library.toastQQ
import com.angcyo.putData
import com.angcyo.viewmodel.updateValue
import java.io.File

/**
 * 内部的文件管理界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
open class InnerFileManageFragment : BaseDslFragment() {

    val innerFileManageModel = vmApp<InnerFileManageModel>()

    /**选择模式下的参数*/
    var innerFileSelectParamBean = InnerFileSelectParamBean()

    /**是否是选择模式*/
    val _isSelectModel: Boolean
        get() = innerFileSelectParamBean.maxSelectFileCount > 0

    init {
        fragmentTitle = _string(R.string.core_file_list)

        fragmentConfig.isLightStyle = true
        fragmentConfig.showTitleLineView = true

        enableAdapterRefresh = true

        page.filePage()

        contentLayoutId = R.layout.layout_inner_file_manage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        innerFileSelectParamBean = getData() ?: innerFileSelectParamBean
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        val helpUrl = innerFileManageModel.innerFileImportHelpUrl
        if (!helpUrl.isNullOrBlank()) {
            appendRightItem(ico = R.drawable.core_help_svg) {
                CoreApplication.onOpenUrlAction?.invoke(helpUrl)
            }
        }
        appendRightItem(ico = R.drawable.core_add_svg) {
            showAddFileDialog()
        }

        innerFileManageModel.importStateOnceData.observe {
            if (it == true) {
                //导入成功
                startRefresh()
            }
        }

        _vh.visible(R.id.lib_bottom_wrap_layout, _isSelectModel)
        _vh.enable(R.id.lib_bottom_wrap_layout, false)
        _vh.click(R.id.import_button) {
            val selectList = _adapter.allSelectedItem()
            if (selectList.isEmpty()) {
                //toastQQ("请选择文件")
                toastQQ(_string(R.string.ui_choose))
            } else {
                innerFileManageModel.innerSelectedFileOnceData.updateValue(selectList.map { it.itemData as File })
                removeThis()
            }
        }

        _adapter.onDispatchUpdatesAfter {
            _vh.visible(R.id.lib_bottom_wrap_layout, !_adapter.isEmpty())
        }
    }

    override fun onLoadData() {
        super.onLoadData()

        doBack {
            //加载文件列表
            val list =
                innerFileManageModel.loadInnerFile(page, innerFileSelectParamBean.filterFileExtList)
            loadDataEnd(DslInnerFileItem::class, list) {
                itemMaxSelectCount = innerFileSelectParamBean.maxSelectFileCount

                if (_isSelectModel) {
                    observeItemChange {
                        val selectList = itemDslAdapter?.allSelectedItem()
                        _vh.enable(R.id.lib_bottom_wrap_layout, !selectList.isNullOrEmpty())
                    }
                }
            }
        }
    }

    /**显示添加文件方式对话框*/
    private fun showAddFileDialog() {
        val list = if (LanguageModel.isChinese()) {
            innerFileManageModel.zhImportFilePackageNameList
        } else {
            innerFileManageModel.importFilePackageNameList
        }

        val appList = list.mapNotNull { it.appBean() }
        fContext().itemsDialog {
            for (appBean in appList) {
                addDialogItem {
                    itemText = appBean.appName
                    itemClick = {
                        _dialog?.dismiss()
                        appBean.packageName.openApp()
                    }
                }
            }
        }
    }
}

/**快速启动内部文件选择界面*/
@DSL
fun Context.innerFileSelectFragment(
    maxSelectFileCount: Int = 0,
    filterFileExtList: List<String>? = null
) {
    dslAHelper {
        start(InnerFileManageFragment::class) {
            putData(InnerFileSelectParamBean(maxSelectFileCount, filterFileExtList))
        }
    }
}