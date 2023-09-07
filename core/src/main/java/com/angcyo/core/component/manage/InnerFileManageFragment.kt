package com.angcyo.core.component.manage

import android.os.Bundle
import com.angcyo.core.R
import com.angcyo.core.component.model.LanguageModel
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.core.vmApp
import com.angcyo.dialog.itemsDialog
import com.angcyo.http.rx.doBack
import com.angcyo.library.component.appBean
import com.angcyo.library.ex._string
import com.angcyo.library.ex.openApp
import com.angcyo.library.toastQQ

/**
 * 内部的文件管理界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
open class InnerFileManageFragment : BaseDslFragment() {

    val innerFileManageModel = vmApp<InnerFileManageModel>()

    init {
        fragmentTitle = _string(R.string.core_file_list)

        fragmentConfig.isLightStyle = true
        fragmentConfig.showTitleLineView = true

        enableAdapterRefresh = true

        page.filePage()
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        appendRightItem(ico = R.drawable.core_help_svg) {
            toastQQ("导入文件帮助")
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
    }

    override fun onLoadData() {
        super.onLoadData()

        doBack {
            //加载文件列表
            val list = innerFileManageModel.loadInnerFile(page)
            loadDataEnd(DslInnerFileItem::class, list)
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