package com.angcyo.library.component

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.media.session.MediaButtonReceiver
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.ex.baseConfig
import com.angcyo.library.ex.have
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.undefined_int
import kotlin.math.min


/**
 * https://developer.android.google.cn/guide/topics/ui/notifiers/notifications.html
 *
 * https://developer.android.google.cn/guide/topics/ui/notifiers/notifications.html#Templates
 *
 * https://developer.android.com/training/notify-user/channels?hl=zh-cn
 *
 * 用户可见的重要性级别	重要性（Android 8.0 及更高版本）	优先级（Android 7.1 及更低版本）
 * 紧急
 * ：发出提示音，并以浮动通知的形式显示	IMPORTANCE_HIGH	PRIORITY_HIGH 或 PRIORITY_MAX
 * 高
 * ：发出提示音	IMPORTANCE_DEFAULT	PRIORITY_DEFAULT
 * 中
 * ：无提示音	IMPORTANCE_LOW	PRIORITY_LOW
 * 低
 * ：无提示音，且不会在状态栏中显示。	IMPORTANCE_MIN	PRIORITY_MIN
 *
 * Android 13 / API 33需要声明权限
 * [android.permission.POST_NOTIFICATIONS]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/13
 */
class DslNotify {

    companion object {

        val _notifyIds: MutableList<Int> = mutableListOf()

        /**默认的通知图标 */
        var DEFAULT_NOTIFY_ICON: Int = R.drawable.lib_notify_icon

        /**默认通知栏大图的大小, px, 请手动乘以 dpi*/
        var DEFAULT_NOTIFY_LARGE_ICON_SIZE = 40

        fun cancelNotify(context: Context?, id: Int?) {
            if (id == null) {
                return
            }
            val notificationManager: NotificationManagerCompat =
                NotificationManagerCompat.from(context ?: app())
            notificationManager.cancel(id)
            _notifyIds.remove(id)
        }

        fun cancelNotify(id: Int?) {
            cancelNotify(app(), id)
        }

        fun cancelNotifyList(idList: Collection<Int>?) {
            idList?.forEach {
                cancelNotify(app(), it)
            }
        }

        fun cancelNotifyLast(context: Context?) {
            _notifyIds.lastOrNull()?.run {
                cancelNotify(context, this)
            }
        }

        /**取消所有通知*/
        fun cancelNotifyAll(context: Context? = app()) {
            val notificationManager: NotificationManagerCompat =
                NotificationManagerCompat.from(context ?: app())
            notificationManager.cancelAll()
            _notifyIds.clear()
        }

        fun getNotificationChannel(
            channelId: String,
            context: Context = app()
        ): NotificationChannel? {
            return getNotificationChannel(context, channelId)
        }

        /**获取[channelId]对应的通道信息, 可以检测通道通知是否被关闭*/
        fun getNotificationChannel(context: Context, channelId: String): NotificationChannel? {
            return NotificationManagerCompat.from(context).getNotificationChannel(channelId)
            /*val manager: NotificationManager? =
                (context ?: app()).getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                try {
                    val channel: NotificationChannel = manager.getNotificationChannel(channelId)
                    //channel.importance == NotificationManager.IMPORTANCE_NONE
                    channel
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }*/
        }

        /**打开通道设置页*/
        fun openNotificationChannelSetting(context: Context = app(), channelId: String? = null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = if (channelId.isNullOrEmpty()) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                } else {
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                    }
                }
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                intent.baseConfig(context)
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:" + context.packageName)
                //intent.data = Uri.fromParts("package", context.packageName, null)
                intent.baseConfig(context)
                context.startActivity(intent)
            }
        }

        fun deleteNotificationChannel(channelId: String, context: Context? = app()) {
            deleteNotificationChannel(context, channelId)
        }

        /**删除通道*/
        fun deleteNotificationChannel(context: Context?, channelId: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager: NotificationManager? = (context ?: app()).getSystemService(
                    NOTIFICATION_SERVICE
                ) as NotificationManager?
                try {
                    manager?.deleteNotificationChannel(channelId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
            intent.baseConfig(context)
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                flags.pendingIntentMutableFlag(),
                options
            )
        }

        fun pendingBroadcast(
            context: Context,
            action: String,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            return pendingBroadcast(context, Intent(action), requestCode, flags)
        }

        fun pendingBroadcast(
            context: Context,
            intent: Intent,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags.pendingIntentMutableFlag()
            )
        }

        fun pendingService(
            context: Context,
            serviceClass: Class<out Service>,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            val intent = Intent(context, serviceClass)
            return pendingService(context, intent, requestCode, flags)
        }

        fun pendingService(
            context: Context,
            intent: Intent,
            requestCode: Int = 0x999,
            flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
        ): PendingIntent {
            return PendingIntent.getService(
                context,
                requestCode,
                intent,
                flags.pendingIntentMutableFlag()
            )
        }

        fun action(
            title: CharSequence,
            pendingIntent: PendingIntent,
            icon: Int = 0,
            action: NotificationCompat.Action.Builder.() -> Unit = {}
        ): NotificationCompat.Action {
            val builder = NotificationCompat.Action.Builder(icon, title, pendingIntent)
            //https://developer.android.google.cn/training/notify-user/build-notification.html#reply-action
            //builder.addRemoteInput()//添加快速回复action
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
    var channelImportance: Int = NotificationManagerCompat.IMPORTANCE_HIGH

    /**通道id*/
    var channelId: String? = null
        get() {
            return field ?: "$channelName"
        }

    /**通道名称*/
    var channelName: CharSequence = "DefaultChannel"

    /**通道描述文本*/
    var channelDescription: String? = null

    /**通道是否要显示小圆点*/
    var channelShowBadge = true

    /**允许气泡通知*/
    var channelAllowBubbles = true

    /**激活通知灯*/
    var channelEnableLights = true

    /**通知灯颜色*/
    var channelLightColor = 0

    /**激活震动*/
    var channelEnableVibration = true

    /**通道的通知声, channelImportance至少是default以上*/
    var channelSoundUri: Uri? = null
    var channelAudioAttributes: AudioAttributes? = null

    var onConfigChannel: (NotificationChannel) -> Unit = {}

    /**创建通道,反复调用这段代码是安全的，因为创建现有通知渠道不会执行任何操作。*/
    fun _createNotifyChannel(context: Context = lastContext) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                channelImportance
            )

            channel.run {
                setShowBadge(channelShowBadge)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(channelAllowBubbles)
                }
                enableLights(channelEnableLights)
                enableVibration(channelEnableVibration)
                lightColor = channelLightColor
                description = channelDescription

                //system属性
                //setBypassDnd()

                channelSoundUri?.run {
                    setSound(this, channelAudioAttributes ?: Notification.AUDIO_ATTRIBUTES_DEFAULT)
                }

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
     * https://developer.android.google.cn/training/notify-user/expanded.html#media-style
     * https://developer.android.google.cn/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html#mediastyle-notifications*/
    var styleMediaSessionToken: MediaSessionCompat.Token? = null

    /**要显示[notifyActions]中的那些action的索引, 最多3个*/
    var styleMediaShowActions: List<Int>? = null

    fun _createStyle(context: Context): NotificationCompat.Style? {
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
            if (styleMediaSessionToken != null || styleMediaShowActions != null) {
                style = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle().also {
                    it.setShowCancelButton(true)
                    it.setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
                    styleMediaSessionToken?.run { it.setMediaSession(this) }
                    styleMediaShowActions?.run { it.setShowActionsInCompactView(*this.toIntArray()) }
                }
            }
        }
        return style
    }

    //</editor-fold desc="Style配置">

    //<editor-fold desc="通知相关配置">

    var notifyId: Int = (System.currentTimeMillis() and 0xFFFFFFF).toInt()

    /**必须的最小成员变量,一般建议在24dp×24dp, 支持svg, 但是色彩会丢失, 系统会自动使用colorFilter
     * png 可以支持彩色(需要高版本支持api29单色效果测试通过)
     * Android 5.0 SVG显示有系统背景
     *
     * android 13 pixel 6 透明背景彩色png/svg测试通过(彩色会变成白色)
     * 因为系统会上色成白色, 所以如果不是透明的背景, 全部会变成白色
     * */
    var notifySmallIcon: Int = DEFAULT_NOTIFY_ICON

    /**通知栏图标的强调色, 在下拉后的图标和默认label会使用此颜色
     * android 13 pixel 6 图片的背景色
     * */
    var notifyColor: Int = NotificationCompat.COLOR_DEFAULT

    /**通知栏, 右边的大图, 不支持SVG?*/
    var notifyLargeIcon: Bitmap? = null

    /**通知标题.(标准配置项1)*/
    var notifyTitle: CharSequence? = null

    /**通知正文文本, 默认情况下，通知的文字内容会被截断以放在一行。使用[setStyle].(标准配置项2)*/
    var notifyText: CharSequence? = null
        set(value) {
            field = value
            if (value != null) {
                notifyTickerText = value
            }
        }

    /**右下角, 时间下面描述[notifyText]的文本信息*/
    var notifyInfo: CharSequence? = null

    /**显示在[notifyTitle]后面的文本, 当一组中有多个通知, 这个就是title*/
    var notifySubText: CharSequence? = null

    /**首次通知时, 立马就要显示的文本, 高版本测试没效果.*/
    var notifyTickerText: CharSequence? = null

    /**
     * 通知的优先级, 通道还有一个重要性.
     * 首次横幅通知,需要优先级高.
     * 低优先级, 将不会显示首次的横幅通知
     * */
    var notifyPriority = NotificationCompat.PRIORITY_HIGH

    /**设置通知时的效果, 比如 震动, 声音, 灯光等*/
    var notifyDefaults = NotificationCompat.DEFAULT_VIBRATE

    /**[NotificationCompat.CATEGORY_MESSAGE]
     * https://developer.android.google.cn/training/notify-user/build-notification.html#system-category*/
    var notifyCategory: String? = null

    /**通知点击事件[Intent]
     * [com.angcyo.library.component.DslNotify.Companion.pendingActivity]*/
    var notifyContentIntent: PendingIntent? = null

    /**当点击通知时, 是否自动关闭通知. 配置[notifyContentIntent]才有效果*/
    var notifyAutoCancel = true

    /**用户一键清除通知时, 触发的意图.[NotificationManager.cancel]不会触发*/
    var notifyDeleteIntent: PendingIntent? = null

    /**立即要展示, 而不是发送到状态栏. 横幅通知. 不会消失. 通常用来实现电话通知*/
    var notifyFullScreenIntent: PendingIntent? = null

    var notifyFullScreenIntentHighPriority = true

    /**一个通知最多可以提供三个操作按钮
     * https://developer.android.google.cn/training/notify-user/build-notification.html#Actions*/
    var notifyActions: List<NotificationCompat.Action>? = null

    /**通知可见性,
     * [VISIBILITY_PUBLIC]在所有界面上都显示内容,
     * [VISIBILITY_PRIVATE]隐藏隐私信息,
     * [VISIBILITY_SECRET]锁屏不展示
     * https://developer.android.google.cn/training/notify-user/build-notification.html#lockscreenNotification
     * */
    var notifyVisibility = VISIBILITY_PUBLIC

    /**需要开启[channelShowBadge]后, 长按应用桌面图标, 弹出的菜单会提示消息未读数*/
    var notifyNumber: Int = 1

    /**进度, 大于等于0时, 激活进度
     * https://developer.android.google.cn/training/notify-user/build-notification.html#progressbar
     * [notifyProgressIndeterminate]
     * */
    var notifyProgress = undefined_int

    // When done, update the notification one more time to remove the progress bar
    var notifyProgressMax = 100
    var notifyProgressIndeterminate = false

    /**通知的时间*/
    var notifyWhen: Long = System.currentTimeMillis()

    /**是否需要显示通知的时间*/
    var notifyShowWhen = true

    /**正在进行的通知, 不允许侧滑删除*/
    var notifyOngoing = false
        set(value) {
            field = value
            if (value) {
                notifyAutoCancel = false
            }
        }

    /**通知的声音 [channelSoundUri]*/
    var notifySoundUri: Uri? = null

    var onConfigNotify: (NotificationCompat.Builder) -> Unit = {}

    /**
     * setContent 设置普通视图，高度限制为 64 dp
     * setCustomContentView设置普通视图，高度限制为 64 dp
     * setCustomBigContentView() 设置扩展视图，高度可以扩展到256dp
     * setCustomHeadsUpContentView() 设置浮动通知视图
     */
    var notifyContentView: RemoteViews? = null

    /**如果设置了,[notifyContentView] 会优先使用这个*/
    var notifyCustomContentView: RemoteViews? = null

    /**高度更高的[notifyContentView], 如果设置了, 会优先于[notifyContentView]使用*/
    var notifyCustomBigContentView: RemoteViews? = null

    /**如果设置了, 横幅通知, 会优先使用这个*/
    var notifyCustomHeadsUpContentView: RemoteViews? = null

    /**
     * 是否只通知本地设备, 远程设备不通知. 比如手表
     * https://developer.android.google.cn/training/notify-user/build-notification.html#Updating
     * */
    var notifyLocalOnly = false

    /**是否只在第一次显示通知时, 有声音, 震动提示*/
    var notifyOnlyAlertOnce = true

    /**毫秒
     * https://developer.android.google.cn/training/notify-user/build-notification.html#Removing*/
    var notifyTimeout: Long = -1

    /**创建通知*/
    fun _createNotify(context: Context): Notification {
        val builder = NotificationCompat.Builder(context, channelId!!)
        builder.run {
            setSmallIcon(notifySmallIcon)
            color = notifyColor

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
            setNumber(notifyNumber)

            //PRIORITY_HIGH 就会有横幅通知, 并且会自动消失
            priority = notifyPriority
            setDefaults(notifyDefaults)
            notifyCategory?.run { setCategory(this) }

            notifySoundUri?.run {
                setSound(this)
            }

            _createStyle(context)?.run {
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

            //progress
            if (notifyProgressIndeterminate) {
                setProgress(notifyProgressMax, notifyProgress, notifyProgressIndeterminate)
            } else if (notifyProgress >= 0) {
                setProgress(
                    notifyProgressMax,
                    min(notifyProgress, notifyProgressMax),
                    notifyProgressIndeterminate
                )
            }

            //custom
            notifyContentView?.run { setContent(this) }
            notifyCustomContentView?.run { setCustomContentView(this) }
            notifyCustomBigContentView?.run { setCustomBigContentView(this) }
            notifyCustomHeadsUpContentView?.run { setCustomHeadsUpContentView(this) }

            setLocalOnly(notifyLocalOnly)
            setOnlyAlertOnce(notifyOnlyAlertOnce)

            if (notifyTimeout > 0) {
                setTimeoutAfter(notifyTimeout)
            }

            notifyFlags

            onConfigNotify(this)
        }
        return builder.build().apply {
            if (notifyFlags != undefined_int) {
                flags = notifyFlags
            }
        }
    }

    /**
     * Notification.FLAG_SHOW_LIGHTS         //三色灯提醒，在使用三色灯提醒时候必须加该标志符
     * Notification.FLAG_ONGOING_EVENT       //发起正在运行事件（活动中）
     * Notification.FLAG_INSISTENT           //让声音、振动无限循环，直到用户响应 （取消或者打开）
     * Notification.FLAG_ONLY_ALERT_ONCE     //发起Notification后，铃声和震动均只执行一次
     * Notification.FLAG_AUTO_CANCEL         //用户单击通知后自动消失
     * Notification.FLAG_NO_CLEAR            //只有全部清除时，Notification才会清除 ，不清楚该通知(QQ的通知无法清除，就是用的这个)
     * Notification.FLAG_FOREGROUND_SERVICE  //表示正在运行的服务
     */
    var notifyFlags: Int = undefined_int

    fun doIt(context: Context = lastContext): Int {
        _createNotifyChannel(context)
        val notification = _createNotify(context)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notifyId, notification)

        if (!_notifyIds.contains(notifyId)) {
            _notifyIds.add(notifyId)
        }
        return notifyId
    }

    //</editor-fold desc="通知相关配置">
}

//<editor-fold desc="快速构建">

/**快速创建配置通知*/
fun dslBuildNotify(context: Context = app(), action: DslNotify.() -> Unit): Notification {
    return DslNotify().run {
        action()
        _createNotifyChannel(context)
        _createNotify(context)
    }
}

/**快速创建配置通知, 并显示
 * 返回通知id*/
fun dslNotify(context: Context = app(), action: DslNotify.() -> Unit): Int {
    return DslNotify().run {
        action()
        doIt(context)
    }
}

/**快速显示一个通知*/
fun dslNotify(
    context: Context = app(),
    title: CharSequence?,
    content: CharSequence?,
    action: DslNotify.() -> Unit
): Int {
    return DslNotify().run {
        notifyTitle = title
        notifyText = content
        action()
        doIt(context)
    }
}

/**取消通知*/
fun Int.cancelNotify() = DslNotify.cancelNotify(this)

/**取消通知集合*/
fun Collection<Int>.cancelNotifyList() = DslNotify.cancelNotifyList(this)

//</editor-fold desc="快速构建">

//<editor-fold desc="通知扩展">

/**通知的基础简单配置*/
fun DslNotify.single(
    title: CharSequence? = null,
    content: CharSequence? = null
) {
    notifyTitle = title
    notifyText = content
}

/**轻提示, 显示在次要通知栏里面, 没有声音/震动/横幅提醒*/
fun DslNotify.low() {
    channelImportance = NotificationManagerCompat.IMPORTANCE_LOW
    notifyPriority = NotificationCompat.PRIORITY_LOW
    notifyDefaults = NotificationCompat.DEFAULT_LIGHTS
}

/**强提示*/
fun DslNotify.high() {
    channelImportance = NotificationManagerCompat.IMPORTANCE_HIGH
    notifyPriority = NotificationCompat.PRIORITY_HIGH
    notifyDefaults = NotificationCompat.DEFAULT_ALL
}

/**快速设置通知点击事件*/
fun DslNotify.clickActivity(
    intent: Intent?,
    requestCode: Int = 0x999,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    options: Bundle? = null,
    context: Context = app()
) {
    if (intent != null) {
        notifyContentIntent =
            DslNotify.pendingActivity(context, intent, requestCode, flags, options)
    }
}

//---

/**应用程序的通知是否打开了, 通知的总开关
 * App的通知是否打开了, 通知的总开关. Toast也受影响
 * */
fun isNotificationsEnabled() = NotificationManagerCompat.from(app()).areNotificationsEnabled()

/**通知通道是否激活*/
fun NotificationChannel?.isEnable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this?.importance != NotificationManagerCompat.IMPORTANCE_NONE
    } else {
        true
    }
}

/**指定的通知通道是否激活*/
fun String.isChannelEnable() = DslNotify.getNotificationChannel(this).isEnable()

/**打开通知通道设置*/
fun String.openNotificationChannelSetting(context: Context = app()) {
    DslNotify.openNotificationChannelSetting(context, this)
}

/**打开通知总开关设置界面*/
fun openNotificationSetting() {
    DslIntent.toNotifySetting()
}

//---

/**
 * [PendingIntent.FLAG_MUTABLE]   33554432 可变
 * [PendingIntent.FLAG_IMMUTABLE] 67108864 不可变
 * [PendingIntent.FLAG_NO_CREATE] 536870912
 * [PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT] 16777216
 * [mutableFlag]
 *
 * ```
 * Targeting U+ (version 34 and above) disallows creating or retrieving a PendingIntent with FLAG_MUTABLE,
 * an implicit Intent within and without FLAG_NO_CREATE and FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT for security reasons.
 * To retrieve an already existing PendingIntent, use FLAG_NO_CREATE, however,
 * to create a new PendingIntent with an implicit Intent use FLAG_IMMUTABLE.
 * ```
 * */
fun Int.pendingIntentMutableFlag(
    mutableFlag: Int = 33554432,
    createFlag: Int = PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
): Int {
    var flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //android 12
        if (this.have(PendingIntent.FLAG_MUTABLE) || this.have(PendingIntent.FLAG_IMMUTABLE)) {
            this
        } else {
            this or mutableFlag
        }
    } else {
        this
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        flag = if (flag.have(createFlag)) {
            flag
        } else {
            flag or createFlag
        }
    }

    return flag
}

//</editor-fold desc="通知扩展">
