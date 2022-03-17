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
import com.angcyo.library.ex.isListEmpty
import com.angcyo.library.model.Page
import com.angcyo.widget.recycler.initDsl

/**
 * 左右双RV, 状态切换表单界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/11/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class BaseStatusFragment : BaseDslFragment() {

    companion object {
        const val LEFT_SELECT_INDEX = "left_select_index"
    }

    /**左边默认选中第几项*/
    var leftDefaultSelectIndex = 0

    /**状态映射*/
    val statusList = mutableListOf<StatusItem>()

    /**当前选中的状态*/
    var currentStatus: StatusItem? = null

    val leftDslAdapter = DslAdapter()

    var leftRecyclerView: RecyclerView? = null

    init {
        contentLayoutId = R.layout.lib_status_content_fragment
    }

    open fun needLoadLeftNetData(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        leftDefaultSelectIndex = getData(LEFT_SELECT_INDEX) ?: leftDefaultSelectIndex
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        leftRecyclerView = _vh.rv(R.id.left_recycler_view)

        leftRecyclerView?.initDsl()

        if (needLoadLeftNetData()) {
            //延迟请求左边的网络数据
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
            if (leftDefaultSelectIndex < statusList.size) {
                it.itemSelectorHelper.selector(leftDefaultSelectIndex)
            } else {
                it.itemSelectorHelper.selector(0)
            }
        }
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
                itemUpdateFlag = true
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

        page = statusItem.page

        val dslAdapter = statusItem.dslAdapter
        _recycler.adapter = dslAdapter

        //刷新 or 加载监听
        dslAdapter.onRefreshOrLoadMore { _, loadMore ->
            if (loadMore) {
                onLoadMore()
            } else {
                onRefresh(null)
            }
        }

        if (dslAdapter.getValidFilterDataList().isEmpty() || dslAdapter.isAdapterStatus()) {
            dslAdapter.setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
        }
    }

    //<editor-fold desc="数据请求处理">

    override fun onRefresh(refreshContentBehavior: IRefreshContentBehavior?) {
        if (needLoadLeftNetData() && leftDslAdapter.itemCount <= 0) {
            //左边的数据 还未初始化好.
            onLoadLeftNetData {
                statusList.clear()
                statusList.addAll(it)
                _initLeftRecyclerView()
            }
        } else {
            super.onRefresh(refreshContentBehavior)
        }
    }

    /**左边数据加载完成之后回调此方法*/
    fun <Bean> loadLeftDataEnd(
        dataList: List<Bean>?,
        error: Throwable? = null,
        action: (statusList: MutableList<StatusItem>, bean: Bean) -> Unit
    ) {
        if (dataList.isListEmpty()) {
            //如果左边数据为空, 则使用右边的adapter, 显示情感图状态 (空状态/错误状态)
            loadDataEnd(DslAdapterItem::class.java, listOf<String>(), error)
        } else {
            //否则左边有数据
            finishRefresh()
            statusList.clear()
            dataList?.forEach {
                action(statusList, it)
            }
            _initLeftRecyclerView()
        }
    }

    /**重写此方法, 加载左边的数据集合*/
    open fun onLoadLeftNetData(loadCallback: (status: MutableList<StatusItem>) -> Unit = {}) {
        //no op
    }

    //</editor-fold desc="数据请求处理">
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
    var page: Page = Page()
)

/**快速获取[StatusItem]*/
fun Any.ofStatusItem(statusText: CharSequence, statusCount: Int = -1): StatusItem =
    StatusItem(statusText, statusCount, this)