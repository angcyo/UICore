package com.angcyo.library.component

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.angcyo.library.L
import com.angcyo.library.app
import java.util.*

/**
 *
 * Email:angcyo@126.com
 *
 *  <uses-permission android:name="android.permission.NFC" />
 *
 * @author angcyo
 * @date 2019/09/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object RNfc {

    val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(app())
    }

    /**
     * 返回NFC是否可用, 不可用, 弹出设置界面
     * */
    fun checkNfc(activity: Activity): Boolean {
        return if (isNfcEnable()) {
            true
        } else {
            startNfcSetting(activity)
            false
        }
    }

    fun startNfcSetting(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 16) {
            activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        } else {
            activity.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
    }

    /**
     * 是否支持NFC
     * */
    fun isNfcSupport(): Boolean {
        return nfcAdapter != null
    }

    /**是否支持并且激活了NFC*/
    fun isNfcEnable(): Boolean {
        return isNfcSupport() && nfcAdapter?.isEnabled == true
    }

    /**获取NFC标签*/
    fun getNfcTag(intent: Intent?): Tag? {
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            return getNfcTag(intent.extras)
        }
        return null
    }

    fun getNfcTag(bundle: Bundle?): Tag? {
        if (bundle == null) {
            return null
        }
        if (bundle.containsKey(NfcAdapter.EXTRA_TAG)) {
            return bundle.getParcelable(NfcAdapter.EXTRA_TAG)
        }
        return null
    }

    fun getNfcNdefMessages(intent: Intent?): List<NdefMessage>? {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            return getNfcNdefMessages(intent.extras)
        }
        return null
    }

    fun getNfcNdefMessages(bundle: Bundle?): List<NdefMessage>? {
        if (bundle != null) {
            bundle.getParcelableArray(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                // Process the messages array.
                return rawMessages.map { it as NdefMessage }
            }
        }
        return null
    }

    fun getNfcNdefStringMessages(intent: Intent?): List<String>? {
        val list = getNfcNdefMessages(intent)
        if (list.isNullOrEmpty()) {
            return null
        }
        val result = mutableListOf<String>()
        list.forEach {
            result.add(String(it.records[0].payload))
        }
        return result
    }

    /**启动NFC前台派发, 当发现标签的时候, 会触发[Intent]*/
    fun enableNfcForegroundDispatch(
        activity: Activity,
        pendingIntent: PendingIntent,
        techLists: Array<Array<String>> = arrayOf(
            arrayOf(IsoDep::class.java.name),
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name),
            arrayOf(MifareClassic::class.java.name),
            arrayOf(MifareUltralight::class.java.name),
        )
    ) {
        if (isNfcEnable()) {
            nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, null, techLists)
            //nfcAdapter?.enableForegroundNdefPush()
            //nfcAdapter?.setNdefPushMessage()
        }
    }

    fun enableNfcForegroundNdefPush(activity: Activity, message: NdefMessage) {
        if (isNfcEnable()) {
            nfcAdapter?.enableForegroundNdefPush(activity, message)
        }
    }

    fun disableNfcForegroundNdefPush(activity: Activity) {
        if (isNfcEnable()) {
            nfcAdapter?.disableForegroundNdefPush(activity)
        }
    }

    /**启动目标[Activity]*/
    fun enableNfcForegroundDispatch(activity: Activity, targetActivity: Class<*>? = null) {
        if (isNfcEnable()) {
            val intent = Intent(activity, targetActivity ?: activity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            val pendingIntent = PendingIntent.getActivity(
                activity, 0, intent, 0
            )
            enableNfcForegroundDispatch(activity, pendingIntent)
        }
    }

    /**关闭派发*/
    fun disableNfcForegroundDispatch(activity: Activity) {
        if (isNfcEnable()) {
            nfcAdapter?.disableForegroundDispatch(activity)
            //nfcAdapter?.disableForegroundNdefPush(activity)
        }
    }

    fun byte2HexString(bytes: ByteArray?): String {
        val ret = StringBuilder()
        if (bytes != null) {
            for (b in bytes) {
                ret.append(String.format(Locale.US, "%02X", b.toInt() and 0xFF))
            }
        }
        return ret.toString()
    }
}

fun ByteArray?.toHexString(): String = RNfc.byte2HexString(this)

fun Tag?.toLog(): String {
    val tag = this
    return buildString {
        if (tag == null) {
            append("--")
        } else {
            appendln("NFC标签信息↓")
            appendln("id(hex):${tag.id.toHexString()}")
            appendln("id(long):${tag.id.toHexString().toLong(16)}")
            appendln("Tech↓")
            tag.techList.forEachIndexed { index, s ->
                appendln(s)
            }

            appendln()
            append("$tag")
        }
        L.i(this)
    }
}