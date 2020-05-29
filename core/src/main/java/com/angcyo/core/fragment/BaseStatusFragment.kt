package com.angcyo.core.fragment

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.refresh.IRefreshContentBehavior
import com.angcyo.core.R
import com.angcyo.core.dslitem.DslStatusTipItem
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.ItemSelectorHelper.Companion.MODEL_SINGLE
import com.angcyo.getData
import com.angcyo.library.ex.each
import com.angcyo.widget.recycler.initDsl

/**
 * 左右双RV, 状态切换表单界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/11/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class BaseStatusFragment : BaseTitleFragment() {

    companion object {
        const val SELECT_INDEX = "select_index"
    }

    /**默认选中第几项*/
    var defaultSelectIndex = 0

    /**状态映射*/
    val statusList = mutableListOf<StatusItem>()

    /**当前选中的状态*/
    var currentStatus: StatusItem? = null

    val leftDslAdapter = DslAdapter()
    val rightDslAdapter: DslAdapter? get() = rightRecyclerView?.adapter as DslAdapter?

    var leftRecyclerView: RecyclerView? = null
    var rightRecyclerView: RecyclerView? = null

    init {
        contentLayoutId = R.layout.lib_status_content_fragment
    }

    open fun needLoadLeftNetData(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defaultSelectIndex = getData(SELECT_INDEX) ?: 0
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        leftRecyclerView = _vh.rv(R.id.left_recycler_view)
        rightRecyclerView = _vh.rv(R.id.lib_recycler_view)

        leftRecyclerView?.initDsl()
        rightRecyclerView?.initDsl()

        if (needLoadLeftNetData()) {
            onLoadLeftNetData {
                _initLeftRecyclerView()
            }
        } else {
            _initLeftRecyclerView()
        }
    }

    fun _initLeftRecyclerView() {
        leftRecyclerView?.adapter = leftDslAdapter

        statusList.forEach { entry ->
            leftDslAdapter + DslStatusTipItem().apply {
                itemStatusText = entry.statusText
                itemStatusCount = entry.statusCount

                itemData = entry

                itemClick = {
                    leftDslAdapter.itemSelectorHelper.selector(this)
                }
            }
        }

        leftDslAdapter.itemSelectorHelper.onItemSelectorListener = object : OnItemSelectorListener {
            override fun onSelectorItemChange(
                selectorItems: MutableList<DslAdapterItem>,
                selectorIndexList: MutableList<Int>,
                isSelectorAll: Boolean,
                selectorParams: SelectorParams
            ) {
                super.onSelectorItemChange(
                    selectorItems,
                    selectorIndexList,
                    isSelectorAll,
                    selectorParams
                )
                statusList.getOrNull(selectorIndexList.firstOrNull() ?: -1)?.apply {
                    onSelectorStatus(this)
                }
            }
        }

        //单选模式
        leftDslAdapter.itemSelectorHelper.selectorModel = MODEL_SINGLE

        //默认选中
        leftDslAdapter.onDispatchUpdatesAfterOnce = {
            if (defaultSelectIndex < statusList.size) {
                it.itemSelectorHelper.selector(defaultSelectIndex)
            } else {
                it.itemSelectorHelper.selector(0)
            }
        }
    }

    override fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {
        //super.onRefresh(refreshContentBehavior)

    }

    fun onAdapterRefresh(dslAdapter: DslAdapter) {

    }

    /**注册状态*/
    fun registerStatus(status: StatusItem) {
        statusList.add(status)

        status.dslAdapter.apply {
            //setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
            //setOnLoadMoreListener(this@BaseStatusFragment)
        }
    }

    /**清空所有状态的[DslAdapter]数据*/
    fun clearAllStatusData() {
        statusList.forEach { entry ->
            entry.dslAdapter.clearItems()
        }
        //onBaseRefresh(null)
    }

    /**改变左边状态*/
    fun changeStatus(run: () -> Unit) {
        run()

        each(statusList, leftDslAdapter.getValidFilterDataList()) { status, item ->
            (item as? DslStatusTipItem)?.apply {
                itemChanging = true
                itemStatusText = status.statusText
                itemStatusCount = status.statusCount
            }
        }

        leftDslAdapter.updateItemDepend()
    }

    /**切换了左边的状态*/
    open fun onSelectorStatus(statusItem: StatusItem) {
        if (currentStatus == statusItem) {
            return
        }

        currentStatus = statusItem

//        //取消之前的请求
//        onCancelSubscriptions()
//        onCancelCoroutine()
//
//        requestPageIndex = statusItem.requestPageIndex
//        currentPageIndex = statusItem.currentPageIndex
//
//        dslAdapter =
//        baseAdapter = dslAdapter
//        baseDslAdapter = dslAdapter

        val dslAdapter = statusItem.dslAdapter
        rightRecyclerView?.adapter = dslAdapter

        if (dslAdapter.getValidFilterDataList().isEmpty()) {
            //onBaseRefresh(null)
            dslAdapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
        }
    }

    open fun onLoadLeftNetData(loadCallback: (status: MutableList<StatusItem>) -> Unit = {}) {

    }
}

data class StatusItem(
    //显示的文本
    var statusText: CharSequence,
    //显示的提示数量
    var statusCount: Int = -1,

    //额外扩展数据
    var statusData: Any? = null,

    var dslAdapter: DslAdapter = DslAdapter(),

    //分页参数
    var currentPageIndex: Int = 1,
    var requestPageIndex: Int = 1
)