package com.angcyo.item.form

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.library.L
import com.angcyo.library.ex.elseNull
import com.angcyo.tablayout.clamp
import com.angcyo.widget.recycler.DslRecyclerView
import com.angcyo.widget.recycler.ScrollHelper
import com.angcyo.widget.recycler.haveItemDecoration
import com.angcyo.widget.recycler.scrollHelper

/**
 * 表单助手, 用于获取表单数据, 表单验证等相关操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslFormHelper {

    var _recyclerView: RecyclerView? = null

    /**表单错误提示分割线*/
    var formItemDecoration = DslFormItemDecoration()

    fun install(recyclerView: RecyclerView?) {
        uninstall()
        _recyclerView = recyclerView
        _recyclerView?.apply {
            haveItemDecoration {
                it == formItemDecoration
            }.let {
                if (!it) {
                    addItemDecoration(formItemDecoration)
                }
            }
        }
    }

    fun uninstall() {
        _recyclerView?.apply {
            removeItemDecoration(formItemDecoration)
        }
    }

    /**错误提示[formItem]*/
    fun tipFormItemError(formItem: DslAdapterItem?) {
        if (formItem == null) {
            return
        }

        formItemDecoration.itemErrorTipTask?.apply {
            L.w("已有错误提示在执行:$errorPosition p:$errorPathProgress")
            //重置进度
            errorPathProgress = 0f
            return
        }

        _dslAdapter {
            val dslAdapter = this
            val position = formItem.itemIndexPosition(dslAdapter)
            if (position != RecyclerView.NO_POSITION) {

                //错误提示
                val task = ItemErrorTipTask(position)
                formItemDecoration.itemErrorTipTask = task

                _recyclerView?.apply {
                    if (this is DslRecyclerView) {
                        this.scrollHelper
                    } else {
                        scrollHelper()
                    }.lockPositionByDraw {
                        scrollType = ScrollHelper.SCROLL_TYPE_TOP
                        lockPosition = clamp(position + task.errorPositionOffset, 0, itemCount)
                    }
                }
            }
        }
    }

    fun _dslAdapter(action: DslAdapter.() -> Unit) {
        _recyclerView?.apply {
            if (adapter is DslAdapter) {
                (adapter as DslAdapter).action()
            } else {
                L.e("adapter is null.")
            }
        }.elseNull {
            L.e("请先调用[install]")
        }
    }

/*    */
    /**获取表单对应的json数据*//*
    fun obtainFormJson(params: FormParams = FormParams(), action: (FormDataEntity) -> Unit) {
        obtainFormJsonDsl(params) {
            action(this)
        }
    }

    fun obtainFormJsonDsl(
        params: FormParams = FormParams(),
        action: FormDataEntity.(FormParams) -> Unit
    ) {
        _dslAdapter {
            if (checkFormData(params)) {

                getDataList(params.useFilterList).forEachIndexed { _, dslAdapterItem ->
                    if (dslAdapterItem is IFormItem) {
                        if (!params.formObtainBeforeAction(dslAdapterItem)) {
                            dslAdapterItem.formObtain(params)

                            params.formObtainAfterAction(dslAdapterItem)
                        }
                    }
                }

                FormDataEntity(formJson = params.jsonBuilder.get()).action(params)
            }
        }
    }*/

    /** 检查表单数据验证
     * 返回 true 数据通过 */
    fun DslAdapter.checkFormData(params: DslFormParams): Boolean {
        if (params.skipFormCheck) {
            return true
        }

        var result = RecyclerView.NO_POSITION

        var formItem: DslAdapterItem? = null

        //错误表单之前, 是否有悬浮item
        var haveHoverItem = false

        val allDataList = getDataList(params.useFilterList)

        for (i in allDataList.indices) {
            val item = allDataList[i]

            if (item is IFormItem) {
                formItem = item

                if (item.itemIsHover) {
                    haveHoverItem = true
                }

                if (params.formCheckBeforeAction(item)) {
                    //跳过了默认的check检查
                } else if (item.itemFormConfig.formCheck(params)) {
                    //item.formRequired 表单的[formRequired]必填判断, 请在[formCheck]里面自行处理
                } else {
                    result = i
                    break
                }
            }
        }

        if (result != RecyclerView.NO_POSITION) {
            formItem?.let {
                tipFormItemError(it)
            }
        }
        return result == RecyclerView.NO_POSITION
    }
}

/**获取用于保存的表单json数据*/
fun DslAdapter.obtainFormSaveJson(
    params: DslFormParams = DslFormParams.fromSave(),
    action: (json: String) -> Unit
) {

    getDataList(params.useFilterList).forEachIndexed { _, dslAdapterItem ->
        if (dslAdapterItem is IFormItem) {
            if (!params.formObtainBeforeAction(dslAdapterItem)) {
                dslAdapterItem.itemFormConfig.formObtain(params)

                params.formObtainAfterAction(dslAdapterItem)
            }
        }
    }

    action(params.jsonBuilder.get())
}