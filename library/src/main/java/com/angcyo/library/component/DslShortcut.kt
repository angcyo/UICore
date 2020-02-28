package com.angcyo.library.component

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.angcyo.library.L
import com.angcyo.library.ex.have


/**
 * 创建桌面快捷方式
 * https://developer.android.google.cn/guide/topics/ui/shortcuts
 *
 * 相同id, 相同名称的快捷方式都可以并存.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslShortcut(val context: Context) {

    companion object {
        /**当快捷方式固定成功后触发的广播[action]*/
        const val ACTION_PIN_SHORTCUT = "request_pin_shortcut"

        /**固定快捷方式到桌面*/
        const val ACTION_TYPE_PIN_SHORTCUT = 0x1
        /**添加动态的快捷方式 API>=25, 有数量限制, 最好不要超过5个, 只会显示4个, 超过会崩溃*/
        const val ACTION_TYPE_DYNAMIC_SHORTCUT = 0x2
        /**移除快捷方式 API>=25 可以删除[ACTION_TYPE_DYNAMIC_SHORTCUT]*/
        const val ACTION_TYPE_REMOVE_SHORTCUT = 0x4
        /**更新快捷方式 API>=25 可以更新[ACTION_TYPE_DYNAMIC_SHORTCUT]*/
        const val ACTION_TYPE_UPDATE_SHORTCUT = 0x8
        /**删除所有 API>=25 可以删除[ACTION_TYPE_DYNAMIC_SHORTCUT]*/
        const val ACTION_TYPE_REMOVE_ALL_SHORTCUT = 0xF
        /**安装快捷方式*/
        const val ACTION_TYPE_INSTALL_SHORTCUT = 0x20
    }

    /**操作类型*/
    var shortcutAction: Int = ACTION_TYPE_PIN_SHORTCUT

    /**快捷方式触发的[Intent]*/
    var shortcutIntent: Intent? = null

    /**低版本只能用资源创建图标 API25*/
    @DrawableRes
    var shortcutIconId: Int = -1
        set(value) {
            field = value
            if (value > 0) {
                shortcutIcon = IconCompat.createWithResource(context, value)
            }
        }

    /**API25及以上可以使用[Icon]*/
    var shortcutIcon: IconCompat? = null
    /**快捷方式的id, 用于移除快捷方式*/
    var shortcutId: String? = null
    /**短标签*/
    var shortcutLabel: CharSequence? = null
        set(value) {
            field = value
            if (shortcutId.isNullOrBlank()) {
                shortcutId = value.toString()
            }
        }
    /**长标签*/
    var shortcutLongLabel: CharSequence? = null
    /**不可用时, 提示的信息*/
    var shortcutDisabledMessage: CharSequence? = "快捷方式不可用"

    /**当快捷方式固定成功后触发的[PendingIntent] Android Q收不到广播*/
    var pendingPinShortcutIntent: PendingIntent = DslNotify.pendingBroadcast(
        context, Intent("${context.packageName}.${ACTION_PIN_SHORTCUT}")
    )

    /**执行*/
    fun doIt() {
        if (shortcutAction == 0) {
            L.w("请设置快捷方式行为[shortcutAction]")
            return
        }

        if (shortcutAction.have(ACTION_TYPE_REMOVE_ALL_SHORTCUT)) {
            ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        }

        if (shortcutId == null) {
            L.w("请设置快捷方式Id[shortcutId]")
            return
        }

        /*创建快捷方式的必要素*/
        val canCreateShortcut = shortcutLabel != null && shortcutIntent != null

        //给Intent添加 对应的flag
        //Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

        if (shortcutAction.have(ACTION_TYPE_REMOVE_SHORTCUT)) {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcutId))

            //https://blog.csdn.net/lanfei1027/article/details/48297409
            val shortcut = Intent("com.android.launcher.action.UNINSTALL_SHORTCUT")
            //快捷方式的名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutLabel)
            /*删除和创建需要对应才能找到快捷方式并成功删除**/
            val intent = Intent()
            if (canCreateShortcut) {
                try {
                    intent.setClass(context, Class.forName(shortcutIntent!!.component!!.className))
                } catch (e: Exception) {
                    L.e(e)
                }
            }
            intent.action = Intent.ACTION_VIEW
            //intent.addCategory("android.intent.category.LAUNCHER")
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            context.sendBroadcast(shortcut);
        }

        if (context !is Activity) {
            shortcutIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        //快捷方式信息
        val shortcutInfoCompat: ShortcutInfoCompat? = if (canCreateShortcut) {
            ShortcutInfoCompat.Builder(context, shortcutId!!)
                .apply {
                    shortcutIntent?.run {
                        if (action.isNullOrBlank()) {
                            action = Intent.ACTION_VIEW
                        }
                        setIntent(this)
                    }
                    shortcutIcon?.run { setIcon(this) }
                    shortcutLabel?.run { setShortLabel(this) }
                    shortcutLongLabel?.run { setLongLabel(this) }
                    shortcutDisabledMessage?.run { setDisabledMessage(this) }
                    setLongLived(true)
                }
                .build()
        } else {
            L.w("请检查:shortcutLabel=$shortcutLabel shortcutIntent=$shortcutIntent")
            null
        }

        //打成list
        val shortcutInfoList = shortcutInfoCompat?.run { listOf(this) } ?: emptyList()

        //固定到桌面
        if (shortcutAction.have(ACTION_TYPE_PIN_SHORTCUT) &&
            ShortcutManagerCompat.isRequestPinShortcutSupported(context) &&
            shortcutInfoCompat != null
        ) {
            //Android 5.0测试有效
            ShortcutManagerCompat.requestPinShortcut(
                context,
                shortcutInfoCompat,
                pendingPinShortcutIntent.intentSender
            )
        } else if (shortcutAction.have(ACTION_TYPE_INSTALL_SHORTCUT) && shortcutInfoCompat != null) {
            val resultIntent =
                ShortcutManagerCompat.createShortcutResultIntent(context, shortcutInfoCompat)

            //设置点击快捷方式，进入指定的Activity
            //注意：因为是从Lanucher中启动，所以这里用到了ComponentName
            //其中new ComponentName这里的第二个参数，是Activity的全路径名，也就是包名类名要写全。
            //shortcutIntent.component = ComponentName(this.getPackageName(), "这里是包名.类名")

            //https://blog.csdn.net/c676063769/article/details/25485685
            //不允许重复创建, 测试没通过
            resultIntent.putExtra("duplicate", false)

            //设置Action 可行, Android R 不可行.
            resultIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            //发送广播、通知系统创建桌面快捷方式
            context.sendBroadcast(resultIntent)

            //无效
            //resultIntent.action = Intent.ACTION_CREATE_SHORTCUT
            //context.sendBroadcast(resultIntent)
        }

        //动态快捷方式
        if (shortcutAction.have(ACTION_TYPE_DYNAMIC_SHORTCUT) && canCreateShortcut) {
            ShortcutManagerCompat.addDynamicShortcuts(context, shortcutInfoList)
        }

        //更新快捷方式
        if (shortcutAction.have(ACTION_TYPE_UPDATE_SHORTCUT) && canCreateShortcut) {
            ShortcutManagerCompat.updateShortcuts(context, shortcutInfoList)
        }
    }
}

/**[context]必须是前台可见*/
fun dslShortcut(context: Context, action: DslShortcut.() -> Unit) {
    DslShortcut(context).apply {
        action()
        doIt()
    }
}