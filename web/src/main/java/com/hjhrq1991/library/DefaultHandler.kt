package com.hjhrq1991.library

open class DefaultHandler : BridgeHandler {
    var TAG = "DefaultHandler"
    override fun handler(data: String?, function: CallBackFunction?) {
        function?.onCallBack("DefaultHandler response data")
    }
}