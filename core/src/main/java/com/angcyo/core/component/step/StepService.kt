package com.angcyo.core.component.step

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.angcyo.core.vmApp
import com.angcyo.http.rx.doBack
import com.angcyo.library.L

/**
 * 2020-11-19
 * 计步服务
 * https://www.jianshu.com/p/c51c58f4e2b1
 *
 * https://github.com/Guojiankai/JiBu
 * https://github.com/linglongxin24/DylanStepCount
 */
class StepService : Service(), SensorEventListener {

    private val stepModel: StepModel = vmApp()

    /** binder服务与activity交互桥梁 */
    private val lcBinder = LcBinder()

    /** 当前步数 */
    private var nowBuSu = 0L

    /** 传感器管理对象 */
    lateinit var sensorManager: SensorManager

    /** 加速度传感器中获取的步数 */
    private var mStepCount: StepCount? = null

    /** 数据回调接口，通知上层调用者数据刷新 */
    private var mCallback: StepUpdateUiCallBack? = null

    /** 每次第一次启动记步服务时是否从系统中获取了已有的步数记录 */
    private var hasRecord = false

    /** 系统中获取到的已有的步数 */
    private var hasStepCount = 0

    /** 上一次的步数 */
    private var previousStepCount = 0

    companion object {
        /** 存储计步传感器类型  Sensor.TYPE_STEP_COUNTER或者Sensor.TYPE_STEP_DETECTOR */
        private var stepSensorType = -1
    }

    override fun onCreate() {
        super.onCreate()
        //获取传感器管理类
        sensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        L.i("StepService—onCreate", "开启计步")
        doBack {
            startStepDetector()
            L.i("StepService—子线程", "startStepDetector()")
        }
    }

    /**
     * 选择计步数据采集的传感器
     * SDK大于等于19，开启计步传感器，小于开启加速度传感器
     */
    private fun startStepDetector() {
        val versionCodes = Build.VERSION.SDK_INT //取得SDK版本
        if (versionCodes >= Build.VERSION_CODES.KITKAT) {
            //SDK版本大于等于19开启计步传感器
            addCountStepListener()
        } else {        //小于就使用加速度传感器
            addBasePedometerListener()
        }
    }

    /** 启动计步传感器计步 */
    private fun addCountStepListener() {
        val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (countSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_COUNTER
            sensorManager.registerListener(
                this@StepService,
                countSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            L.i("计步传感器类型", "Sensor.TYPE_STEP_COUNTER")
        } else if (detectorSensor != null) {
            stepSensorType = Sensor.TYPE_STEP_DETECTOR
            sensorManager.registerListener(
                this@StepService,
                detectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            L.i("计步传感器类型", "Sensor.TYPE_STEP_DETECTOR")
        } else {
            addBasePedometerListener()
        }
    }

    /** 启动加速度传感器计步 */
    private fun addBasePedometerListener() {
        L.i("StepService", "加速度传感器")
        mStepCount = StepCount()
        mStepCount!!.setSteps(nowBuSu)
        //获取传感器类型 获得加速度传感器
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        val isAvailable = sensorManager.registerListener(
            mStepCount!!.stepDetector,
            sensor,
            SensorManager.SENSOR_DELAY_UI
        )

        mStepCount!!.initListener(object : StepValuePassListener {
            override fun stepChanged(steps: Long) {
                nowBuSu = steps //通过接口回调获得当前步数
                updateNotification() //更新步数通知
            }
        })
    }

    /**
     * 通知调用者步数更新 数据交互
     */
    private fun updateNotification() {
        L.i("StepService", "数据更新")
        stepModel.stepCountData.postValue(nowBuSu)
        mCallback?.updateUi(nowBuSu)
    }

    override fun onBind(intent: Intent): IBinder? {
        return lcBinder
    }

    /**
     * 计步传感器数据变化回调接口
     */
    override fun onSensorChanged(event: SensorEvent) {
        //这种类型的传感器返回步骤的数量由用户自上次重新启动时激活。返回的值是作为浮动(小数部分设置为0),
        // 只在系统重启复位为0。事件的时间戳将该事件的第一步的时候。这个传感器是在硬件中实现,预计低功率。
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            //获取当前传感器返回的临时步数
            val tempStep = event.values[0].toInt()
            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if (!hasRecord) {
                hasRecord = true
                hasStepCount = tempStep
            } else {
                //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                val thisStepCount = tempStep - hasStepCount
                //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                val thisStep = thisStepCount - previousStepCount
                //总步数=现有的步数+本次有效步数
                nowBuSu += thisStep
                //记录最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount
            }
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                nowBuSu++
            }
        }
        updateNotification()
    }

    /**
     * 计步传感器精度变化回调接口
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //no op
    }

    /**
     * 数据传递接口
     *
     * @param paramICallback
     */
    fun registerCallback(paramICallback: StepUpdateUiCallBack?) {
        mCallback = paramICallback
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //返回START_STICKY ：在运行onStartCommand后service进程被kill后，那将保留在开始状态，但是不保留那些传入的intent。
        // 不久后service就会再次尝试重新创建，因为保留在开始状态，在创建     service后将保证调用onstartCommand。
        // 如果没有传递任何开始命令给service，那将获取到null的intent。
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(this)
        mStepCount?.stepDetector?.let { sensorManager.unregisterListener(it) }

        //取消前台进程
        stopForeground(true)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    /**
     * 绑定回调接口
     */
    inner class LcBinder : Binder() {
        val service: StepService
            get() = this@StepService
    }
}