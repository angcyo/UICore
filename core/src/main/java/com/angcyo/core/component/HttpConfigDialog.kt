package com.angcyo.core.component

import android.content.Context
import android.view.View
import com.angcyo.core.CoreApplication
import com.angcyo.core.R
import com.angcyo.dialog.DslDialogConfig
import com.angcyo.dialog.dslDialog
import com.angcyo.http.DslHttp
import com.angcyo.http.base.fromJson
import com.angcyo.http.base.toJson
import com.angcyo.http.isSucceed
import com.angcyo.http.rx.BaseObserver
import com.angcyo.http.rx.observer
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.toast
import com.angcyo.widget.RSpinner
import com.angcyo.widget.base.setInputText
import com.angcyo.widget.base.string
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.RetrofitServiceMapping
import retrofit2.RetrofitServiceMapping.init

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/13
 */
object HttpConfigDialog {

    var MAPPING_URL = "https://www.angcyo.com/api/php/android/c/url_mapping"

    /**自定义后的url*/
    var customBaseUrl: String? = null

    /**app正式使用的服务器地址*/
    val appBaseUrl: String
        get() {
            return DslHttp.dslHttpConfig.onGetBaseUrl()
        }

    /**app中配置自定义的其他服务器*/
    val appCustomUrls: List<String>
        get() {
            val result = mutableListOf<String>()
            val app = (app() as? CoreApplication)
            if (app == null) {
                customBaseUrl?.let { result.add(it) }
            } else {
                result.add("当前 ${app.getHostBaseUrl()}")
                app.getHostUrls()?.split(";")?.forEach {
                    if (it.isNotEmpty()) {
                        result.add(it)
                    }
                }
            }
            return result
        }

    /**显示网络配置地址配置对话框*/
    fun showHttpConfig(context: Context, end: () -> Unit = {}) {
        show(context, appBaseUrl, appCustomUrls) { url, cancel ->
            if (!cancel) {
                customBaseUrl = url
            }
            end()
        }
    }

    /**
     * @param urlList 可以使用空格, key:value 的形式, 会取空格分隔后的最后一个
     */
    fun show(
        context: Context,
        baseUrl: String,
        urlList: List<String>? = null,
        /*url,是否被取消*/
        save: (url: String?, cancel: Boolean) -> Unit
    ) {
        dslDialog(context) {
            canceledOnTouchOutside = false
            dialogThemeResId = 0
            dialogType = DslDialogConfig.DIALOG_TYPE_ALERT_DIALOG
            dialogLayoutId = R.layout.lib_http_config_layout
            onCancelListener = {
                save(null, true)
            }
            onDialogInitListener = { dialog, dialogViewHolder ->

                dialogViewHolder.ev(R.id.host_edit)?.setInputText(baseUrl)

                dialogViewHolder.check(
                    R.id.map_box,
                    RetrofitServiceMapping.enableMapping
                ) { _, isChecked ->
                    dialogViewHolder.view(R.id.get_list)?.isEnabled = isChecked

                    init(isChecked, RetrofitServiceMapping.defaultMap)
                }

                dialogViewHolder.click(R.id.get_list, View.OnClickListener { v ->

                    com.angcyo.http.get {
                        url = MAPPING_URL
                        isSuccessful = {
                            it.isSuccessful
                        }
                    }.observer(BaseObserver<Response<JsonElement>>().apply {
                        onObserverEnd = { data, _ ->
                            if (data.isSucceed(null)) {
                                val json = data!!.body().toJson()
                                L.i(json)
                                val mapping: MutableMap<String, String> =
                                    json.fromJson(MutableMap::class.java) as MutableMap<String, String>
                                init(RetrofitServiceMapping.enableMapping, mapping)
                                v.post { toast(json) }
                            }
                        }
                    })
                })

                val spinner: RSpinner? = dialogViewHolder.v(R.id.url_spinner)
                val urls = mutableListOf<String>()
                urls.add("选择服务器Url")
                if (urlList == null || urlList.isEmpty()) {
                    urls.add(baseUrl)
                } else {
                    urls.addAll(urlList)
                }

                spinner?.setStrings(urls) { position ->
                    if (position != 0) {
                        val url = urls[position]
                        val split = url.split(" ".toRegex()).toTypedArray()
                        dialogViewHolder.ev(R.id.host_edit)?.setInputText(split[split.size - 1])
                    }
                }

                dialogViewHolder.click(R.id.lib_save_button) {
                    val url = dialogViewHolder.ev(R.id.host_edit).string()
                    save(url, false)
                    L.w("save url->$url")
                    dialog.dismiss()
                }
            }
        }
    }

}