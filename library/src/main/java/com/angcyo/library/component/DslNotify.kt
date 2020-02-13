package com.angcyo.library.component

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.angcyo.library.app
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.undefined_int

/**
 * https://developer.android.google.cn/guide/topics/ui/notifiers/notifications.html
 *
 * https://developer.android.google.cn/guide/topics/ui/notifiers/notifications.html#Templates
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/13
 */
class DslNotify {

    companion object {

        val _notifyIds = mutableListOf<Int>()

        /**默认的通知图标*/
        var DEFAULT_NOTIFY_ICON = android.R.mipmap.sym_def_app_icon

        fun cancelNotify(context: Context, id: Int) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(id)
            _notifyIds.remove(id)
        }

        fun cancelNotifyLast(context: Context) {
            _notifyIds.lastOrNull()?.run {
                cancelNotify(context, this)
            }
        }

        fun pendingActivity(
            context: Context,
            targetActivity: Class<out Activity>,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
            options: Bundle? = null
        ): PendingIntent {
            val intent = Intent(context, targetActivity)
            return pendingActivity(context, intent, requestCode, flags, options)
        }

        fun pendingActivity(
            context: Context,
            intent: Intent,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
            options: Bundle? = null
        ): PendingIntent {
            return PendingIntent.getActivity(context, requestCode, intent, flags, options)
        }

        fun pendingBroadcast(
            context: Context,
            intent: Intent,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            return PendingIntent.getBroadcast(context, requestCode, intent, flags)
        }

        fun pendingService(
            context: Context,
            intent: Intent,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            return PendingIntent.getService(context, requestCode, intent, flags)
        }

        fun action(
            title: CharSequence,
            pendingIntent: PendingIntent,
            icon: Int = 0,
            action: NotificationCompat.Action.Builder.() -> Unit = {}
        ): NotificationCompat.Action {
            val builder = NotificationCompat.Action.Builder(icon, title, pendingIntent)
            builder.action()
            return builder.build()
        }

        fun person(name: CharSequence? = null, action: Person.Builder.() -> Unit = {}): Person {
            val builder = Person.Builder()
            builder.setName(name)
            builder.action()
            return builder.build()
        }

        fun message(
            text: CharSequence?,
            timestamp: Long = nowTime(),
            person: Person? = null,
            action: NotificationCompat.MessagingStyle.Message .() -> Unit = {}
        ): NotificationCompat.MessagingStyle.Message {
            val message = NotificationCompat.MessagingStyle.Message(text, timestamp, person)
            message.action()
            return message
        }
    }

    //<editor-fold desc="通道相关配置 Android 8 (O)">

    /**
     * 通知渠道的重要性
     * 可能的重要性级别如下：
     * 紧急：发出声音并以浮动通知的形式显示。
     * 高：发出声音。
     * 中：不发出声音。
     * 低：不发出声音，也不在状态栏中显示。
     * */
    var channelImportance: Int = undefined_int

    /**通道id*/
    var channelId = "DefaultChannel"

    /**通道名称*/
    var channelName: CharSequence = "DefaultChannel"

    /**通道是否要显示小圆点*/
    var showBadge = true

    /**允许气泡通知*/
    var allowBubbles = true

    /**激活通知灯*/
    var enableLights = true

    /**激活震动*/
    var enableVibration = true

    var onConfigChannel: (NotificationChannel) -> Unit = {}

    /**创建通道,反复调用这段代码是安全的，因为创建现有通知渠道不会执行任何操作。*/
    fun _createNotifyChannel(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                if (channelImportance == undefined_int) NotificationManager.IMPORTANCE_HIGH else channelImportance
            )

            channel.run {
                setShowBadge(showBadge)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(allowBubbles)
                }
                enableLights(enableLights)
                enableVibration(enableVibration)

                //setBypassDnd()

//                setSound(
//                    Settings.System.DEFAULT_NOTIFICATION_URI,
//                    Notification.AUDIO_ATTRIBUTES_DEFAULT
//                )

                onConfigChannel(this)
            }

            notificationManager.createNotificationChannel(channel)
        }

    }

    //</editor-fold desc="通道相关配置 Android 8 (O)">

    //<editor-fold desc="Style配置">

    /**
     * [NotificationCompat.BigTextStyle]
     * [NotificationCompat.BigPictureStyle]
     * [NotificationCompat.InboxStyle]
     * 通用*/
    var styleBigContentTitle: CharSequence? = null
    /**没啥鸟用?*/
    var styleBigSummaryText: CharSequence? = null

    /**[NotificationCompat.BigTextStyle]
     * https://developer.android.google.cn/training/notify-user/expanded.html#large-style*/
    var styleBigText: CharSequence? = null

    /**[NotificationCompat.BigPictureStyle]
     * https://developer.android.google.cn/training/notify-user/expanded.html#image-style*/
    var styleBigPicture: Bitmap? = null
    var styleBigLargeIcon: Bitmap? = null

    /**[NotificationCompat.InboxStyle], 最多6行, 测试有7行, 超出截断
     * https://developer.android.google.cn/training/notify-user/expanded.html#inbox-style*/
    var styleLineList: List<CharSequence>? = null

    /**[NotificationCompat.MessagingStyle]
     * https://developer.android.google.cn/training/notify-user/expanded.html#message-style*/
    var stylePerson: Person? = null
    var styleMessageList: List<NotificationCompat.MessagingStyle.Message>? = null

    /**
     * 需要[androidx.media]的支持
     * https://developer.android.google.cn/training/notify-user/expanded.html#media-style*/
    var styleMediaSessionToken: MediaSessionCompat.Token? = null

    fun _createStyle(): NotificationCompat.Style? {
        var style: NotificationCompat.Style? = null
        if (styleBigText != null) {
            style = NotificationCompat.BigTextStyle().also {
                styleBigContentTitle?.run { it.setBigContentTitle(this) }
                styleBigSummaryText?.run { it.setSummaryText(this) }
                styleBigText?.run { it.bigText(this) }
            }
        }

        if (style == null) {
            if (styleBigPicture != null) {
                style = NotificationCompat.BigPictureStyle().also {
                    styleBigContentTitle?.run { it.setBigContentTitle(this) }
                    styleBigSummaryText?.run { it.setSummaryText(this) }

                    styleBigPicture?.run { it.bigPicture(this) }
                    styleBigLargeIcon?.run { it.bigLargeIcon(this) }
                }
            }
        }

        if (style == null) {
            if (styleLineList != null) {
                style = NotificationCompat.InboxStyle().also {
                    styleBigContentTitle?.run { it.setBigContentTitle(this) }
                    styleBigSummaryText?.run { it.setSummaryText(this) }

                    styleLineList?.forEach { line ->
                        it.addLine(line)
                    }
                }
            }
        }

        if (style == null) {
            if (stylePerson != null) {
                style = NotificationCompat.MessagingStyle(stylePerson!!).also {
                    //it.conversationTitle
                    styleMessageList?.forEach { message ->
                        it.addMessage(message)
                    }
                }
            }
        }

        if (style == null) {
            if (styleMediaSessionToken != null) {
                style = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle().also {
                    it.setMediaSession(styleMediaSessionToken)
                }
            }
        }
        return style
    }

    //</editor-fold desc="Style配置">

    //<editor-fold desc="通知相关配置">

    var notifyId = System.currentTimeMillis().toInt()

    /**必须的最小成员变量,一般建议在24×24*/
    var notifySmallIcon = DEFAULT_NOTIFY_ICON
    /**通知栏, 右边的大图, 不支持SVG?*/
    var notifyLargeIcon: Bitmap? = null

    /**通知标题*/
    var notifyTitle: CharSequence? = null
    /**通知正文文本, 默认情况下，通知的文字内容会被截断以放在一行。使用[setStyle]*/
    var notifyText: CharSequence? = null
        set(value) {
            field = value
            if (value != null) {
                notifyTickerText = value
            }
        }

    /**没发现有啥鸟用*/
    var notifyInfo: CharSequence? = null
    /**显示在[notifyTitle]后面的文本, 当一组中有多个通知, 这个就是title*/
    var notifySubText: CharSequence? = null

    /**首次通知时, 立马就要显示的文本*/
    var notifyTickerText: CharSequence? = null

    /**通知的优先级, 通道还有一个重要性*/
    var notifyPriority = NotificationCompat.PRIORITY_HIGH

    var notifyDefaults = NotificationCompat.DEFAULT_VIBRATE

    var notifyContentIntent: PendingIntent? = null
    /**配置[notifyContentIntent]才有效果*/
    var notifyAutoCancel = true

    /**用户一键清除通知时, 触发的意图.[NotificationManager.cancel]不会触发*/
    var notifyDeleteIntent: PendingIntent? = null

    /**立即要展示, 而不是发送到状态栏. 横幅通知. 不会消失.*/
    var notifyFullScreenIntent: PendingIntent? = null

    var notifyFullScreenIntentHighPriority = false

    var notifyActions: List<NotificationCompat.Action>? = null

    /**通知可见性,
     * [VISIBILITY_PUBLIC]在所有界面上都显示内容,
     * [VISIBILITY_PRIVATE]隐藏隐私信息,
     * [VISIBILITY_SECRET]锁屏不展示*/
    var notifyVisibility = VISIBILITY_PUBLIC

    /**通知的时间*/
    var notifyWhen = System.currentTimeMillis()

    /**是否需要显示通知的时间*/
    var notifyShowWhen = true

    /**正在进行的通知, 不允许侧滑删除*/
    var notifyOngoing = false

    var onConfigNotify: (NotificationCompat.Builder) -> Unit = {}

    /**创建通知*/
    fun _createNotify(context: Context): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
        builder.run {
            setSmallIcon(notifySmallIcon)
            notifyLargeIcon?.run { setLargeIcon(this) }

            notifyTitle?.run { setContentTitle(this) }
            notifyText?.run { setContentText(this) }
            notifyInfo?.run { setContentInfo(this) }
            notifySubText?.run { setSubText(this) }

            notifyTickerText?.run {
                setTicker(this)
            }

            setWhen(notifyWhen)
            setShowWhen(notifyShowWhen)
            setOngoing(notifyOngoing)

            setVisibility(notifyVisibility)

            //PRIORITY_HIGH 就会有横幅通知, 并且会自动消失
            priority = notifyPriority
            setDefaults(notifyDefaults)

            _createStyle()?.run {
                setStyle(this)
            }

            notifyContentIntent?.run { setContentIntent(this) }
            notifyDeleteIntent?.run { setDeleteIntent(this) }
            notifyFullScreenIntent?.run {
                setFullScreenIntent(
                    this,
                    notifyFullScreenIntentHighPriority
                )
            }

            setAutoCancel(notifyAutoCancel)

            notifyActions?.forEach {
                addAction(it)
            }

            onConfigNotify(this)
        }
        return builder.build()
    }

    fun doIt(context: Context): Int {
        _createNotifyChannel(context)
        val notification = _createNotify(context)

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(notifyId, notification)

        _notifyIds.add(notifyId)
        return notifyId
    }

    //</editor-fold desc="通知相关配置">

}

fun dslNotify(context: Context = app(), action: DslNotify.() -> Unit): Int {
    return DslNotify().run {
        action()
        doIt(context)
    }
}