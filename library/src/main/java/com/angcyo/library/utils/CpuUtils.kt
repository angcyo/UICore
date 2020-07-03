package com.angcyo.library.utils

import android.util.Log
import java.io.*
import java.util.*
import java.util.regex.Pattern

/**
 * https://github.com/sufadi/AndroidCpuTools
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-7-1
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object CpuUtils {
    private val TAG = CpuUtils::class.java.simpleName

    /**
     * It's also good way to get cpu core number
     */
    val cpuCoreNum: Int get() = Runtime.getRuntime().availableProcessors()

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     *
     * Source: http://stackoverflow.com/questions/7962155/
     *
     * @return The number of cores, or 1 if failed to get result
     */
    val numCpuCores: Int
        get() = try {
            // Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")
            // Filter to only list the devices we care about
            val files =
                dir.listFiles { file -> // Check if filename is "cpu", followed by a single digit number
                    Pattern.matches("cpu[0-9]+", file.name)
                }
            // Return the number of cores (virtual CPU devices)
            files.size
        } catch (e: Exception) {
            // Default to return 1 core
            Log.e(TAG, "Failed to count number of cores, defaulting to 1", e)
            1
        }

    /**
     * 64 系统判断
     *
     * @return
     */
    val isCpu64: Boolean
        get() {
            var result = false
            if (BuildHelper.isCpu64 || ProcCpuInfo.isCpu64) {
                result = true
            }
            return result
        }

    //大核 CPU频率
    val cpuMaxFreq: Long
        get() {
            var max = 0L
            getAllCpuFreqList().forEach {
                if (it > max) {
                    max = it
                }
            }
            return max
        }

    //小核 CPU频率
    val cpuMinFreq: Long
        get() {
            var min = Long.MAX_VALUE
            getAllCpuFreqList().forEach {
                if (it in 1 until min) {
                    min = it
                }
            }
            return min
        }

    /**
     * CPU 最大频率, 能够达到的最小频率
     *
     * @return
     */
    val cpuMaxFreqInfo: Long
        get() {
            var result = 0L
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"))
                if (br.readLine().also { line = it } != null) {
                    result = line.toLong()
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * CPU 最小频率, 能够达到的最小频率
     *
     * @return
     */
    val cpuMinFreqInfo: Long
        get() {
            var result = 0L
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"))
                if (br.readLine().also { line = it } != null) {
                    result = line.toLong()
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * 可调节 CPU 频率档位
     *
     * @return
     */
    val cpuAvailableFrequenciesSimple: String?
        get() {
            var result: String? = null
            try {
                var line: String?
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies"))
                if (br.readLine().also { line = it } != null) {
                    result = line
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * 可调节 CPU 频率档位
     *
     * @return
     */
    val cpuAvailableFrequencies: List<Long>
        get() {
            val result: MutableList<Long> = ArrayList()
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies"))
                if (br.readLine().also { line = it } != null) {
                    val list = line.split("\\s+".toRegex()).toTypedArray()
                    for (value in list) {
                        val freq = value.toLong()
                        result.add(freq)
                    }
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * 可调节 CPU 频率档位
     *
     * @return
     */
    val cpuFreqList: List<Int>
        get() {
            val result: MutableList<Int> = ArrayList()
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies"))
                if (br.readLine().also { line = it } != null) {
                    val list = line.split("\\s+".toRegex()).toTypedArray()
                    for (value in list) {
                        result.add(value.toInt())
                    }
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    val cpuAFreqList: Array<String>?
        get() {
            var result: Array<String>? = null
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies"))
                if (br.readLine().also { line = it } != null) {
                    result = line.split("\\s+".toRegex()).toTypedArray()
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * CPU 调频策略
     *
     * @return
     */
    val cpuGovernor: String?
        get() {
            var result: String? = null
            try {
                var line: String?
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"))
                if (br.readLine().also { line = it } != null) {
                    result = line
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * CPU 支持的调频策略
     *
     * @return
     */
    val cpuAvailableGovernorsSimple: String?
        get() {
            var result: String? = null
            try {
                var line: String?
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors"))
                if (br.readLine().also { line = it } != null) {
                    result = line
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    val cpuAvailableGovernorsList: Array<String>?
        get() {
            var result: Array<String>? = null
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors"))
                if (br.readLine().also { line = it } != null) {
                    result = line.split("\\s+".toRegex()).toTypedArray()
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * CPU 支持的调频策略
     *
     * @return
     */
    val cpuAvailableGovernors: List<String>
        get() {
            val result: MutableList<String> =
                ArrayList()
            try {
                var line: String
                val br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors"))
                if (br.readLine().also { line = it } != null) {
                    val list = line.split("\\s+".toRegex()).toTypedArray()
                    for (value in list) {
                        result.add(value)
                    }
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

    /**
     * Get cpu's current frequency
     * unit:KHZ
     * 获取cpu当前频率,单位 HZ
     *
     * @return
     */
    fun getCpuCurFreq(): List<String> {
        val result: MutableList<String> = mutableListOf()
        val cpuCoreNumber = numCpuCores
        var br: BufferedReader? = null
        try {
            for (i in 0 until cpuCoreNumber) {
                val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
                val file = File(path)
                if (file.exists()) {
                    br = BufferedReader(FileReader(path))
                    val line = br.readLine()
                    if (line != null) {
                        //CPU %1$d : Freq %2$s Hz
                        result.add(String.format("CPU %d : Freq %s Hz", i, line))
                    }
                } else {
                    result.add(
                        //CPU %1$d: Stopped
                        String.format("CPU %d: Stopped", i)
                    )
                }
                br?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /**返回每个CPU核心, 对应的CPU频率. [1, 960000], [2, 2803200], 频率-1表示 未工作*/
    fun getAllCpuFreqPairList(): List<Pair<Int, Long>> {
        val result: MutableList<Pair<Int, Long>> = mutableListOf()
        val cpuCoreNumber = numCpuCores
        var br: BufferedReader? = null
        try {
            for (i in 0 until cpuCoreNumber) {
                val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
                val file = File(path)
                if (file.exists()) {
                    br = BufferedReader(FileReader(path))
                    val line = br.readLine()
                    if (line != null) {
                        //CPU %1$d : Freq %2$s Hz
                        result.add(i to (line.toLongOrNull() ?: 0L))
                    }
                } else {
                    result.add(i to -1L)
                }
                br?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun getAllCpuFreqList(): List<Long> {
        val result = mutableListOf<Long>()
        val cpuCoreNumber = numCpuCores
        var br: BufferedReader? = null
        try {
            for (i in 0 until cpuCoreNumber) {
                val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
                val file = File(path)
                if (file.exists()) {
                    br = BufferedReader(FileReader(path))
                    val line = br.readLine()
                    if (line != null) {
                        //CPU %1$d : Freq %2$s Hz
                        result.add(line.toLongOrNull() ?: 0L)
                    }
                } else {
                    result.add(-1L)
                }
                br?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }


    /**
     * Get cpu's current frequency
     * unit:KHZ
     * 获取cpu当前频率,单位 HZ
     *
     * @return
     */
    fun getCpuOnlineStatus(): List<String> {
        val result: MutableList<String> =
            ArrayList()
        val mCpuCoreNumber = numCpuCores
        var br: BufferedReader? = null
        try {
            for (i in 0 until mCpuCoreNumber) {
                br =
                    BufferedReader(FileReader("/sys/devices/system/cpu/cpu$i/online"))
                val line = br.readLine()
                if (line != null) {
                    //CPU %1$d Online Status: %2$s
                    result.add(
                        String.format(
                            "CPU %d Online Status: %s", i,
                            if ("1" == line) "Online" else "Offline"
                        )
                    )
                }
                br.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /**
     * CPU 场景配置文件
     *
     * @return
     */
    val cpuSceneInfo: List<String>
        get() {
            val result: MutableList<String> =
                ArrayList()
            var br: BufferedReader? = null
            try {
                var line: String
                br = BufferedReader(FileReader("/system/vendor/etc/perfservscntbl.txt"))
                result.add("/system/vendor/etc/perfservscntbl.txt")
                while (br.readLine().also { line = it } != null) {
                    result.add(line)
                }
                result.add("/system/vendor/etc/perf_whitelist_cfg.xml")
                br =
                    BufferedReader(FileReader("/system/vendor/etc/perf_whitelist_cfg.xml"))
                while (br.readLine().also { line = it } != null) {
                    result.add(line)
                }
                br.close()
            } catch (e: FileNotFoundException) {
                result.add(e.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (br != null) {
                    try {
                        br.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return result
        }

    /**
     * CPU 电压
     *
     * @return
     */
    val cpuVoltage: List<String>
        get() {
            val result: MutableList<String> =
                ArrayList()
            var br: BufferedReader? = null
            try {
                var line: String
                br =
                    BufferedReader(FileReader("/proc/cpufreq/MT_CPU_DVFS_LL/cpufreq_oppidx"))
                while (br.readLine().also { line = it } != null) {
                    result.add(line)
                }
                br.close()
            } catch (e: FileNotFoundException) {
                result.add(e.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (br != null) {
                    try {
                        br.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return result
        }
}