package com.angcyo.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import android.view.View
import android.view.Window
import com.angcyo.library.getAppString
import com.angcyo.library.getAppVersionCode
import com.angcyo.library.getAppVersionName
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Device {

    var psuedoID: String? = null

    //获得独一无二的Psuedo ID
    fun getUniquePsuedoID(): String? {
        if (psuedoID != null) {
            return psuedoID
        }
        var result: String? = null
        var serial: String? = null
        val idShort = "35" +
                Build.BOARD.length % 10 +
                Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 +
                Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 +
                Build.HOST.length % 10 +
                Build.ID.length % 10 +
                Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 +
                Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 +
                Build.TYPE.length % 10 +
                Build.USER.length % 10 //13 位
        try {
            serial = Build::class.java.getField("SERIAL").toString()
            //API>=9 使用serial号
            result = UUID(idShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: Exception) { //serial需要一个初始化
            serial = "serial" // 随便一个初始化
            //使用硬件信息拼凑出来的15位号码
            result = UUID(idShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        }
        psuedoID = result
        return result
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    fun getAvailableMemory(context: Context): Long {
        val memoryInfo = getMemoryInfo(context)
        return memoryInfo.availMem
    }

    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    /**
     * 获取系统总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    fun getTotalMemorySize(): Long {
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            return subMemoryLine.replace("\\D+".toRegex(), "").toInt() * 1024L
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * SDCARD是否存
     */
    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    /**
     * 获取SDCARD总的存储空间
     *
     * @return
     */
    fun getTotalExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val totalBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                totalBlocks = stat.blockCountLong
            } else {
                blockSize = stat.blockSize.toLong()
                totalBlocks = stat.blockCount.toLong()
            }
            totalBlocks * blockSize
        } else {
            -1
        }
    }

    /**
     * 获取SDCARD剩余存储空间 kb单位->KB->MB
     *
     * @return
     */
    fun getAvailableExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val availableBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            availableBlocks * blockSize
        } else {
            -1
        }
    }

    fun deviceInfo(context: Context, builder: StringBuilder): StringBuilder {
        val pm: PackageManager = context.packageManager
        val pi: PackageInfo =
            pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        builder.append("psuedoID: ")
        builder.appendln(getUniquePsuedoID())
        builder.appendln()
        // cpu架构
        builder.append("CPU ABI: ")
        builder.appendln(Build.CPU_ABI)
        //        builder.appendln();
        builder.append("CPU ABI 2: ")
        builder.appendln(Build.CPU_ABI2)
        builder.appendln()
        builder.append("memoryClass: ")
        builder.appendln(manager.memoryClass)
        builder.append("largeMemoryClass: ")
        builder.appendln(manager.largeMemoryClass)
        builder.appendln()
        builder.append("手机内存大小:")
        builder.appendln(Formatter.formatFileSize(context, getTotalMemorySize()))
        //        builder.appendln();
//        builder.append("JVM可用内存大小:");
//        builder.appendln(Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
//        builder.appendln();
        val memoryInfo: ActivityManager.MemoryInfo = getMemoryInfo(context)
        builder.append("系统总内存:")
        builder.appendln(Formatter.formatFileSize(context, memoryInfo.totalMem))
        builder.append("系统剩余内存:")
        builder.appendln(Formatter.formatFileSize(context, memoryInfo.availMem))
        //        builder.append("是否内存警告:");
//        builder.appendln(memoryInfo.lowMemory);
//        builder.append("阈值:");
//        builder.appendln(Formatter.formatFileSize(context, memoryInfo.threshold));
//        builder.appendln();
//        builder.append("getNativeHeapSize:");
//        builder.appendln(Formatter.formatFileSize(context, Debug.getNativeHeapSize()));
//
//        builder.append("getNativeHeapAllocatedSize:");
//        builder.appendln(Formatter.formatFileSize(context, Debug.getNativeHeapAllocatedSize()));
//
//        builder.append("getNativeHeapFreeSize:");
//        builder.appendln(Formatter.formatFileSize(context, Debug.getNativeHeapFreeSize()));
        builder.appendln()
        builder.append("SD空间大小:")
        builder.appendln(
            Formatter.formatFileSize(
                context, getTotalExternalMemorySize()
            )
        )
        builder.append("SD可用空间大小:")
        builder.appendln(
            Formatter.formatFileSize(
                context, getAvailableExternalMemorySize()
            )
        )
        //        builder.appendln();
        return builder
    }

    /**设备屏幕信息*/
    fun screenInfo(context: Context, builder: StringBuilder): StringBuilder {
        builder.apply {

            val decorView = (context as? Activity)?.window?.decorView
            val contentView = (context as? Activity)?.findViewById<View>(Window.ID_ANDROID_CONTENT)

            val displayMetrics = context.resources.displayMetrics
            val widthDp: Float = displayMetrics.widthPixels / displayMetrics.density
            val heightDp: Float = displayMetrics.heightPixels / displayMetrics.density

            // 屏幕尺寸
            val width = displayMetrics.widthPixels / displayMetrics.xdpi
            //displayMetrics.heightPixels / displayMetrics.ydpi
            val height = (decorView?.measuredHeight ?: 0) / displayMetrics.ydpi
            val x = width.toDouble().pow(2.0)
            val y = height.toDouble().pow(2.0)
            val screenInches = sqrt(x + y)

            append("wp:").append(displayMetrics.widthPixels)
            append(" hp:").appendln(displayMetrics.heightPixels)

            append("dw:").append(decorView?.measuredWidth)
            append(" dh:").appendln(decorView?.measuredHeight)

            append("cw:").append(contentView?.measuredWidth)
            append(" ch:").appendln(contentView?.measuredHeight)

            append("wDp:").append(widthDp)
            append(" hDp:").append(heightDp)
            append(" dp:").append(displayMetrics.density)
            append(" sp:").append(displayMetrics.scaledDensity)
            append(" dpi:").appendln(displayMetrics.densityDpi)

            append("w:").append("%.02f".format(width))
            append(" h:").append("%.02f".format(height))
            append(" inches:").appendln("%.02f".format(screenInches))

            val dRect = Rect()
            val dPoint = Point()
            decorView?.getGlobalVisibleRect(dRect, dPoint)
            append(" d:").append(dRect)
            append(" d:").append(dPoint).appendln()

            val cRect = Rect()
            val cPoint = Point()
            contentView?.getGlobalVisibleRect(cRect, cPoint)
            append(" c:").append(cRect)
            append(" c:").append(cPoint)
        }
        return builder
    }

    fun buildString(builder: StringBuilder): StringBuilder {
        builder.apply {
            append(getAppVersionName()).append(":").append(getAppVersionCode())
            appendln()
            appendln(getAppString("user_name"))
            appendln(getAppString("build_time"))
            appendln(getAppString("os_name"))
        }
        return builder
    }
}