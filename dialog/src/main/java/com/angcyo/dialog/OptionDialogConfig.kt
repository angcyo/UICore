package com.angcyo.dialog

import android.app.Dialog
import android.widget.TextView
import androidx.collection.ArrayMap
import com.angcyo.component.DslAffect
import com.angcyo.component.dslAffect
import com.angcyo.dialog.dslitem.DslOptionItem
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.loadSingleData2
import com.angcyo.dsladapter.singleModel
import com.angcyo.library.L
import com.angcyo.library.ex.string
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.find
import com.angcyo.widget.base.resetChild
import com.angcyo.widget.recycler.initDslAdapter
import com.angcyo.widget.tab

/**
 * 万级联动选择对话框
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/12
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class OptionDialogConfig : BaseDialogConfig() {

    /**已选中的选项*/
    var optionList = mutableListOf<Any>()

    /**是否需要异步加载数据, true 会开启[DslAffect]*/
    var optionNeedAsyncLoad = true

    /**任意选择, 不强制要求选择到最后一级*/
    var optionAnySelector = false

    /**当加载返回空数据时, 是否自动结束无限操作, 否则会提示空数据*/
    var optionEndOfEmpty = true

    /**
     * 根据级别, 加载级别对应的选项列表
     * @param _dslAffect 情感图切换
     * @param options 之前选中的选项
     * @param loadLevel 当前需要请求级别, 从0开始
     * */
    var onLoadOptionList: (
        options: MutableList<Any>,
        loadLevel: Int,
        itemsCallback: (MutableList<out Any>?) -> Unit,
        errorCallback: (Throwable?) -> Unit
    ) -> Unit =
        { options, loadLevel, itemsCallback, errorCallback ->

        }

    /**是否没有下一级可以选择了*/
    var onCheckOptionEnd: (options: MutableList<Any>, loadLevel: Int) -> Boolean = { options, _ ->
        false
    }

    /**
     * 选项返回回调
     * 返回 true, 则不会自动 调用 dismiss
     * */
    var onOptionResult: (dialog: Dialog, options: MutableList<Any>) -> Boolean = { _, _ ->
        false
    }

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var onOptionItemToString: (item: Any) -> CharSequence = { item ->
        item.string()
    }

    /**Tab中的[Item]是否和列表中的[Item]相同*/
    var isOptionEquItem: (option: Any, item: Any) -> Boolean =
        { option, item -> onOptionItemToString(option) == onOptionItemToString(item) }

    init {
        dialogLayoutId = R.layout.lib_dialog_option_layout

        positiveButtonListener = { dialog, _ ->
            if (onOptionResult.invoke(dialog, optionList)) {
                //被拦截
            } else {
                dialog.dismiss()
            }
        }
    }

    /**当前查看的选项级别*/
    var _selectorLevel = -1

    /**需要加载数据的级别*/
    var _loadLevel = 0

    //_loadLevel 对应的 缓存
    var _cacheMap = ArrayMap<Int, MutableList<out Any>>()

    //是否需要使用缓存, 建议开启.
    var optionEnableCache: Boolean = true

    lateinit var _dslAffect: DslAffect
    lateinit var _adapter: DslAdapter

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //列表
        dialogViewHolder.rv(R.id.lib_recycler_view)?.apply {
            _adapter = initDslAdapter()
            _adapter.singleModel()
        }

        //情感图
        dialogViewHolder.group(R.id.content_wrap_layout)?.apply {
            _dslAffect = dslAffect(this) {
                contentAffect = DslAffect.CONTENT_AFFECT_NONE
                onAffectChange = { dslAffect, from, to, fromView, toView, data ->
                    toView.isClickable = to == DslAffect.AFFECT_LOADING
                }
            }
        }

        //tab控制
        dialogViewHolder.tab(R.id.lib_tab_layout)?.apply {
            configTabLayoutConfig {
                onSelectIndexChange = { fromIndex, selectIndexList, reselect ->
                    val toIndex = selectIndexList.last()
                    _loadOptionList(toIndex, dialogViewHolder)
                }
            }
        }

        _loadOptionList(optionList.size, dialogViewHolder)
    }

    /**重置tab*/
    fun _resetTabTo(
        dialogViewHolder: DslViewHolder,
        isEnd: Boolean = false,
        scrollTo: Boolean = false
    ) {
        val tabs = mutableListOf<Any>()
        tabs.addAll(optionList)

        if (!isEnd) {
            tabs.add("请选择")
        }

        dialogViewHolder.tab(R.id.lib_tab_layout)
            ?.apply {
                resetChild(tabs.size, R.layout.lib_tab_option_item_layout) { itemView, itemIndex ->
                    itemView.find<TextView>(R.id.lib_text_view)?.text =
                        tabs.getOrNull(itemIndex)?.run(onOptionItemToString)
                }
                if (scrollTo) {
                    post {
                        setCurrentItem(tabs.lastIndex)
                    }
                }
            }

        //确定按钮状态
        dialogViewHolder.enable(R.id.positive_button, optionAnySelector || isEnd)
    }

    /**加载选项数据, 并且重置Tab*/
    fun _loadOptionList(loadLevel: Int, dialogViewHolder: DslViewHolder) {
        L.i("加载level:$loadLevel")

        //获取数据后的回调处理
        val onItemListCallback: (MutableList<out Any>?) -> Unit = {
            if (it.isNullOrEmpty()) {
                if (optionEndOfEmpty) {
                    _resetTabTo(dialogViewHolder, true)
                } else {
                    _dslAffect.showAffect(DslAffect.AFFECT_EMPTY)
                    _adapter.loadSingleData2<DslOptionItem>(null)
                }
            } else {
                _dslAffect.showAffect(DslAffect.AFFECT_CONTENT)
                _adapter.loadSingleData2<DslOptionItem>(it, 1, Int.MAX_VALUE) { data ->
                    itemIsSelected = optionList.getOrNull(loadLevel)?.run {
                        isOptionEquItem(this, data)
                    } ?: false

                    itemOptionText = onOptionItemToString(data)

                    onItemClick = {
                        if (loadLevel == optionList.size) {
                            //当前选择界面数据, 是最后级别的

                            optionList.add(data)
                        } else {
                            //清除之后的选项
                            for (i in optionList.size - 1 downTo loadLevel) {
                                _cacheMap.remove(i)
                                optionList.removeAt(i)
                            }
                            optionList.add(data)
                        }

                        if (onCheckOptionEnd(optionList, loadLevel)) {
                            //最后一级
                            _resetTabTo(dialogViewHolder, true)
                            updateItemSelector(true)
                        } else {
                            _resetTabTo(dialogViewHolder, false)

                            _loadOptionList(loadLevel + 1, dialogViewHolder)
                        }
                    }
                }
            }

            it?.let {
                //缓存
                _cacheMap[loadLevel] = it

                //滚动到目标位置
                optionList.getOrNull(loadLevel)?.apply {
                    //已经选中的item
                    it.forEachIndexed { index, any ->
                        if (isOptionEquItem(any, this)) {
                            dialogViewHolder.rv(R.id.lib_recycler_view)
                                ?.smoothScrollToPosition(index)
                        }
                    }
                }
            }
        }

        //错误回调的处理
        val onErrorCallback: (Throwable?) -> Unit = {
//            selectorLevel = loadLevel
//            adapter.resetData(emptyList())
//
//            if (selectorLevel >= optionList.size) {
//                resetTabToLevel(dialogViewHolder, selectorLevel)
//            }
            if (optionNeedAsyncLoad) {
                _dslAffect.showAffect(DslAffect.AFFECT_ERROR)
            }
        }

        if (onCheckOptionEnd(optionList, loadLevel)) {
            //请求的加载已是最后
            _resetTabTo(dialogViewHolder, true)
        } else {
            _resetTabTo(dialogViewHolder, false)
            val cacheList = _cacheMap[loadLevel]
            if (!optionEnableCache || cacheList.isNullOrEmpty()) {
                //关闭缓存 或者 无缓存
                if (optionNeedAsyncLoad) {
                    _dslAffect.showAffect(DslAffect.AFFECT_LOADING)
                }
                onLoadOptionList(optionList, loadLevel, onItemListCallback, onErrorCallback)
            } else {
                //有缓存
                onItemListCallback(cacheList)
            }
        }
    }
}