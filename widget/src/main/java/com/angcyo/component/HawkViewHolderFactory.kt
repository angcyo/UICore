package com.angcyo.component

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.view.forEach
import com.angcyo.component.HawkViewHolderFactory.Companion.hawkViewHolderFactory
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.onTextChange
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.string

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

open class HawkViewHolderFactory : HawkFactory {

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
        } else {
            val viewId = view.id
            if (viewId != View.NO_ID) {
                key = "$viewId"
            }
        }

        return key
    }

    override fun onInstall(view: View) {
        val key = generateKey(view)
        key?.apply {
            when (view) {
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
        key?.run {
            when (view) {
                is EditText -> key.hawkPut(view.string(false))
                is CompoundButton -> key.hawkPut(if (view.isChecked) "1" else "0")
                is AdapterView<*> -> key.hawkPut("${view.selectedItemPosition}")
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
                is EditText -> key.hawkGet()?.run { view.setInputText(this) }
                is CompoundButton -> key.hawkGet()?.run { view.isChecked = this == "1" }
                is AdapterView<*> -> key.hawkGet()?.toIntOrNull()?.run { view.setSelection(this) }
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

    /**自动识别安装对应的view*/
    fun onInstall(view: View)

    /**保存view对应的值*/
    fun onSaveView(view: View)

    /**恢复view对应的值*/
    fun onRestoreView(view: View)
}

fun DslViewHolder.hawkInstall(factory: HawkFactory = hawkViewHolderFactory) {
    factory.onInstall(itemView)
}

fun DslViewHolder.hawkSave(factory: HawkFactory = hawkViewHolderFactory) {
    factory.onSaveView(itemView)
}

fun DslViewHolder.hawkRestore(factory: HawkFactory = hawkViewHolderFactory) {
    factory.onRestoreView(itemView)
}