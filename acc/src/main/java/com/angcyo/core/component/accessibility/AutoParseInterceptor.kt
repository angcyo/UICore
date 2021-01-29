package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.core.component.accessibility.action.*
import com.angcyo.core.component.accessibility.base.AccessibilityWindow
import com.angcyo.core.component.accessibility.base.BaseFloatInterceptor
import com.angcyo.core.component.accessibility.parse.*
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.library.LTime
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.FileUtils
import com.angcyo.widget.span.span
import kotlin.io.readText
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class AutoParseInterceptor(val taskBean: TaskBean) : BaseFloatInterceptor() {

    companion object {
        const val LOG_TASK_FILE_NAME = "task.log"

        /**当前运行的[ActionBean]*/
        var currentActionBean: ActionBean? = null

        /**执行任务时的日志输出*/
        fun log(data: CharSequence?) {
            if (!data.isNullOrEmpty()) {
                DslFileHelper.write(
                    AccessibilityHelper.logFolderName,
                    LOG_TASK_FILE_NAME,
                    data.wrapData()
                )
            }
        }

        fun logPath() = FileUtils.appRootExternalFolderFile(
            folder = AccessibilityHelper.logFolderName,
            name = LOG_TASK_FILE_NAME
        )?.absolutePath

        /**读取日志文本*/
        fun readLog(): String? {
            return logPath()?.file()?.readText()
        }
    }

    /**请求表单时, 配置表单数据的回调*/
    var onConfigParams: ((params: HashMap<String, Any>) -> Unit)? = null

    /**离开界面多久后, 重启. 毫秒*/
    var leaveRestartTime: Long = 10 * 1000

    init {
        //固定通知id
        notifyId = 999
    }

    //发送通知
    fun notify(
        content: CharSequence?,
        title: CharSequence? = "${taskBean.name}(${max(0, actionIndex)}/${actionList.size})"
    ) {
        sendNotify(title, content)
    }

    override fun onDoActionStart() {
        super.onDoActionStart()
        AccessibilityWindow.notTouch = taskBean.notTouchable

        if (updateWindow) {

            if (taskBean.fullscreen) {
                //全屏浮窗
                AccessibilityWindow.fullscreenLayer = true
            }

            AccessibilityWindow.fullTitleText = span {
                if (!taskBean.name.isNullOrEmpty()) {
                    append(taskBean.name)
                }
                if (!taskBean.gold.isNullOrEmpty()) {
                    if (!taskBean.name.isNullOrEmpty()) {
                        appendln()
                    }
                    if (taskBean.gold!!.isNumber()) {
                        append("做完任务您将获得${taskBean.gold}金币")
                    } else {
                        append(taskBean.gold)
                    }
                }
            }
        }

        //清空缓存
        taskBean.getTextResultMap = null

        notify("就绪")

        //立即启动对应app
        //taskBean.packageName?.openApp(0)
    }

    override fun handleFilterNode(
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.handleFilterNode(service, nodeList)
    }

    override fun onDoAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //save
        if (action is AutoParseAction) {
            currentActionBean = action.actionBean
        }
        notify(action.actionTitle)
        super.onDoAction(action, service, nodeList)
    }

    override fun onDoActionFinish(action: BaseAccessibilityAction?, error: ActionException?) {
        handleFormRequest(error)

        log("[${taskBean.name}/${action?.actionTitle}]执行结束:${runActionStatus.toActionStatusStr()} ${error ?: ""} 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        when (runActionStatus) {
            ACTION_STATUS_ERROR -> {
                //出现异常
                notify("异常:${error?.message}")
            }
            ACTION_STATUS_INTERRUPTED -> {
                //出现异常
                notify("中止:${error?.message}")
            }
            ACTION_STATUS_FINISH -> {
                //流程结束
                notify("执行完成!")
                //lastService?.home()
            }
        }

        if (taskBean.finishToApp) {
            openApp()
        }

        super.onDoActionFinish(action, error)
    }

    override fun checkLeave(
        service: BaseAccessibilityService,
        mainPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        //actionList.getOrNull(actionIndex)
        return super.checkLeave(service, mainPackageName, nodeList).apply {

            var needDefaultHandle = true

            //超限
            if (taskBean.leave != null || taskBean.leaveOut != null) {
                currentAccessibilityAction?.also { action ->
                    val old = action.accessibilityInterceptor
                    action.accessibilityInterceptor = this@AutoParseInterceptor
                    if (action is AutoParseAction) {
                        if (action._actionFinish == null) {
                            action._actionFinish = {
                                _actionFinish(action, service, it, nodeList)
                            }
                        }

                        if (taskBean.leave != null) {
                            val result =
                                action.parseHandleAction(
                                    service,
                                    currentAccessibilityAction,
                                    nodeList,
                                    taskBean.leave
                                )
                            if (result) {
                                needDefaultHandle = false
                                interceptorLeaveCount.clear()
                            }
                        }

                        if (taskBean.leaveOut != null && interceptorLeaveCount.isMaxLimit()) {
                            val result =
                                action.parseHandleAction(
                                    service,
                                    currentAccessibilityAction,
                                    nodeList,
                                    taskBean.leaveOut
                                )
                            if (result) {
                                needDefaultHandle = false
                                interceptorLeaveCount.clear()
                            }
                        }
                    }
                    action.accessibilityInterceptor = old
                }
            }

            if (filterPackageNameList.isNotEmpty()) {
                interceptorLog?.log("离开目标:$filterPackageNameList ${LTime.time(_lastLeaveTime)} 停留在:$_lastLeavePackageName")

                if (needDefaultHandle && (nowTime() - _lastLeaveTime) > leaveRestartTime) {
                    //离开停留在同一个界面时间超过10分钟,强制重新开始
                    interceptorLog?.log("超过${leaveRestartTime / 1000}秒,即将重新开始!")
                    _lastLeaveTime = nowTime()
                    //restart()
                    startAction(true)
                }
            }
        }
    }

    override fun onLeavePackageName(
        service: BaseAccessibilityService,
        fromPackageName: CharSequence?,
        toPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //super.onLeavePackageName(service, fromPackageName, toPackageName, nodeList)

        //权限窗口
        PermissionsAction().apply {
            if (doActionWidth(this, service, lastEvent, nodeList)) {
                log("[${taskBean.name}]权限请求:true")
            }
        }

        //多开窗口
        MultiAppAction().apply {
            packageName = filterPackageNameList.firstOrNull()
            initAction()
            if (doActionWidth(this, service, lastEvent, nodeList)) {
                log("检测到多开应用[${packageName?.appBean()?.appName}], 打开第[$defaultOpenIndex]个:true")
            }
        }
    }

    override fun onNoOtherActionHandle(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.onNoOtherActionHandle(action, service, event, nodeList)
    }

    override fun onDestroy(reason: String?) {
        //自动恢复touch
        AccessibilityWindow.notTouch = false

        //clear
        currentActionBean = null

        val isInterrupt = runActionStatus.isActionStart()
        if (isInterrupt) {
            val actionInterruptedException =
                ActionInterruptedException(reason ?: "拦截器被中断[onDestroy]!")

            var needDefaultHandle = true
            currentAccessibilityAction?.let {
                if (it.isActionStart()) {
                    needDefaultHandle = false
                    it.doActionFinish(actionInterruptedException)
                }
            }
            if (needDefaultHandle) {
                onDoActionFinish(currentAccessibilityAction, actionInterruptedException)
            }
        }

        //super
        super.onDestroy(reason)
        if (isInterrupt) {
            notify("中止")
        }
    }

    /**表单请求*/
    fun handleFormRequest(error: ActionException?) {
        taskBean.form?.let {
            //指定了表单处理
            it.request(result = { data, error ->
                if (error != null) {
                    //拦截器,接口请求失败
                    toastQQ(error.message)
                }
            }) { map ->

                //params
                handleFormParams(map)

                //action执行结果, 执行成功发送 200
                if (error == null) {
                    map[FormBean.KEY_MSG] =
                        "${taskBean.name} 执行完成,耗时${nowTime() - _actionStartTime}"
                } else {
                    map[FormBean.KEY_MSG] = "${taskBean.name},${error.message ?: ""},任务失败!"
                }

                //错误码绑定
                map.bindErrorCode(error)

                //额外配置
                onConfigParams?.apply {
                    invoke(map)
                }
            }
        }
    }

    /**表单参数*/
    fun handleFormParams(params: HashMap<String, Any>) {
        //将action获取到的所有文本, 打包进参数
        taskBean.getTextResultMap?.forEach { entry ->
            params[entry.key] = "${entry.value.firstOrNull()}"
        }
    }

    override fun _actionStart(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super._actionStart(action, service, nodeList)

        if (action is AutoParseAction && !taskBean.start.isListEmpty()) {
            action._actionFinish = {
                action._actionFinish = null
                if (it != null) {
                    actionError(action, it)
                }
            }

            action.handleActionLog("执行(${action.actionBean?.title})前的处理↓")
            val result = action.parseHandleAction(
                service,
                currentAccessibilityAction,
                nodeList,
                taskBean.start
            )
            action.handleActionLog("执行(${action.actionBean?.title})前的处理:$result")
            action._actionFinish = null
        }
    }

    override fun _actionFinish(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        error: ActionException?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super._actionFinish(action, service, error, nodeList)

        if (action is AutoParseAction && !taskBean.end.isListEmpty()) {
            action._actionFinish = {
                action._actionFinish = null
                if (it != null) {
                    actionError(action, it)
                }
            }

            action.handleActionLog("执行(${action.actionBean?.title})后的处理↓")
            val result = action.parseHandleAction(
                service,
                currentAccessibilityAction,
                nodeList,
                taskBean.end
            )
            action.handleActionLog("执行(${action.actionBean?.title})后的处理:$result")
            action._actionFinish = null
        }
    }
}