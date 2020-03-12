package com.angcyo.dialog

import android.app.Dialog
import androidx.collection.ArrayMap
import com.angcyo.component.DslAffect
import com.angcyo.component.dslAffect
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.widget.DslViewHolder
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

    /**
     * 根据级别, 加载级别对应的选项列表
     * @param affectUI 情感图切换
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
        if (item is CharSequence) {
            item
        } else {
            item.toString()
        }
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
    var enableCache: Boolean = true

    lateinit var _dslAffect: DslAffect
    lateinit var _adapter: DslAdapter

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        //列表
        dialogViewHolder.rv(R.id.lib_recycler_view)?.apply {
            _adapter = initDslAdapter()
        }

        //情感图
        dialogViewHolder.group(R.id.content_wrap_layout)?.apply {
            _dslAffect = dslAffect(this) {
                onAffectChange = { dslAffect, from, to, fromView, toView, data ->
                    toView.isClickable = true
                }
            }
        }

        //tab控制
        dialogViewHolder.tab(R.id.lib_tab_layout)?.apply {
            configTabLayoutConfig {
                onSelectIndexChange = { fromIndex, selectIndexList, reselect ->
                    val toIndex = selectIndexList.last()

                }
            }
        }

        //确定按钮状态
        dialogViewHolder.enable(R.id.positive_button, optionAnySelector)


    }
}