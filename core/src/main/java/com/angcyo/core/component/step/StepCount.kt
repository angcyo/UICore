package com.angcyo.core.component.step

import com.angcyo.library.L

/**
 * Created by Administrator on 2017/6/22 0022.
 */
class StepCount : StepCountListener {

    private var mCount = 0L//当前步数

    private var count = 0L //缓存步数，步数3秒内小于10步则不计数

    private var timeOfLastPeak: Long = 0 //计时  开始时间 步数3秒内小于10步则不计数
    private var timeOfThisPeak: Long = 0 //计时  现在时间 步数3秒内小于10步则不计数
    private var stepValuePassListener: StepValuePassListener? = null//接口用来传递步数变化

    /**
     * 用来给调用者获取SensorEventListener实例
     * @return 返回SensorEventListener实例
     */
    val stepDetector: StepDetector = StepDetector() //传感器SensorEventListener子类实例

    init {
        stepDetector.initListener(this)
    }

    override fun countStep() {
        timeOfLastPeak = timeOfThisPeak
        timeOfThisPeak = System.currentTimeMillis()
        L.i("countStep", "传感器数据刷新回调")
        //notifyListener();
        if (timeOfThisPeak - timeOfLastPeak <= 3000L) {
            if (count < 9) {
                count++
            } else if (count == 9L) {
                count++
                mCount += count
                notifyListener()
            } else {
                mCount++
                notifyListener()
            }
        } else { //超时
            count = 1 //为1,不是0
        }
    }

    fun setSteps(initNowBusu: Long) {
        mCount = initNowBusu //接收上层调用传递过来的当前步数
        count = 0
        timeOfLastPeak = 0
        timeOfThisPeak = 0
        notifyListener()
    }

    /**
     * 更新步数，通过接口函数通过上层调用者
     */
    fun notifyListener() {
        L.i("countStep", "数据更新")
        stepValuePassListener?.stepChanged(mCount) //当前步数通过接口传递给调用者
    }

    fun initListener(listener: StepValuePassListener?) {
        stepValuePassListener = listener
    }
}