package com.angcyo.library.utils

import android.util.Log
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * https://github.com/sufadi/AndroidCpuTools
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-7-1
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object ProcCpuInfo {
    private val TAG = ProcCpuInfo::class.java.simpleName
    val cpuInfo: List<String>
        get() {
            val result: MutableList<String> =
                ArrayList()
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/proc/cpuinfo"))
                while (br.readLine().also { line = it } != null) {
                    result.add(line)
                }
                br.close()
            } catch (e: FileNotFoundException) {
                result.add(e.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /* /proc/cpuinfo
        processor       : 0
        Processor       : AArch64 Processor rev 4 (aarch64)
        model name      : AArch64 Processor rev 4 (aarch64)
        BogoMIPS        : 26.00
        Features        : fp asimd evtstrm aes pmull sha1 sha2 crc32
        CPU implementer : 0x41
        CPU architecture: 8
        CPU variant     : 0x0
        CPU part        : 0xd03
        CPU revision    : 4
    */
    @Throws(IOException::class)
    fun getFieldFromCpuinfo(field: String): String? {
        val br = BufferedReader(FileReader("/proc/cpuinfo"))
        val p = Pattern.compile("$field\\s*:\\s*(.*)")
        try {
            var line: String?
            while (br.readLine().also { line = it } != null) {
                val m = p.matcher(line)
                if (m.matches()) {
                    return m.group(1)
                }
            }
        } finally {
            br.close()
        }
        return null
    }

    /**
     * 获取 CPU 名称
     *
     * @return
     */
    val processor: String?
        get() {
            var result: String? = null
            try {
                result = getFieldFromCpuinfo("Processor")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    // D/CpuUtils: isCPU64 mProcessor = AArch64 Processor rev 4 (aarch64)
    val isCpu64: Boolean
        get() {
            var result = false
            var mProcessor: String? = null
            try {
                mProcessor = getFieldFromCpuinfo("Processor")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mProcessor != null) {
                // D/CpuUtils: isCPU64 mProcessor = AArch64 Processor rev 4 (aarch64)
                Log.d(TAG, "isCPU64 mProcessor = $mProcessor")
                if (mProcessor.contains("aarch64")) {
                    result = true
                }
            }
            return result
        }

    val architecture: String?
        get() {
            var result: String? = null
            try {
                var mCpuPart = getFieldFromCpuinfo("CPU part")
                Log.d(TAG, "mCpuPart = $mCpuPart")
                if (mCpuPart!!.startsWith("0x") || mCpuPart.startsWith("0X")) {
                    mCpuPart = mCpuPart.substring(2)
                }
                val mCpuPartId = Integer.valueOf(mCpuPart, 16)
                result = when (mCpuPartId) {
                    0x920 -> "ARM" + " ARM920T"
                    0x926 -> "ARM" + " ARM926EJ"
                    0xB36 -> "ARM" + " ARM1136"
                    0xB56 -> "ARM" + " ARM1156"
                    0xB76 -> "ARM" + " ARM1176"
                    0xC05 -> "ARM" + " Cortex-A5"
                    0xC07 -> "ARM" + " Cortex-A7"
                    0xC08 -> "ARM" + " Cortex-A8"
                    0xC09 -> "ARM" + " Cortex-A9"
                    0xC0C -> "ARM" + " Cortex-A12"
                    0xC0F -> "ARM" + " Cortex-A15"
                    0xC0E -> "ARM" + " Cortex-A17"
                    0xc14 -> "ARM" + " Cortex-R4"
                    0xc15 -> "ARM" + " Cortex-R5"
                    0xc20 -> "ARM" + " Cortex-M0"
                    0xc21 -> "ARM" + " Cortex-M1"
                    0xc23 -> "ARM" + " Cortex-M3"
                    0xc24 -> "ARM" + " Cortex-M4"
                    0xD03 -> "ARM" + " Cortex-A53"
                    0xD07 -> "ARM" + " Cortex-A57"
                    0x8 -> "NVIDIA" + " Tegra K1"
                    0xf -> "Qualcomm" + " Snapdragon S1/S2"
                    0x2d -> "Qualcomm" + " Snapdragon S2/S3"
                    0x4d -> "Qualcomm" + " Snapdragon S4"
                    0x6F -> "Qualcomm" + " Snapdragon 200/400/600/800"
                    else -> "0x" + Integer.toHexString(mCpuPartId)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }
}