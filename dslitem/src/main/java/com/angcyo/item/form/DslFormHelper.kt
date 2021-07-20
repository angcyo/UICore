package com.angcyo.item.form

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter._dslAdapter
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.library.L
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.size
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

    /** 检查表单数据验证
     * 返回 true 数据通过 */
    fun checkFormData(
        adapter: DslAdapter,
        params: DslFormParams,
        end: (error: Throwable?) -> Unit
    ) {
        if (params.skipFormCheck) {
            return
        }
        val allDataList = adapter.getDataList(params.useFilterList)
        _checkFormData(params, allDataList, 0, end)
    }

    fun _checkFormData(
        params: DslFormParams,
        itemList: List<DslAdapterItem>,
        checkIndex: Int,
        end: (error: Throwable?) -> Unit
    ) {
        if (checkIndex >= itemList.size()) {
            end(null)
            return
        }

        val item = itemList[checkIndex]

        var result = RecyclerView.NO_POSITION
        var formItem: DslAdapterItem? = null

        //错误表单之前, 是否有悬浮item
        var haveHoverItem = false

        if (item is IFormItem) {
            params._formAdapterItem = item
            formItem = item

            if (item.itemIsHover) {
                haveHoverItem = true
            }

            if (params.formCheckBeforeAction(item)) {
                //跳过了默认的check检查
            } else {
                item.itemFormConfig.formCheck(params) { error ->
                    if (error == null) {
                        //item.formRequired 表单的[formRequired]必填判断, 请在[formCheck]里面自行处理
                    } else {
                        result = checkIndex
                    }
                }

            }
        } else {
            params._formAdapterItem = null
        }

        if (result != RecyclerView.NO_POSITION) {
            //有错误
            formItem?.let {
                tipFormItemError(it)
            }
            end(IllegalStateException("第${result}个Item有异常"))
        } else {
            _checkFormData(params, itemList, checkIndex + 1, end)
        }
    }

    /**获取表单数据, 支持 json 和 map
     * 默认是可见item的数据*/
    fun obtainData(
        adapter: DslAdapter = _recyclerView!!._dslAdapter!!,
        params: DslFormParams = DslFormParams.fromHttp(),
        end: (params: DslFormParams, error: Throwable?) -> Unit
    ) {
        val dataList = adapter.getDataList(params.useFilterList)

        if (params.isHttp()) {
            checkFormData(adapter, params) {
                if (it == null) {
                    _obtainData(adapter, params, dataList, 0) {
                        end(params, it)
                    }
                } else {
                    //验证失败
                    end(params, it)
                }
            }
        } else {
            _obtainData(adapter, params, dataList, 0) {
                end(params, it)
            }
        }
    }

    fun _obtainData(
        adapter: DslAdapter,
        params: DslFormParams,
        itemList: List<DslAdapterItem>,
        obtainIndex: Int,
        end: (error: Throwable?) -> Unit
    ) {

        if (obtainIndex >= itemList.size()) {
            end(null)
            return
        }

        val item = itemList[obtainIndex]

        if (item is IFormItem) {
            params._formAdapterItem = item
            if (!params.formObtainBeforeAction(item)) {
                item.itemFormConfig.formObtain(params) { error ->
                    if (error == null) {
                        //无异常
                        params.formObtainAfterAction(item)
                        _obtainData(adapter, params, itemList, obtainIndex + 1, end)
                    } else {
                        //异常
                        end(error)
                    }
                }
            }
        } else {
            params._formAdapterItem = null
            _obtainData(adapter, params, itemList, obtainIndex + 1, end)
        }
    }
}