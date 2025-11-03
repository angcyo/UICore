package com.angcyo.item.component

import android.os.Bundle
import android.widget.TextView
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.core.component.interceptor.PrintLogAction
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.http.DslHttp
import com.angcyo.http.base.toJsonElement
import com.angcyo.http.post
import com.angcyo.http.rx.observe
import com.angcyo.item.DslInputItem
import com.angcyo.item.DslLastWrapItem
import com.angcyo.item.DslTextItem
import com.angcyo.item.R
import com.angcyo.item.style.itemEditHint
import com.angcyo.item.style.itemEditText
import com.angcyo.item.style.itemLabel
import com.angcyo.item.style.itemMaxInputLength
import com.angcyo.item.style.itemText
import com.angcyo.item.style.multiLineEditMode
import com.angcyo.library.L
import com.angcyo.library.ex.connectUrl
import com.angcyo.library.ex.find
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.isNil
import com.angcyo.library.ex.toStr
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.resetChild

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/11/03
 *
 * http 接口调试界面
 */
class HttpRequestTestFragment : BaseDslFragment() {

    companion object {

        /**请求调试入口*/
        val REQUEST_DEBUG_ACTION_LIST = mutableListOf<DebugAction>().apply {
            add(
                DebugAction(
                    "登录接口",
                    host = "https://alternate.hingin.com",
                    requestUrl = "/login",
                    requestBody = """
                    {
                        "email": "angcyo@126.com",
                        "credential": "angcyo"
                    }
                    """.trimIndent()
                )
            )
        }

        /**
         * HttpRequestTestFragment.addRequestDebugAction {
         *     name = "FileServer"
         *     action = {
         *       coreApp().bindFileServer()
         *       //(RBackground.lastActivityRef?.get() as? LifecycleOwner ?: it)
         *   }
         * }
         * */
        fun addRequestDebugAction(action: DebugAction.() -> Unit) {
            REQUEST_DEBUG_ACTION_LIST.add(DebugAction().apply(action))
        }
    }

    init {
        fragmentTitle = "接口请求调试"
        enableSoftInput = true
    }

    /**服务器地址*/
    private var hostUrl
        get() = "_http_host_url".hawkGet(DslHttp.dslHttpConfig.onGetBaseUrl())
        set(value) {
            "_http_host_url".hawkPut(value)
        }

    /**请求的接口*/
    private var requestUrl
        get() = "_http_request_url".hawkGet("")
        set(value) {
            "_http_request_url".hawkPut(value)
        }

    //--

    /**请求的json字符串*/
    private var requestBody
        get() = "_http_request_body_string".hawkGet("")
        set(value) {
            "_http_request_body_string".hawkPut(value)
        }

    /**返回字符串*/
    private var responseString
        get() = "_http_request_response_string".hawkGet("")
        set(value) {
            "_http_request_response_string".hawkPut(value)
        }

    private val printRequestLogAction: PrintLogAction = { log ->
        //requestBodyJsonString = log
    }

    private val printResponseLogAction: PrintLogAction = { log ->
        responseString = log
        renderAdapter()
    }

    //--

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        LogFileInterceptor.printRequestLogActionList.add(printRequestLogAction)
        LogFileInterceptor.printResponseLogActionList.add(printResponseLogAction)
        renderAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogFileInterceptor.printRequestLogActionList.remove(printRequestLogAction)
        LogFileInterceptor.printResponseLogActionList.remove(printResponseLogAction)
    }

    /**渲染界面*/
    private fun renderAdapter() {
        renderDslAdapter(true) {
            DslInputItem()() {
                itemLabel = "服务器地址"
                itemEditHint = itemLabel
                itemEditText = hostUrl
                itemMaxInputLength = 999
                observeItemChange {
                    hostUrl = itemEditText?.toStr()
                }
            }
            DslInputItem()() {
                itemLabel = "请求接口"
                itemEditHint = itemLabel
                itemEditText = requestUrl
                itemMaxInputLength = 999
                observeItemChange {
                    requestUrl = itemEditText?.toStr()
                }
            }
            DslInputItem()() {
                itemLabel = "请求体(jsonString)"
                itemEditHint = itemLabel
                itemEditText = requestBody
                itemMaxInputLength = 9999
                multiLineEditMode(6)
                observeItemChange {
                    requestBody = itemEditText?.toStr()
                }
            }
            //--
            DslTextItem()() {
                itemText = responseString
            }

            DslLastWrapItem()() {
                itemContentLayoutId = R.layout.item_debug_last_flow_layout
                itemBindOverride = { itemHolder, itemPosition, adapterItem, payloads ->
                    itemHolder.group(R.id.lib_flow_layout)
                        ?.resetChild(
                            //渲染按钮actions
                            REQUEST_DEBUG_ACTION_LIST.filter { it.key.isNullOrEmpty() },
                            R.layout.lib_button_layout
                        ) { itemView, item, itemIndex ->
                            itemView.find<TextView>(R.id.lib_button)?.apply {
                                text = item.name
                                clickIt {
                                    if (item.action == null) {
                                        if (item.host != null) {
                                            hostUrl = item.host
                                        }
                                        if (item.requestUrl != null) {
                                            requestUrl = item.requestUrl
                                        }
                                        if (item.requestBody != null) {
                                            requestBody = item.requestBody
                                        }
                                    } else {
                                        item.action?.invoke(this@HttpRequestTestFragment, item)
                                    }
                                    //
                                    renderAdapter()
                                }
                            }
                        }

                    itemHolder.click(R.id.start_button) {
                        startRequest(hostUrl.connectUrl(requestUrl), requestBody)
                    }
                }
            }
        }
    }

    /**开始请求*/
    fun startRequest(url: String, jsonBody: String?) {
        post {
            this.url = url
            this.body = if (isNil(jsonBody)) null else jsonBody?.toJsonElement()
        }.observe { data, error ->
            L.d(data, error)
        }
    }

}
