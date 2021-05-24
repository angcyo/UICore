package com.angcyo.component

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.view.forEach
import com.angcyo.component.HawkViewHolderFactory.Companion.hawkViewHolderFactory
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.string
import com.angcyo.widget.edit.AutoCompleteEditText

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

open class HawkViewHolderFactory : HawkFactory {

    /**是否启用Id当key, 否则请将key放在[R.id.lib_tag_hawk]的tag中*/
    var enableIdKey = true

    /**通过View的id生成的key的前缀*/
    var idKeyPrefix = ""

    /**保存选择状态*/
    var enableSelectedState = true

    /**保存激活状态*/
    var enableEnabledState = true

    companion object {
        val hawkViewHolderFactory = HawkViewHolderFactory()
    }

    override fun generateKey(view: View): String? {
        var key: String? = null
        val tag = view.getTag(R.id.lib_tag_hawk)
        if (tag is String) {
            if (tag.isNotBlank()) {
                key = tag
            }
        } else if (enableIdKey) {
            val viewId = view.id
            if (viewId != View.NO_ID) {
                key = "$idKeyPrefix$viewId"
            }
        }

        return key
    }

    override fun onInstall(view: View) {
        val key = generateKey(view)
        key?.apply {
            when (view) {
                is AutoCompleteEditText -> {
                    view.onItemDeleteClick = { adapter, position ->
                        adapter.remove(position)

                        generateKey(view)?.run {
                            hawkPutList("${adapter.dataList.connect(",")}")
                        }

                        view.setDataList(
                            adapter.dataList,
                            view.autoCompleteShowOnFocus,
                            view.autoCompleteFocusDelay
                        )

                        //view.dismissDropDown()
                    }
                    view.onTextChange(shakeDelay = 1000) {
                        onSaveView(view)
                    }
                }
                is EditText -> view.onTextChange {
                    onSaveView(view)
                }
                is CompoundButton -> view.setOnCheckedChangeListener { _, _ ->
                    onSaveView(view)
                }
                is AdapterView<*> -> view.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            onSaveView(parent)
                        }
                    }
            }
        }
        if (view is ViewGroup) {
            view.forEach {
                onInstall(it)
            }
        }
    }

    override fun onSaveView(view: View) {
        val key = generateKey(view)
        key?.apply {
            when (view) {
                is AutoCompleteEditText -> key.hawkPutList(view.string())
                is EditText -> key.hawkPut(view.string(false))
                is CompoundButton -> key.hawkPut(if (view.isChecked) "1" else "0")
                is AdapterView<*> -> key.hawkPut("${view.selectedItemPosition}")
                else -> {
                    if (enableSelectedState) {
                        "${key}_selected".hawkPut(if (view.isSelected) "1" else "0")
                    }
                    if (enableEnabledState) {
                        "${key}_enabled".hawkPut(if (view.isEnabled) "1" else "0")
                    }
                }
            }
        }
        if (view is ViewGroup) {
            view.forEach {
                onSaveView(it)
            }
        }
    }

    override fun onRestoreView(view: View) {
        val key = generateKey(view)
        key?.apply {
            when (view) {
                is AutoCompleteEditText -> key.hawkGetList().run {
                    view.setDataList(this)
                    firstOrNull()?.run { view.setInputText(this) }
                }
                is EditText -> key.hawkGet()?.run { view.setInputText(this) }
                is CompoundButton -> key.hawkGet()?.run { view.isChecked = this == "1" }
                is AdapterView<*> -> key.hawkGet()?.toIntOrNull()?.run { view.setSelection(this) }
                else -> {
                    if (enableSelectedState) {
                        "${key}_selected".hawkGet()?.run { view.isSelected = this == "1" }
                    }
                    if (enableEnabledState) {
                        "${key}_enabled".hawkGet()?.run { view.isEnabled = this == "1" }
                    }
                }
            }
        }
        if (view is ViewGroup) {
            view.forEach {
                onRestoreView(it)
            }
        }
    }
}

interface HawkFactory {
    /**构建用于hawk存储的key值*/
    fun generateKey(view: View): String?

    /**自动识别安装对应的view, 监听对应的值变化*/
    fun onInstall(view: View)

    /**保存view对应的值*/
    fun onSaveView(view: View)

    /**恢复view对应的值*/
    fun onRestoreView(view: View)
}

fun DslViewHolder.hawkInstallAndRestore(
    idKeyPrefix: String = "",
    factory: HawkFactory = hawkViewHolderFactory
) {
    (factory as? HawkViewHolderFactory)?.idKeyPrefix = idKeyPrefix
    factory.onInstall(itemView)
    factory.onRestoreView(itemView)
}

fun DslViewHolder.hawkInstall(
    idKeyPrefix: String = "",
    factory: HawkFactory = hawkViewHolderFactory
) {
    (factory as? HawkViewHolderFactory)?.idKeyPrefix = idKeyPrefix
    factory.onInstall(itemView)
}

fun DslViewHolder.hawkSave(
    idKeyPrefix: String = "",
    factory: HawkFactory = hawkViewHolderFactory
) {
    (factory as? HawkViewHolderFactory)?.idKeyPrefix = idKeyPrefix
    factory.onSaveView(itemView)
}

fun DslViewHolder.hawkRestore(
    idKeyPrefix: String = "",
    factory: HawkFactory = hawkViewHolderFactory
) {
    (factory as? HawkViewHolderFactory)?.idKeyPrefix = idKeyPrefix
    factory.onRestoreView(itemView)
}