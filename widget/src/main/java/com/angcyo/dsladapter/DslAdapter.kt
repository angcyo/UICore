package com.angcyo.dsladapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.filter.IFilterInterceptor
import com.angcyo.dsladapter.internal.AdapterStatusFilterInterceptor
import com.angcyo.dsladapter.internal.LoadMoreFilterInterceptor
import com.angcyo.library.L
import com.angcyo.widget.DslViewHolder
import kotlin.math.min

/**
 * https://github.com/angcyo/DslAdapter
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslAdapter(dataItems: List<DslAdapterItem>? = null) :
    RecyclerView.Adapter<DslViewHolder>(), OnDispatchUpdatesListener {

    /**
     * 为了简单起见, 这里写死套路, 理论上应该用状态器管理的.
     * 2.0.0 版本更新之后, [dslAdapterStatusItem] [dslLoadMoreItem] 将在过滤数据源后加载追加,
     * 所以不能在[adapterItems]中找到.
     * @since 2.0.0
     * */
    var dslAdapterStatusItem = DslAdapterStatusItem()
    var dslLoadMoreItem = DslLoadMoreItem()

    /**[dslAdapterStatusItem] [dslLoadMoreItem] 功能的支持*/
    var adapterStatusIFilterInterceptor: IFilterInterceptor = AdapterStatusFilterInterceptor()
    var loadMoreIFilterInterceptor: IFilterInterceptor = LoadMoreFilterInterceptor()

    /**包含所有[DslAdapterItem], 包括 [headerItems] [dataItems] [footerItems]的数据源*/
    val adapterItems = mutableListOf<DslAdapterItem>()

    /**底部数据, 用来存放 [DslLoadMoreItem] */
    val footerItems = mutableListOf<DslAdapterItem>()

    /**头部数据*/
    val headerItems = mutableListOf<DslAdapterItem>()

    /**列表数据*/
    val dataItems = mutableListOf<DslAdapterItem>()

    /**数据过滤规则*/
    var dslDataFilter: DslDataFilter? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.apply {
                removeDispatchUpdatesListener(this@DslAdapter)
                beforeFilterInterceptorList.remove(adapterStatusIFilterInterceptor)
                afterFilterInterceptorList.remove(loadMoreIFilterInterceptor)
            }
            field = value
            field?.apply {
                addDispatchUpdatesListener(this@DslAdapter)
                beforeFilterInterceptorList.add(0, adapterStatusIFilterInterceptor)
                afterFilterInterceptorList.add(loadMoreIFilterInterceptor)
            }
            updateItemDepend()
        }

    /**单/多选助手*/
    val itemSelectorHelper = ItemSelectorHelper(this)

    /**
     * 一次性的[FilterParams], 使用完之后会被置空,调用无参[updateItemDepend]方法时使用.
     * */
    var onceFilterParams: FilterParams? = null

    /**默认的[FilterParams]*/
    var defaultFilterParams: FilterParams? = null
        get() {
            return onceFilterParams ?: (field ?: _defaultFilterParams())
        }

    /**
     * [Diff]更新数据后回调, 只会执行一次
     * */
    var onDispatchUpdatesAfterOnce: DispatchUpdates? = null

    val dispatchUpdatesAfterList = mutableListOf<DispatchUpdates>()
    val dispatchUpdatesAfterOnceList = mutableListOf<DispatchUpdates>()

    init {
        dslDataFilter = DslDataFilter(this)

        dataItems?.let {
            this.dataItems.clear()
            this.dataItems.addAll(dataItems)
            _updateAdapterItems()
            updateItemDepend(FilterParams(asyncDiff = false, justRun = true))
        }
    }

    //<editor-fold desc="生命周期方法">

    override fun getItemViewType(position: Int): Int {
        return getItemData(position)?.itemLayoutId ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DslViewHolder {
        if (viewType <= 0) {
            throw IllegalStateException("请检查是否未指定[itemLayoutId]")
        }
        //viewType, 就是布局的 Id, 这是设计核心原则.
        val dslViewHolder: DslViewHolder
        val itemView: View = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        dslViewHolder = DslViewHolder(itemView)
        return dslViewHolder
    }

    override fun getItemCount(): Int {
        return getValidFilterDataList().size
    }

    override fun onBindViewHolder(
        holder: DslViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        val dslItem = getItemData(position)
        dslItem?.itemDslAdapter = this
        dslItem?.itemBind?.invoke(holder, position, dslItem, payloads)
    }

    override fun onBindViewHolder(holder: DslViewHolder, position: Int) {
        //no op
    }

    var _recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        _recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        _recyclerView = null
    }

    override fun onViewAttachedToWindow(holder: DslViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.getDslAdapterItem()?.apply {
            holder.itemView.fullSpan(itemSpanCount == -1)
            itemViewAttachedToWindow.invoke(holder, holder.adapterPosition)
        }
    }

    override fun onViewDetachedFromWindow(holder: DslViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.getDslAdapterItem()?.apply {
            itemViewDetachedToWindow.invoke(holder, holder.adapterPosition)
        }
    }

    override fun onViewRecycled(holder: DslViewHolder) {
        super.onViewRecycled(holder)
        holder.getDslAdapterItem()?.apply {
            itemViewRecycled.invoke(holder, holder.adapterPosition)
        }
    }

    override fun onFailedToRecycleView(holder: DslViewHolder): Boolean {
        return super.onFailedToRecycleView(holder).apply {
            L.w("是否回收失败:$holder $this")
        }
    }

    /**返回[DslViewHolder]对应的[DslAdapterItem]*/
    fun DslViewHolder.getDslAdapterItem(): DslAdapterItem? {
        return when (adapterPosition) {
            in getValidFilterDataList().indices -> getItemData(adapterPosition)
            else -> null
        }
    }

    //</editor-fold desc="生命周期方法">

    //<editor-fold desc="其他方法">

    /**
     * [Diff]操作结束之后的通知事件
     * */
    override fun onDispatchUpdatesAfter(dslAdapter: DslAdapter) {
        onDispatchUpdatesAfterOnce?.invoke(dslAdapter)
        onDispatchUpdatesAfterOnce = null

        dispatchUpdatesAfterOnceList.forEach {
            it.invoke(dslAdapter)
        }

        dispatchUpdatesAfterOnceList.clear()

        dispatchUpdatesAfterList.forEach {
            it.invoke(dslAdapter)
        }
    }

    fun onDispatchUpdates(action: DispatchUpdates) {
        dispatchUpdatesAfterList.add(action)
    }

    fun onDispatchUpdatesOnce(action: DispatchUpdates) {
        dispatchUpdatesAfterOnceList.add(action)
    }

    //</editor-fold>

    //<editor-fold desc="辅助方法">

    fun _updateAdapterItems() {
        //整理数据
        adapterItems.clear()
        adapterItems.addAll(headerItems)
        adapterItems.addAll(dataItems)
        adapterItems.addAll(footerItems)
    }

    //</editor-fold desc="辅助方法">

    //<editor-fold desc="操作方法">

    /**
     * 适配器当前是情感图状态
     * */
    fun isAdapterStatus(): Boolean {
        return dslAdapterStatusItem.isInStateLayout()
    }

    /**
     * 设置[Adapter]需要显示情感图的状态
     * [DslAdapterStatusItem.ADAPTER_STATUS_NONE]
     * [DslAdapterStatusItem.ADAPTER_STATUS_EMPTY]
     * [DslAdapterStatusItem.ADAPTER_STATUS_LOADING]
     * [DslAdapterStatusItem.ADAPTER_STATUS_ERROR]
     * */
    fun setAdapterStatus(status: Int, filterParams: FilterParams = defaultFilterParams!!) {
        if (dslAdapterStatusItem.itemState == status) {
            return
        }
        dslAdapterStatusItem.itemChanging = true
        dslAdapterStatusItem.itemState = status
        updateItemDepend(filterParams)
    }

    /**自动设置状态*/
    fun autoAdapterStatus() {
        if (isAdapterStatus()) {
            //no op
        } else {
            val emptyCount = 0
            if (adapterItems.size <= emptyCount) {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
            } else {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
            }
        }
    }

    fun setAdapterStatusEnable(
        enable: Boolean = true,
        filterParams: FilterParams = defaultFilterParams!!
    ) {
        val old = dslAdapterStatusItem.itemStateEnable
        if (old == enable) {
            return
        }
        dslAdapterStatusItem.itemStateEnable = enable

        updateItemDepend(filterParams)
    }

    fun setLoadMoreEnable(
        enable: Boolean = true,
        filterParams: FilterParams = defaultFilterParams!!
    ) {
        if (dslLoadMoreItem.itemStateEnable == enable) {
            return
        }
        dslLoadMoreItem.itemStateEnable = enable

        updateItemDepend(filterParams)
    }

    /**
     * [DslLoadMoreItem.LOAD_MORE_NORMAL]
     * [DslLoadMoreItem.LOAD_MORE_NO_MORE]
     * [DslLoadMoreItem.LOAD_MORE_ERROR]
     * */
    fun setLoadMore(status: Int, payload: Any? = null, notify: Boolean = true) {
        if (dslLoadMoreItem.itemStateEnable && dslLoadMoreItem.itemState == status) {
            return
        }
        dslLoadMoreItem.itemChanging = true
        dslLoadMoreItem.itemState = status
        if (notify) {
            notifyItemChanged(dslLoadMoreItem, payload)
        }
    }

    //<editor-fold desc="Item操作">

    /**
     * 在最后的位置插入数据
     */
    fun addLastItem(item: DslAdapterItem) {
        insertItem(-1, item)
    }

    fun addLastItem(item: List<DslAdapterItem>) {
        insertItem(-1, item)
    }

    //修正index
    fun _validIndex(list: List<*>, index: Int): Int {
        return if (index < 0) {
            list.size
        } else {
            min(index, list.size)
        }
    }

    /**插入数据列表*/
    fun insertItem(index: Int, list: List<DslAdapterItem>) {
        if (list.isEmpty()) {
            return
        }
        dataItems.addAll(_validIndex(dataItems, index), list)
        _updateAdapterItems()
        updateItemDepend()
    }

    /**插入数据列表*/
    fun insertItem(index: Int, item: DslAdapterItem) {
        dataItems.add(_validIndex(dataItems, index), item)
        _updateAdapterItems()
        updateItemDepend()
    }

    /**移除一组数据*/
    fun removeItem(list: List<DslAdapterItem>) {
        val listInclude = mutableListOf<DslAdapterItem>()

        list.filterTo(listInclude) {
            dataItems.contains(it)
        }

        if (dataItems.removeAll(listInclude)) {
            _updateAdapterItems()
            updateItemDepend()
        }
    }

    /**移除数据*/
    fun removeItem(item: DslAdapterItem) {
        if (dataItems.remove(item)) {
            _updateAdapterItems()
            updateItemDepend()
        }
    }

    /**重置数据列表*/
    fun resetItem(list: List<DslAdapterItem>) {
        dataItems.clear()
        dataItems.addAll(list)
        _updateAdapterItems()
        updateItemDepend()
    }

    /**清理数据列表, 但不刷新界面*/
    fun clearItems() {
        dataItems.clear()
        _updateAdapterItems()
    }

    fun clearHeaderItems() {
        headerItems.clear()
        _updateAdapterItems()
    }

    fun clearFooterItems() {
        footerItems.clear()
        _updateAdapterItems()
    }

    fun clearAllItems() {
        headerItems.clear()
        dataItems.clear()
        footerItems.clear()
        _updateAdapterItems()
    }

    /**可以在回调中改变数据, 并且会自动刷新界面*/
    fun changeItems(filterParams: FilterParams = defaultFilterParams!!, change: () -> Unit) {
        change()
        _updateAdapterItems()
        updateItemDepend(filterParams)
    }

    fun changeDataItems(
        filterParams: FilterParams = defaultFilterParams!!,
        change: (dataItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        changeItems(filterParams) {
            change(dataItems)
        }
    }

    fun changeHeaderItems(
        filterParams: FilterParams = defaultFilterParams!!,
        change: (headerItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        changeItems(filterParams) {
            change(headerItems)
        }
    }

    fun changeFooterItems(
        filterParams: FilterParams = defaultFilterParams!!,
        change: (footerItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        changeItems(filterParams) {
            change(footerItems)
        }
    }

    fun renderHeader(
        filterParams: FilterParams = defaultFilterParams!!,
        render: DslAdapter.(headerItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        val dslAdapter = DslAdapter()
        dslAdapter.dslDataFilter = null
        dslAdapter.render(headerItems)
        changeItems(filterParams) {
            headerItems.addAll(dslAdapter.adapterItems)
        }
    }

    fun renderData(
        filterParams: FilterParams = defaultFilterParams!!,
        render: DslAdapter.(dataItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        val dslAdapter = DslAdapter()
        dslAdapter.dslDataFilter = null
        dslAdapter.render(dataItems)
        changeItems(filterParams) {
            dataItems.addAll(dslAdapter.adapterItems)
        }
    }

    fun renderFooter(
        filterParams: FilterParams = defaultFilterParams!!,
        render: DslAdapter.(footerItems: MutableList<DslAdapterItem>) -> Unit
    ) {
        val dslAdapter = DslAdapter()
        dslAdapter.dslDataFilter = null
        dslAdapter.render(footerItems)
        changeItems(filterParams) {
            footerItems.addAll(dslAdapter.adapterItems)
        }
    }

    //</editor-fold desc="Item操作">

    /**获取有效过滤后的数据集合*/
    fun getValidFilterDataList(): List<DslAdapterItem> {
        return dslDataFilter?.filterDataList ?: adapterItems
    }

    fun getItemData(position: Int, useFilterList: Boolean = true): DslAdapterItem? {
        val list = getDataList(useFilterList)
        return if (position in list.indices) {
            list[position]
        } else {
            null
        }
    }

    /**获取数据列表*/
    fun getDataList(useFilterList: Boolean = true): List<DslAdapterItem> {
        return if (useFilterList) getValidFilterDataList() else adapterItems
    }

    /**创建默认的[FilterParams]*/
    fun _defaultFilterParams(): FilterParams {
        return FilterParams()
    }

    /**调用[DiffUtil]更新界面*/
    fun updateItemDepend(filterParams: FilterParams = defaultFilterParams!!) {
        dslDataFilter?.let {
            it.updateFilterItemDepend(filterParams)

            if (filterParams == onceFilterParams) {
                onceFilterParams = null
            }
        }
    }

    /**刷新某一个item, 支持过滤数据源*/
    fun notifyItemChanged(
        item: DslAdapterItem?,
        payload: Any? = null,
        useFilterList: Boolean = true
    ) {
        if (item == null) {
            return
        }
        val list = getDataList(useFilterList)
        val indexOf = list.indexOf(item)

        if (indexOf in list.indices) {
            notifyItemChangedPayload(indexOf, payload)
        }
    }

    /**更新界面上所有[DslAdapterItem]*/
    fun updateAllItem(payload: Any? = DslAdapterItem.PAYLOAD_UPDATE_PART) {
        notifyItemRangeChanged(0, itemCount, payload)
    }

    /**更新一批[DslAdapterItem]*/
    fun updateItems(
        list: Iterable<DslAdapterItem>,
        payload: Any? = null,
        useFilterList: Boolean = true
    ) {
        getDataList(useFilterList).apply {
            for (item in list) {
                val indexOf = indexOf(item)

                if (indexOf in this.indices) {
                    notifyItemChangedPayload(indexOf, payload)
                }
            }
        }
    }

    fun notifyItemChangedPayload(position: Int, payloads: Any? = null) {
//        if (payloads is Iterable<*>) {
//            for (payload in payloads) {
//                //为了避免多次触发[onChanged]事件, 取消此方式
//                notifyItemChanged(position, payload)
//            }
//        } else {
//            notifyItemChanged(position, payloads)
//        }
        notifyItemChanged(position, payloads)
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="操作符重载">

    /**
     * <pre>
     *  DslDemoItem()(){}
     * </pre>
     * */
    operator fun <T : DslAdapterItem> T.invoke(config: T.() -> Unit = {}) {
        this.config()
        addLastItem(this)
    }

    /**
     * <pre>
     * this + DslAdapterItem()
     * </pre>
     * */
    operator fun <T : DslAdapterItem> plus(item: T): DslAdapter {
        addLastItem(item)
        return this
    }

    operator fun <T : DslAdapterItem> plus(list: List<T>): DslAdapter {
        addLastItem(list)
        return this
    }

    /**
     * <pre>
     * this - DslAdapterItem()
     * </pre>
     * */
    operator fun <T : DslAdapterItem> minus(item: T): DslAdapter {
        removeItem(item)
        return this
    }

    operator fun <T : DslAdapterItem> minus(list: List<T>): DslAdapter {
        removeItem(list)
        return this
    }

    /**
     * ```
     * this[1]
     * this[index]
     * this[index, false]
     *
     * 负数表示倒数
     * ```
     * */
    operator fun get(
        index: Int,
        useFilterList: Boolean = true,
        reverse: Boolean = true //是否开启反序, 倒数
    ): DslAdapterItem? {
        return getDataList(useFilterList).run {
            if (index >= 0 || !reverse)
                getOrNull(index)
            else
                getOrNull(size + index)
        }
    }

    /**
     * ```
     * this["tag"]
     * this["tag", false]
     * ```
     * */
    operator fun get(tag: String?, useFilterList: Boolean = true): DslAdapterItem? {
        return tag?.run { findItemByTag(tag, useFilterList) }
    }

    //</editor-fold desc="操作符重载">
}