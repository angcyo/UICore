package com.angcyo.coroutine

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/14
 */

open class LifecycleScope(
    lifecycleOwner: LifecycleOwner? = null,
    cancelLifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) : AndroidScope(lifecycleOwner, cancelLifeEvent) {

}