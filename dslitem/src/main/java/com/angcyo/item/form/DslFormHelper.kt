package com.angcyo.item.form

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter._dslAdapter
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.library.L
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.size
import com.angcyo.library.toastQQ
import com.angcyo.widget.recycler.*

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

    /**安装组件*/
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

    /**卸载组件*/
    fun uninstall() {
        _recyclerView?.apply {
            removeItemDecoration {
                it == formItemDecoration || it is DslFormItemDecoration
            }
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

        //
        formItemDecoration.restoreLabelColor()

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

    //<editor-fold desc="仅检查数据">

    /** 检查表单数据验证
     * 返回 true 数据通过 */
    fun checkFormData(
        adapter: DslAdapter,
        params: DslFormParams = DslFormParams.fromHttp(),
        onCheckNext: (DslAdapterItem, Int) -> Unit = { _, _ -> },
        end: (error: Throwable?) -> Unit
    ) {
        if (params.skipFormCheck) {
            end(null)
            return
        }
        val allDataList = adapter.getDataList(params.useFilterList)
        _checkFormData(params, allDataList, 0, onCheckNext, end)
    }

    fun _checkFormData(
        params: DslFormParams,
        itemList: List<DslAdapterItem>,
        checkIndex: Int,
        onCheckNext: (DslAdapterItem, Int) -> Unit = { _, _ -> },
        end: (error: Throwable?) -> Unit
    ) {
        if (checkIndex >= itemList.size()) {
            end(null)
            return
        }

        val item = itemList[checkIndex]
        L.i("开始检查表单数据:$checkIndex/${itemList.size} ->$item")

        //错误表单之前, 是否有悬浮item
        var haveHoverItem = false

        if (item is IFormItem) {
            params._formAdapterItem = item

            if (item.itemIsHover) {
                haveHoverItem = true
            }

            if (params.formCheckBeforeAction(item)) {
                //跳过了默认的check检查
            } else {
                item.itemFormConfig.formCheck(params) { error ->
                    params._formAdapterItem = null
                    if (error == null) {
                        //无错误
                        //item.formRequired 表单的[formRequired]必填判断, 请在[formCheck]里面自行处理
                        onCheckNext(item, checkIndex)
                        _checkFormData(params, itemList, checkIndex + 1, onCheckNext, end)
                    } else {
                        //有错误
                        tipFormItemError(item)
                        //end(IllegalStateException("第${checkIndex}个Item有异常"))
                        end(error)
                    }
                }
            }
        } else {
            params._formAdapterItem = null
            _checkFormData(params, itemList, checkIndex + 1, onCheckNext, end)
        }
    }

    //</editor-fold desc="仅检查数据">

    //<editor-fold desc="检查后, 获取数据">

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

        L.i("开始获取表单数据:$obtainIndex/${itemList.size} ->$item")

        if (item is IFormItem) {
            params._formAdapterItem = item
            if (!params.formObtainBeforeAction(item)) {
                item.itemFormConfig.formObtain(params) { error ->
                    params._formAdapterItem = null
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

    //</editor-fold desc="检查后, 获取数据">

    //<editor-fold desc="检查的同时获取数据">

    /**检查的同时获取数据*/
    fun checkAndObtainData(
        adapter: DslAdapter = _recyclerView!!._dslAdapter!!,
        params: DslFormParams = DslFormParams.fromHttp(),
        end: (params: DslFormParams, error: Throwable?) -> Unit
    ) {
        val dataList = adapter.getDataList(params.useFilterList)

        if (params.isHttp()) {
            _checkFormData(params, dataList, 0, { item, index ->
                L.i("开始获取表单数据:$index/${dataList.size} ->$item")
                if (item is IFormItem) {
                    params._formAdapterItem = item
                    if (!params.formObtainBeforeAction(item)) {
                        item.itemFormConfig.formObtain(params) { error ->
                            params._formAdapterItem = null
                            if (error == null) {
                                //无异常
                                params.formObtainAfterAction(item)
                            }
                        }
                    }
                }
            }) {
                //检查结束
                end(params, it)
            }
        } else {
            _obtainData(adapter, params, dataList, 0) {
                end(params, it)
            }
        }
    }

    //</editor-fold desc="检查的同时获取数据">
}

/**检查所有Item, 判断是否有错误, 并提示错误
 * [com.angcyo.dsladapter.DslAdapterItem.itemThrowable] 错误信息存储
 * [predicate] 返回当前item是否有错误
 * @return 是否有错误*/
fun DslAdapter.checkItemThrowable(
    predicate: (DslAdapterItem) -> Boolean = {
        it.itemThrowable != null
    }
): Boolean {
    var reuslt = false
    val validFilterDataList = getValidFilterDataList()
    for (item in validFilterDataList) {
        if (predicate(item)) {
            reuslt = true
            //中断
            _recyclerView?.let {
                DslFormHelper().apply {
                    install(it)
                    tipFormItemError(item)
                }
            }.elseNull {
                item.itemThrowable?.message?.let {
                    toastQQ(it)
                }
            }
            break
        }
    }
    return reuslt
}