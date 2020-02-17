package com.angcyo.library.component.work

import android.content.Context
import com.angcyo.library.app

/**
 * A singleton class to hold an instance of each [ConstraintTracker].
 */
class Trackers(context: Context, val taskExecutor: TaskExecutor) {
//    private val mBatteryChargingTracker: BatteryChargingTracker
//    private val mBatteryNotLowTracker: BatteryNotLowTracker
    /**
     * Gets the tracker used to track network state changes.
     *
     * @return The tracker used to track state of the network
     */
    val networkStateTracker: NetworkStateTracker
//    private val mStorageNotLowTracker: StorageNotLowTracker
//    /**
//     * Gets the tracker used to track the battery charging status.
//     *
//     * @return The tracker used to track battery charging status
//     */
//    val batteryChargingTracker: BatteryChargingTracker
//        get() = mBatteryChargingTracker
//
//    /**
//     * Gets the tracker used to track if the battery is okay or low.
//     *
//     * @return The tracker used to track if the battery is okay or low
//     */
//    val batteryNotLowTracker: BatteryNotLowTracker
//        get() = mBatteryNotLowTracker
//
//    /**
//     * Gets the tracker used to track if device storage is okay or low.
//     *
//     * @return The tracker used to track if device storage is okay or low.
//     */
//    val storageNotLowTracker: StorageNotLowTracker
//        get() = mStorageNotLowTracker

    init {
        val appContext = context.applicationContext
//        mBatteryChargingTracker = BatteryChargingTracker(appContext, taskExecutor)
//        mBatteryNotLowTracker = BatteryNotLowTracker(appContext, taskExecutor)
        networkStateTracker = NetworkStateTracker(appContext, taskExecutor)
//        mStorageNotLowTracker = StorageNotLowTracker(appContext, taskExecutor)
    }

    companion object {
        private var sInstance: Trackers? = null
        /**
         * Gets the singleton instance of [Trackers].
         *
         * @param context The initializing context (we only use the application context)
         * @return The singleton instance of [Trackers].
         */
        @Synchronized
        fun getInstance(
            context: Context = app(),
            taskExecutor: TaskExecutor = WorkManagerTaskExecutor()
        ): Trackers {
            if (sInstance == null) {
                sInstance = Trackers(context, taskExecutor)
            }
            return sInstance!!
        }
    }
}