package com.angcyo.library.component

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.angcyo.library.annotation.CallPoint
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * 摇一摇
 * [com.facebook.react.common.ShakeDetector]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/22
 */
class ShakeDetector : SensorEventListener {

    // Collect sensor data in this interval (nanoseconds)
    private val MIN_TIME_BETWEEN_SAMPLES_NS =
        TimeUnit.NANOSECONDS.convert(20, TimeUnit.MILLISECONDS)

    // Number of nanoseconds to listen for and count shakes (nanoseconds)
    private val SHAKING_WINDOW_NS = TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS).toFloat()

    // Required force to constitute a rage shake. Need to multiply gravity by 1.33 because a rage
    // shake in one direction should have more force than just the magnitude of free fall.
    private val REQUIRED_FORCE = SensorManager.GRAVITY_EARTH * 1.33f

    private var mAccelerationX = 0f
    private var mAccelerationY: Float = 0f
    private var mAccelerationZ: Float = 0f

    private var mShakeListener: ShakeListener? = null

    private var mSensorManager: SensorManager? = null
    private var mLastTimestamp: Long = 0
    private var mNumShakes = 0
    private var mLastShakeTimestamp: Long = 0

    // number of shakes required to trigger onShake()
    private var mMinNumShakes = 0

    constructor(listener: ShakeListener?) : this(listener, 1)

    constructor(listener: ShakeListener?, minNumShakes: Int) {
        mShakeListener = listener
        mMinNumShakes = minNumShakes
    }

    /** Start listening for shakes.  */
    @CallPoint
    fun start(manager: SensorManager) {
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            mSensorManager = manager
            mLastTimestamp = -1
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            mLastShakeTimestamp = 0
            reset()
        }
    }

    /** Stop listening for shakes.  */
    @CallPoint
    fun stop() {
        mSensorManager?.unregisterListener(this)
        mSensorManager = null
    }

    /** Reset all variables used to keep track of number of shakes recorded.  */
    private fun reset() {
        mNumShakes = 0
        mAccelerationX = 0f
        mAccelerationY = 0f
        mAccelerationZ = 0f
    }

    /**
     * Determine if acceleration applied to sensor is large enough to count as a rage shake.
     *
     * @param a acceleration in x, y, or z applied to the sensor
     * @return true if the magnitude of the force exceeds the minimum required amount of force. false
     * otherwise.
     */
    private fun atLeastRequiredForce(a: Float): Boolean {
        return abs(a) > REQUIRED_FORCE
    }

    /**
     * Save data about last shake
     *
     * @param timestamp (ns) of last sensor event
     */
    private fun recordShake(timestamp: Long) {
        mLastShakeTimestamp = timestamp
        mNumShakes++
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.timestamp - mLastTimestamp < MIN_TIME_BETWEEN_SAMPLES_NS) {
            return
        }
        val ax = sensorEvent.values[0]
        val ay = sensorEvent.values[1]
        val az = sensorEvent.values[2] - SensorManager.GRAVITY_EARTH
        mLastTimestamp = sensorEvent.timestamp
        if (atLeastRequiredForce(ax) && ax * mAccelerationX <= 0) {
            recordShake(sensorEvent.timestamp)
            mAccelerationX = ax
        } else if (atLeastRequiredForce(ay) && ay * mAccelerationY <= 0) {
            recordShake(sensorEvent.timestamp)
            mAccelerationY = ay
        } else if (atLeastRequiredForce(az) && az * mAccelerationZ <= 0) {
            recordShake(sensorEvent.timestamp)
            mAccelerationZ = az
        }
        maybeDispatchShake(sensorEvent.timestamp)
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {}

    private fun maybeDispatchShake(currentTimestamp: Long) {
        if (mNumShakes >= 8 * mMinNumShakes) {
            reset()
            mShakeListener?.onShake()
        }
        if (currentTimestamp - mLastShakeTimestamp > SHAKING_WINDOW_NS) {
            reset()
        }
    }

    /**回调*/
    interface ShakeListener {
        fun onShake()
    }
}