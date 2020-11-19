package com.angcyo.core.component.step

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

/**
 * Created by dylan on 16/9/27.
 * 算法的主要部分,检测是否是步点
 */

class StepDetector : SensorEventListener {
    //存放三轴数据
    var oriValues = FloatArray(3)
    val ValueNum = 4

    //用于存放计算阈值的波峰波谷差值
    var tempValue = FloatArray(ValueNum)
    var tempCount = 0

    //是否上升的标志位
    var isDirectionUp = false

    //持续上升次数
    var continueUpCount = 0

    //上一点的持续上升的次数，为了记录波峰的上升次数
    var continueUpFormerCount = 0

    //上一点的状态，上升还是下降
    var lastStatus = false

    //波峰值
    var peakOfWave = 0f

    //波谷值
    var valleyOfWave = 0f

    //此次波峰的时间
    var timeOfThisPeak: Long = 0

    //上次波峰的时间
    var timeOfLastPeak: Long = 0

    //当前的时间
    var timeOfNow: Long = 0

    //当前传感器的值
    var gravityNew = 0f

    //上次传感器的值
    var gravityOld = 0f

    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    val InitialValue = 1.3.toFloat()

    //初始阈值
    var ThreadValue = 2.0.toFloat()

    //波峰波谷时间差
    var TimeInterval = 250

    private var mStepListeners: StepCountListener? = null

    override fun onSensorChanged(event: SensorEvent) { //当传感器值改变回调此方法
        for (i in 0..2) {
            oriValues[i] = event.values[i]
        }
        gravityNew = sqrt(
            oriValues[0] * oriValues[0] +
                    oriValues[1] * oriValues[1] +
                    (oriValues[2] * oriValues[2]).toDouble()
        ).toFloat()
        detectorNewStep(gravityNew)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //
    }

    fun initListener(listener: StepCountListener?) {
        mStepListeners = listener
    }

    /*
    * 检测步子，并开始计步
    * 1.传入sersor中的数据
    * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
    * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
    * */
    fun detectorNewStep(values: Float) {
        if (gravityOld == 0f) {
            gravityOld = values
        } else {
            if (detectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak
                timeOfNow = System.currentTimeMillis()
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && peakOfWave - valleyOfWave >= ThreadValue
                ) {
                    timeOfThisPeak = timeOfNow
                    /*
                     * 更新界面的处理，不涉及到算法
                     * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
                     * 1.连续记录10才开始计步
                     * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
                     * 3.连续记录了9步用户还在运动，之前的数据才有效
                     * */mStepListeners!!.countStep()
                }
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                    && peakOfWave - valleyOfWave >= InitialValue
                ) {
                    timeOfThisPeak = timeOfNow
                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave)
                }
            }
        }
        gravityOld = values
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于20
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    fun detectorPeak(newValue: Float, oldValue: Float): Boolean {
        lastStatus = isDirectionUp
        if (newValue >= oldValue) {
            isDirectionUp = true
            continueUpCount++
        } else {
            continueUpFormerCount = continueUpCount
            continueUpCount = 0
            isDirectionUp = false
        }
        return if (!isDirectionUp && lastStatus
            && (continueUpFormerCount >= 2 || oldValue >= 20)
        ) {
            peakOfWave = oldValue
            true
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue
            false
        } else {
            false
        }
    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    fun peakValleyThread(value: Float): Float {
        var tempThread = ThreadValue
        if (tempCount < ValueNum) {
            tempValue[tempCount] = value
            tempCount++
        } else {
            tempThread = averageValue(tempValue, ValueNum)
            for (i in 1 until ValueNum) {
                tempValue[i - 1] = tempValue[i]
            }
            tempValue[ValueNum - 1] = value
        }
        return tempThread
    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    fun averageValue(value: FloatArray, n: Int): Float {
        var ave = 0f
        for (i in 0 until n) {
            ave += value[i]
        }
        ave /= ValueNum
        ave = if (ave >= 8) 4.3f
        else if (ave >= 7 && ave < 8) 3.3f
        else if (ave >= 4 && ave < 7) 2.3f
        else if (ave >= 3 && ave < 4) 2.0f
        else 1.3f

        return ave
    }
}