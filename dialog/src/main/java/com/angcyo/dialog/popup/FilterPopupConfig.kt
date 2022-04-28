package com.angcyo.dialog.popup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.angcyo.component.DslAffect
import com.angcyo.component.initWith
import com.angcyo.component.toContent
import com.angcyo.component.toLoading
import com.angcyo.dialog.FullPopupConfig
import com.angcyo.dialog.R
import com.angcyo.library.ex.*
import com.angcyo.library.extend.IToText
import com.angcyo.tablayout.dslSelector
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*

/**
 * 全屏的筛选过滤的popup
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/14
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FilterPopupConfig : FullPopupConfig() {

    /**需要填充的Filter布局*/
    var filterGroupLayout: Int = R.layout.lib_popup_filter_group_layout

    /**过滤中具体选择项的布局*/
    var filterItemLayout: Int = R.layout.lib_popup_filter_item

    /**数据*/
    val filterGroupList = mutableListOf<FilterGroup>()

    val dslAffect = DslAffect()

    /**选中改变之后回调*/
    var onFilterChanged: (filterGroupList: List<FilterGroup>, fromUser: Boolean) -> Unit = { _, _ ->

    }

    /**返回回调*/
    var onFilterResult: (container: PopupWindow, filterDataList: List<List<Any>>) -> Boolean =
        { _, _ ->
            false
        }

    init {
        layoutId = R.layout.lib_popup_filter_layout
    }

    override fun initLayout(window: Any, viewHolder: DslViewHolder) {
        super.initLayout(window, viewHolder)

        if (filterGroupList.isEmpty()) {
            dslAffect.initWith(viewHolder.group(R.id.lib_container_wrap_layout))
            dslAffect.toLoading()
        } else {
            render()
        }
    }

    /**渲染界面*/
    fun render() {
        dslAffect.toContent()
        contentView?.dslViewHolder()?.apply {
            group(R.id.lib_container_layout)?.apply {
                removeAllViews()

                //group
                filterGroupList.forEachIndexed { index, group ->
                    inflate(group.groupLayoutId ?: filterGroupLayout).let { groupView ->
                        groupView.find<TextView>(R.id.filter_group_name_view)?.text =
                            group.groupName

                        //填充子项
                        groupView.find<ViewGroup>(R.id.filter_group_item_wrap_layout)?.apply {
                            //item
                            group.groupFilterItemList?.forEach { groupItem ->

                                groupItem._groupIndex = index

                                inflate(
                                    groupItem.itemLayoutId ?: filterItemLayout
                                ).let { itemView ->
                                    //tag save
                                    itemView.setTag(R.id.lib_tag_value, groupItem)

                                    val itemData = groupItem.itemData

                                    itemView.find<TextView>(R.id.lib_text_view)?.text =
                                        when (itemData) {
                                            is IToText -> itemData.toText()
                                            else -> itemData?.str()
                                        }

                                    itemView.selected(groupItem.isSelected)
                                }
                            }

                            //选择器
                            dslSelector(this) {
                                dslMultiMode = true // 多选
                                onStyleItemView = { itemView, index, select ->
                                    itemView.selected(select)
                                }
                                onSelectItemView = { itemView, index, select, fromUser ->
                                    if (itemView.isFilterAll()) {
                                        if (select) {
                                            //选中所有之后, 取消其他项的选中
                                            selectedViewList(groupView).forEach {
                                                it.selected(false)
                                            }
                                        }
                                    }
                                    false
                                }
                                onSelectViewChange =
                                    { fromView, selectViewList, reselect, fromUser ->
                                        onSelectViewChange(groupView, fromUser)
                                    }
                            }
                        }
                    }
                }
            }

            //重置
            click(R.id.reset_button) {
                filterGroupList.forEach { group ->
                    /*val haveFilterAll =
                        it.groupFilterItemList?.find { it is FilterGroupItemAll } != null

                    if (haveFilterAll) {
                        //如果有全部按钮, 则只选中全部
                        it.groupFilterItemList?.forEach {
                            it.isSelected = it is FilterGroupItemAll
                        }
                    } else {
                        //如果没有, 则取消全部选项
                        it.groupFilterItemList?.forEach {
                            it.isSelected = false
                        }
                    }*/

                    group.groupFilterItemList?.forEach {
                        it.isSelected = it is FilterGroupItemAll
                        group._groupSelectAll = group._groupSelectAll || it.isSelected
                    }
                }

                render()
            }
            //确认
            click(R.id.save_button) {
                _container?.let {
                    if (it is PopupWindow) {
                        if (onFilterResult(it, selectedList())) {
                            //被拦截
                        } else {
                            //默认处理
                            it.dismiss()
                        }
                    }
                }
            }
        }
    }

    /**[全部]按钮*/
    fun View.isFilterAll(): Boolean = getTag(R.id.lib_tag_value) is FilterGroupItemAll

    fun View.selected(selected: Boolean) {
        isSelected = selected
        //选中的图标提示
        find<View>(R.id.lib_image_tip_view)?.visible(selected)
    }

    /**
     * 添加一组过滤
     * [needSelectAll] 是否需要显示[全部], 选中全部, 取消其他, 选中任意一个, 取消全部选中
     * */
    fun addFilterGroup(
        groupName: CharSequence?,
        itemList: List<Any>?,
        needSelectAll: Boolean = false,
        init: FilterGroup.() -> Unit = {}
    ) {
        filterGroupList.add(FilterGroup().apply {
            this.groupName = groupName
            val list = mutableListOf<FilterGroupItem>()
            if (needSelectAll) {
                list.add(FilterGroupItemAll())
            }
            itemList?.forEach {
                list.add(FilterGroupItem().apply {
                    itemData = it
                })
            }
            this.groupFilterItemList = list

            init()
        })
    }

    fun cancelFilterAllIfNeed(groupView: View) {
        val filterAllView =
            groupView.findView { it.getTag(R.id.lib_tag_value) is FilterGroupItemAll }
        filterAllView?.let {
            val otherSelectedViewList = selectedViewList(groupView)
            if (otherSelectedViewList.isNotEmpty()) {
                //选中了其他项
                filterAllView.selected(false)
            }
        }
    }

    /**当前分组的view,所有选中的view, 不包含[全部]*/
    fun selectedViewList(groupView: View, containAll: Boolean = false): List<View> {
        val itemWrapLayout = groupView.find<View>(R.id.filter_group_item_wrap_layout)
        val result = mutableListOf<View>()
        if (itemWrapLayout is ViewGroup) {
            itemWrapLayout.forEach { index, child ->
                if (child.isSelected) {
                    if (child.getTag(R.id.lib_tag_value) is FilterGroupItemAll) {
                        if (containAll) {
                            result.add(child)
                        }
                    } else {
                        result.add(child)
                    }
                }
            }
        }
        return result
    }

    /**勾选的结果*/
    fun selectedList(): List<List<Any>> {
        val result = mutableListOf<List<Any>>()

        contentView?.dslViewHolder()?.apply {
            group(R.id.lib_container_layout)?.forEach { index, child ->

                val itemList = mutableListOf<Any>()
                child.find<ViewGroup>(R.id.filter_group_item_wrap_layout)?.apply {
                    for (i in 0 until childCount) {
                        val childAt = getChildAt(i)
                        val tag = childAt.getTag(R.id.lib_tag_value)
                        if (tag is FilterGroupItem) {
                            tag.isSelected = childAt.isSelected
                            if (childAt.isSelected) {
                                if (tag is FilterGroupItemAll) {
                                    itemList.clear()

                                    //添加所有
                                    filterGroupList[index].groupFilterItemList?.forEach {
                                        if (it !is FilterGroupItemAll) {
                                            it.itemData?.let { itemList.add(it) }
                                        }
                                    }
                                } else {
                                    tag.itemData?.let { itemList.add(it) }
                                }
                            }
                        }
                    }
                }
                result.add(itemList)
            }
        }
        return result
    }

    /**勾选的结果*/
    fun selectedItemList(): List<FilterGroup> {
        val result = mutableListOf<FilterGroup>()

        contentView?.dslViewHolder()?.apply {
            group(R.id.lib_container_layout)?.forEach { index, child ->

                val group = filterGroupList[index]

                val groupItemList = mutableListOf<FilterGroupItem>()
                group._groupFilterSelectedItemList = groupItemList
                group._groupSelectAll = false

                child.find<ViewGroup>(R.id.filter_group_item_wrap_layout)?.apply {
                    for (i in 0 until childCount) {
                        val childAt = getChildAt(i)
                        val tag = childAt.getTag(R.id.lib_tag_value)
                        if (tag is FilterGroupItem) {
                            tag.isSelected = childAt.isSelected
                            if (childAt.isSelected) {
                                if (tag is FilterGroupItemAll) {
                                    group._groupSelectAll = true
                                    groupItemList.clear()

                                    //添加所有
                                    group.groupFilterItemList?.forEach {
                                        if (it !is FilterGroupItemAll) {
                                            groupItemList.add(it)
                                        }
                                    }
                                } else {
                                    groupItemList.add(tag)
                                }
                            }
                        }
                    }
                }

                result.add(group)
            }
        }

        return result
    }

    /**选中改变之后*/
    fun onSelectViewChange(groupView: View, fromUser: Boolean) {
        cancelFilterAllIfNeed(groupView)
        onFilterChanged(selectedItemList(), fromUser)
    }
}

/** 展示一个 过滤 popup window */
fun Context.filterPopupWindow(anchor: View?, config: FilterPopupConfig.() -> Unit): Any {
    val popupConfig = FilterPopupConfig()
    popupConfig.anchor = anchor

    /*popupConfig.apply {
        addFilterGroup("紧急程度", list, true)

        onFilterChanged = { filterGroupList, fromUser ->

        }
        onFilterResult = { container, filterDataList ->
            L.i(list)
            false
        }
    }*/

    popupConfig.config()
    return popupConfig.show(this)
}