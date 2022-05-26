package com.angcyo.library.component

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/26
 */
open class ActivityLifecycleCallbacksAdapter : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        L.v(activity.simpleHash())
    }

    override fun onActivityStarted(activity: Activity) {
        L.v(activity.simpleHash())
    }

    override fun onActivityResumed(activity: Activity) {
        L.v(activity.simpleHash())
    }

    override fun onActivityPaused(activity: Activity) {
        L.v(activity.simpleHash())
    }

    override fun onActivityStopped(activity: Activity) {
        L.v(activity.simpleHash())
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        L.v(activity.simpleHash())
    }

    override fun onActivityDestroyed(activity: Activity) {
        L.v(activity.simpleHash())
    }

}