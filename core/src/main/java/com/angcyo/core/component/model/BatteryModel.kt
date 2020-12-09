package com.angcyo.core.component.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.*
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.angcyo.library.isMain


/**
 * 电量监听
 *
 * https://www.jianshu.com/p/6c8286d451c8
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class BatteryModel : ViewModel() {

    /**电量数据即监听*/
    val batteryData: MutableLiveData<BatteryBean> = MutableLiveData()

    var _batteryReceiver: BatteryReceiver? = null

    var _context: Context? = null

    /**开始观察电量*/
    fun observe(context: Context) {
        if (_batteryReceiver == null) {
            _batteryReceiver = BatteryReceiver()
            _context = context.applicationContext
            context.applicationContext.registerReceiver(
                _batteryReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_BATTERY_CHANGED)
                    //addAction(Intent.ACTION_BATTERY_LOW)
                    //addAction(Intent.ACTION_BATTERY_OKAY)
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //    addAction(ACTION_CHARGING)
                    //    addAction(ACTION_DISCHARGING)
                    //}
                })
        }
        load(context)
    }

    /**移除观察*/
    fun remove() {
        _batteryReceiver?.let {
            _context?.unregisterReceiver(it)
        }
        _batteryReceiver = null
    }

    /**主动读取电量信息, 能获取到的信息比较少. 电量百分比和模糊的电池状态*/
    fun load(context: Context): BatteryBean {
        val manager = context.getSystemService(BATTERY_SERVICE) as BatteryManager?

        val bean = BatteryBean()

        manager?.let {
            ///电池剩余电量
            bean.level = it.getIntProperty(BATTERY_PROPERTY_CAPACITY)
            ///获取电池满电量数值
            bean.scale = 100
            ///获取电池状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bean.status = it.getIntProperty(BATTERY_PROPERTY_STATUS)
            } else {
                bean.status = it.getIntProperty(6)
            }

            //电池剩余容量 毫安(mA)
            val intProperty = it.getIntProperty(BATTERY_PROPERTY_ENERGY_COUNTER)
            //电池容量 单位微安(uA). 3542203
            val intProperty1 = it.getIntProperty(BATTERY_PROPERTY_CHARGE_COUNTER)
            val intProperty3 = it.getIntProperty(BATTERY_PROPERTY_CURRENT_AVERAGE)

            //瞬间电流 单位微安(uA), 整数充电, 负数放电. -305
            val currentNow = it.getIntProperty(BATTERY_PROPERTY_CURRENT_NOW)
            if (currentNow > 0) {
                bean.status = BATTERY_STATUS_CHARGING
            }

            //发送数据
            if (isMain()) {
                batteryData.value = bean
            } else {
                batteryData.postValue(bean)
            }
        }
        return bean
    }

    //电量广播监听
    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val bean = BatteryBean()
                ///电池剩余电量
                bean.level = intent.getIntExtra(EXTRA_LEVEL, 0)
                ///获取电池满电量数值
                bean.scale = intent.getIntExtra(EXTRA_SCALE, 0)
                ///获取电池技术支持
                bean.technology = intent.getStringExtra(EXTRA_TECHNOLOGY)
                ///获取电池状态
                bean.status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN)
                ///获取电源信息
                bean.plugged = intent.getIntExtra(EXTRA_PLUGGED, 0)
                ///获取电池健康度
                bean.health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN)
                ///获取电池电压
                bean.voltage = intent.getIntExtra(EXTRA_VOLTAGE, 0)
                //最大电压
                bean.maxVoltage = intent.getIntExtra("max_charging_voltage", 0)
                ///获取电池温度
                bean.temperature = intent.getIntExtra(EXTRA_TEMPERATURE, 0)

                //发送数据
                batteryData.value = bean
            }
        }
    }
}

data class BatteryBean(
    //获取电池健康度
    var health: Int = -1,
    //获取电池电压,单位微伏(uV)
    var voltage: Int = -1,
    //最大电压
    var maxVoltage: Int = -1,
    //获取电池温度
    var temperature: Int = -1,
    //获取电源信息, 是否插入电源, 0表示正在使用电池
    var plugged: Int = -1,
    //获取电池状态
    var status: Int = -1,
    ///获取电池技术支持
    var technology: String? = null,
    ///电池剩余电量,100
    var level: Int = -1,
    ///获取电池满电量数值,100
    var scale: Int = -1
)

/**是否正在充电*/
fun BatteryBean.isCharging(): Boolean = status == BATTERY_STATUS_CHARGING || plugged > 0

/**电池健康状态*/
fun Int.toBatteryHealthStr(): String = when (this) {
    BATTERY_HEALTH_GOOD -> "BATTERY_HEALTH_GOOD" //2
    BATTERY_HEALTH_OVERHEAT -> "BATTERY_HEALTH_OVERHEAT"//3
    BATTERY_HEALTH_DEAD -> "BATTERY_HEALTH_DEAD"//4
    BATTERY_HEALTH_OVER_VOLTAGE -> "BATTERY_HEALTH_OVER_VOLTAGE"//5
    BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "BATTERY_HEALTH_UNSPECIFIED_FAILURE"//6
    BATTERY_HEALTH_COLD -> "BATTERY_HEALTH_COLD"//7
    BATTERY_HEALTH_UNKNOWN -> "BATTERY_HEALTH_UNKNOWN"//1
    else -> "BATTERY_HEALTH_UNKNOWN"
}

/**电池充电状态*/
fun Int.toBatteryPluggedStr(): String = when (this) {
    0 -> "BATTERY"
    BATTERY_PLUGGED_AC -> "BATTERY_PLUGGED_AC"  //1
    BATTERY_PLUGGED_USB -> "BATTERY_PLUGGED_USB" //2
    BATTERY_PLUGGED_WIRELESS -> "BATTERY_PLUGGED_WIRELESS" //4
    else -> "BATTERY_PLUGGED_UNKNOWN"
}

/**电池状态*/
fun Int.toBatteryStatusStr(): String = when (this) {
    BATTERY_STATUS_CHARGING -> "BATTERY_STATUS_CHARGING" //充电中 2
    BATTERY_STATUS_DISCHARGING -> "BATTERY_STATUS_DISCHARGING" //放电中 3
    BATTERY_STATUS_NOT_CHARGING -> "BATTERY_STATUS_NOT_CHARGING" //未充电 4
    BATTERY_STATUS_FULL -> "BATTERY_STATUS_FULL" //满电 5
    BATTERY_STATUS_UNKNOWN -> "BATTERY_STATUS_UNKNOWN" //未知 1
    else -> "BATTERY_STATUS_UNKNOWN"
}