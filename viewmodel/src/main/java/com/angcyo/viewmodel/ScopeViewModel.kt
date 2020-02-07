package com.angcyo.viewmodel

import androidx.lifecycle.ViewModel
import com.angcyo.coroutine.AndroidScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */
open class ScopeViewModel : ViewModel() {

    var scope: CoroutineScope = AndroidScope()
        get() {
            return if (field.isActive) {
                field
            } else {
                AndroidScope()
            }
        }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    /**启动协程*/
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(block = block)
    }
}