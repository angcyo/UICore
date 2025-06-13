package com.angcyo.core.component.model

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.angcyo.core.vmApp
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.app
import com.angcyo.viewmodel.vmData
import java.util.*

/**
 * 多语言相关
 *
 * https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/08
 */
class LanguageModel : ViewModel() {

    companion object {

        /**持久化的key*/
        const val KEY_LOCAL = "key_local"
        const val LOCAL_CONFIG = "local_config.cfg"
        const val LOCAL_SPLIT = ","

        /**获取系统本地语言列表
         * [语言偏好设置]*/
        fun getSystemLocalList(): List<Locale> {
            return getLocaleList(Resources.getSystem().configuration)
        }

        /**获取应用本地语言列表*/
        fun getAppLocaleList(context: Context = app()): List<Locale> {
            return getLocaleList(context.resources.configuration)
        }

        fun getLocaleList(configuration: Configuration): List<Locale> {
            val locales = ConfigurationCompat.getLocales(configuration)
            val result = mutableListOf<Locale>()

            for (i in 0 until locales.size()) {
                locales.get(i)?.let { result.add(it) }
            }
            return result
        }

        /** 获取某个语种下的 Resources 对象 */
        fun getLanguageResources(context: Context, locale: Locale?): Resources {
            val config = Configuration()
            setLocale(config, locale)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context.createConfigurationContext(config).resources
            } else Resources(context.assets, context.resources.displayMetrics, config)
        }

        /**创建一个语言配置*/
        @TargetApi(Build.VERSION_CODES.N)
        fun createConfigurationResources(
            context: Context,
            locale: Locale?, /*跟随系统使用null*/
        ): Context {
            val resources = context.resources
            val configuration = resources.configuration

            val appLocaleList = getAppLocaleList(context)
            val appLocal: Locale = appLocaleList.first()

            val targetLocal = locale ?: getSystemLocalList().first()

            if (isSameLocal(targetLocal, appLocal)) {
                //无变化
                return context
            }
            //调整语言的顺序
            val localList = mutableListOf<Locale>()
            localList.add(targetLocal)
            appLocaleList.forEach {
                if (!isSameLocal(targetLocal, it)) {
                    localList.add(it)
                }
            }
            configuration.setLocales(LocaleList(*localList.toTypedArray()))
            val result = context.createConfigurationContext(configuration)
            //实测，updateConfiguration这个方法虽然很多博主说是版本不适用
            //但是我的生产环境androidX+Android Q环境下，必须加上这一句，才可以通过重启App来切换语言
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return result
        }

        /**老版本的更新预览设置*/
        fun setConfiguration(
            context: Context,
            locale: Locale?, /*跟随系统使用null*/
        ) {
            val resources = context.resources
            val configuration = resources.configuration
            val appLocal: Locale = getAppLocaleList(context).first()

            val targetLocal = locale ?: getSystemLocalList().first()

            if (isSameLocal(targetLocal, appLocal)) {
                //无变化
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(targetLocal)
            } else {
                configuration.locale = targetLocal
            }
            val dm = resources.displayMetrics
            resources.updateConfiguration(configuration, dm) //语言更换生效的代码!
        }

        /** 设置语种对象 */
        fun setLocale(config: Configuration, locale: Locale?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val localeList = LocaleList(locale)
                    config.setLocales(localeList)
                } else {
                    config.setLocale(locale)
                }
            } else {
                config.locale = locale
            }
        }

        /** 设置默认的语种环境（日期格式化会用到） */
        fun setDefaultLocale(context: Context) {
            val configuration = context.resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList.setDefault(configuration.locales)
            } else {
                Locale.setDefault(configuration.locale)
            }
        }

        /**2个语言是否一样*/
        fun isSameLocal(locale: Locale, locale2: Locale): Boolean {
            return if (locale.language == locale2.language && locale.country == locale2.country) {
                if (locale.variant.isNullOrEmpty()) {
                    true
                } else {
                    locale.variant == locale2.variant
                }
            } else {
                false
            }
        }

        /**[super.attachBaseContext(attachBaseContext(base))]*/
        fun attachBaseContext(base: Context): Context {
            return changeAppLanguage(base, readLocal(base), false)
        }

        /**改变app语言*/
        fun changeAppLanguage(
            context: Context,
            locale: Locale?, /*为null时, 表示系统默认*/
            persistence: Boolean = true
        ): Context {
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                createConfigurationResources(context, locale)
            } else {
                setConfiguration(context, locale)
                context
            }
            if (persistence) {
                saveLocal(context, locale)
                vmApp<LanguageModel>().localData.postValue(locale)
                vmApp<LanguageModel>().settingLocalData.postValue(locale)
            }
            return result
        }

        fun readLocalString(context: Context): String? {
            return localConfigSp(context).getString(KEY_LOCAL, null)
        }

        fun readLocal(context: Context): Locale? {
            val localString = readLocalString(context)
            if (localString.isNullOrEmpty()) {
                return null
            }
            val list = localString.split(LOCAL_SPLIT)
            val language = list[0]
            val country = list[1]
            val variant = list[2]
            return Locale(language, country, variant)
        }

        /**持久化*/
        fun saveLocal(context: Context, locale: Locale?) {
            localConfigSp(context).edit {
                putString(
                    KEY_LOCAL,
                    locale?.run { "$language$LOCAL_SPLIT$country$LOCAL_SPLIT$variant" })
            }
        }

        fun localConfigSp(context: Context): SharedPreferences {
            return context.getSharedPreferences(LOCAL_CONFIG, Context.MODE_PRIVATE)
        }

        /**语言配置是否改变*/
        fun isLanguageConfigurationChanged(newConfig: Configuration): Boolean {
            val newLocal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newConfig.locales.get(0)
            } else {
                newConfig.locale
            }
            val local = readLocal(app()) ?: return false
            return !isSameLocal(local, newLocal)
        }

        /**
         * 获取当前时区 [GMT+08:00]
         * @return
         */
        val currentTimeZone: String
            get() {
                val tz = TimeZone.getDefault()
                return tz.getDisplayName(false, TimeZone.SHORT)
            }

        /**时区[Asia/Shanghai]*/
        val timeZoneId: String
            get() {
                val tz = TimeZone.getDefault()
                return tz.id
            }

        /**
         * 时区信息
         * https://developer.android.com/guide/topics/resources/multilingual-support?hl=zh-cn
         * */
        fun getTimeZoneDes(): String = buildString {
            //libcore.util.ZoneInfo[id="Asia/Shanghai",mRawOffset=28800000,mEarliestRawOffset=28800000,mUseDst=false,mDstSavings=0,transitions=28]
            val tz = TimeZone.getDefault()
            append(tz.getDisplayName(false, TimeZone.LONG)) //中国标准时间
            append(" ")
            append(tz.id) //Asia/Shanghai
            append(" ")
            append(tz.getDisplayName(false, TimeZone.SHORT)) //GMT+08:00
            //append(" ")
            //append(tz.rawOffset) //28800000 8小时对应的毫秒数
        }

        /**
         * 获取当前系统语言格式 zh
         * [getLanguage]
         * [getCurrentLanguage]
         */
        fun getLanguage(context: Context = app()): String {
            //zh
            val locale = context.resources.configuration.locale
            return locale.language
        }

        /**
         * 获取当前系统语言格式 zh_CN
         * @param context
         * @return
         * [getLanguage]
         * [getCurrentLanguage]
         */
        fun getCurrentLanguage(context: Context = app()): String {
            //zh_CN_#Hans
            val locale = context.resources.configuration.locale
            val language = locale.language
            val country = locale.country
            return language + "_" + country
        }

        /**zh-CN*/
        fun getCurrentLanguage2(context: Context = app()): String {
            //zh_CN_#Hans
            val locale = context.resources.configuration.locale
            val language = locale.language
            val country = locale.country
            return "$language-$country"
        }

        /**zh-Hans-CN*/
        fun getCurrentLanguageTag(context: Context = app()): String {
            val locale = context.resources.configuration.locale
            return locale.toLanguageTag()
        }

        /**获取语言显示的名称
         * [中文 (简体中文,中国)]*/
        fun getCurrentLanguageDisplayName(context: Context = app()): String {
            val locale = context.resources.configuration.locale
            locale.displayLanguage //中文
            return locale.displayName //中文 (简体中文,中国)
        }

        /**[Locale]
         * [com.angcyo.core.component.model.LanguageModel.Companion.getCurrentLanguageTag]*/
        fun createLanguageForTag(languageTag: String): Locale {
            return Locale.forLanguageTag(languageTag)
        }

        /**判断当前预览环境是否是中文*/
        fun isChinese(): Boolean {
            return getCurrentLanguage().lowercase().startsWith("zh")
        }

        /**获取主要的语言字符串, 优先匹配中文, 其次降级为主要的语言*/
        fun getLanguagePriorityString(priority: String?, zh: String?): String? {
            if (isChinese()) {
                return zh ?: priority
            }
            return priority
        }
    }

    /**设置的语言
     * 为null时, 表示跟随系统*/
    val settingLocalData: MutableLiveData<Locale?> = vmData(null)

    /**当前app的语言 */
    val localData: MutableLiveData<Locale?> = vmData(null)

    private val languageActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                changeAppLanguage(activity, localData.value, false)
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

    /**需要为每一个[Activity]都更新语言配置*/
    @CallPoint
    fun onCreate(application: Application) {
        readLocal(application).apply {
            localData.value = this
            settingLocalData.value = this
        }
        application.registerActivityLifecycleCallbacks(languageActivityLifecycleCallbacks)
    }

    /**
     * 如果app设置的语言是跟随系统, 那么配置改变后, 需要通知app的语言发生了改变, 然后更新界面
     * 如果app强制设置了语言, 那么需要更新[Configuration], 此时不需要通知界面更新
     * [androidx.appcompat.app.AppCompatActivity.onConfigurationChanged]
     * */
    @CallPoint
    fun onConfigurationChanged(activity: Activity, newConfig: Configuration) {
        val settingLocal = settingLocalData.value
        val targetLocal = settingLocalData.value ?: getLocaleList(newConfig).first()

        if (settingLocal == null) {
            localData.value = targetLocal
        }

        changeAppLanguage(activity, targetLocal, false)
    }
}