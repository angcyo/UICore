package com.angcyo.core.component.model

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.angcyo.core.R
import com.angcyo.core.vmApp
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.hawk.HawkPropertyValue
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex._color
import com.angcyo.library.ex.isDarkMode
import com.angcyo.library.ex.setTintList
import com.angcyo.library.ex.tintDrawable

/**
 *
 * 暗色主题配置
 *
 * 深色主题
 *
 * https://developer.android.com/guide/topics/ui/look-and-feel/darktheme?hl=zh-cn
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/15
 */
class NightModel : ViewModel() {

    /**是否激活暗色模式功能
     * 激活之后[nightMode]属性才有效*/
    @delegate:Keep
    var enableNightModel: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**设置的暗色模式*/
    @delegate:Keep
    var nightMode: Int by HawkPropertyValue<Any, Int>(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    /**是否是暗色模式/深色模式*/
    val isDarkMode: Boolean
        get() = if (enableNightModel) isDarkMode(lastContext) else false  //isDarkMode()

    /**暗黑模式下的图标tint颜色*/
    var darkIcoTintColor: Int = _color(R.color.lib_theme_icon_color)

    private val _nightMode: Int
        get() = if (enableNightModel) {
            nightMode
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

    private val nightActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is AppCompatActivity) {
                activity.delegate.localNightMode = _nightMode
            }
        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            //L.i("onActivityResumed")
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            //L.i("onActivitySaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {

        }
    }

    @CallPoint
    fun onCreate(application: Application) {
        //app().resources.configuration.uiMode
        AppCompatDelegate.setDefaultNightMode(_nightMode)
        application.registerActivityLifecycleCallbacks(nightActivityLifecycleCallbacks)
    }

    @CallPoint
    fun onConfigurationChanged(activity: Activity, newConfig: Configuration) {
        //activity.window.decorView.isForceDarkAllowed = false
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        L.i("NightModel currentNightMode:$currentNightMode")
        when (currentNightMode) {
            // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_NO -> { //16

            }
            // Night mode is active, we're using dark theme
            Configuration.UI_MODE_NIGHT_YES -> { //32

            }
        }
    }

    /**图标着色, 只在暗黑模式下, 不改变原来的颜色*/
    fun tintImageViewNight(imageView: ImageView?): ImageView? {
        if (isDarkMode) {
            imageView?.setTintList(darkIcoTintColor)
        }
        return imageView
    }

    /**Drawable着色, 只在暗黑模式下, 不改变原来的颜色*/
    fun tintDrawableNight(drawable: Drawable?): Drawable? {
        if (isDarkMode) {
            return drawable?.tintDrawable(darkIcoTintColor)
        }
        return drawable
    }
}

/**[com.angcyo.core.component.model.NightModel.tintImageViewNight]*/
fun ImageView.tintImageViewNight(): ImageView? {
    return vmApp<NightModel>().tintImageViewNight(this)
}

/**[com.angcyo.core.component.model.NightModel.tintDrawableNight]*/
fun Drawable.tintDrawableNight(): Drawable? {
    return vmApp<NightModel>().tintDrawableNight(this)
}

