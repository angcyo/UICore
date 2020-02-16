package com.angcyo.library.component.work

import android.content.Context
import com.angcyo.library.L.d
import java.util.*

/**
 * A base for tracking constraints and notifying listeners of changes.
 *
 * @param <T> the constraint data type observed by this tracker
</T> */
abstract class ConstraintTracker<T>(context: Context, val taskExecutor: TaskExecutor) {
    val appContext = context.applicationContext

    private val lock = Any()
    private val listeners: MutableSet<ConstraintListener<T>> = LinkedHashSet()

    // Synthetic access
    var currentState: T? = null

    /**
     * Add the given listener for tracking.
     * This may cause [.getInitialState] and [.startTracking] to be invoked.
     * If a state is set, this will immediately notify the given listener.
     *
     * @param listener The target listener to start notifying
     */
    fun addListener(listener: ConstraintListener<T>) {
        synchronized(lock) {
            if (listeners.add(listener)) {
                if (listeners.size == 1) {
                    currentState = initialState
                    d(
                        String.format(
                            "%s: initial state = %s",
                            javaClass.simpleName,
                            currentState
                        )
                    )
                    startTracking()
                }
                listener.onConstraintChanged(currentState)
            }
        }
    }

    /**
     * Remove the given listener from tracking.
     *
     * @param listener The listener to stop notifying.
     */
    fun removeListener(listener: ConstraintListener<T>?) {
        synchronized(lock) {
            if (listeners.remove(listener) && listeners.isEmpty()) {
                stopTracking()
            }
        }
    }

    /**
     * Sets the state of the constraint.
     * If state is has not changed, nothing happens.
     *
     * @param newState new state of constraint
     */
    fun setState(newState: T) {
        synchronized(lock) {
            if ((currentState == newState ||
                        (currentState != null && (currentState == newState)))
            ) {
                return
            }
            currentState = newState

            // onConstraintChanged may lead to calls to addListener or removeListener.
            // This can potentially result in a modification to the set while it is being
            // iterated over, so we handle this by creating a copy and using that for
            // iteration.

            val listenersList: List<ConstraintListener<T>> =
                ArrayList(listeners)

            taskExecutor.mainThreadExecutor.execute {
                for (listener: ConstraintListener<T> in listenersList) {
                    listener.onConstraintChanged(currentState)
                }
            }
        }
    }

    /**
     * Determines the initial state of the constraint being tracked.
     */
    abstract val initialState: T

    /**
     * Start tracking for constraint state changes.
     */
    abstract fun startTracking()

    /**
     * Stop tracking for constraint state changes.
     */
    abstract fun stopTracking()
}