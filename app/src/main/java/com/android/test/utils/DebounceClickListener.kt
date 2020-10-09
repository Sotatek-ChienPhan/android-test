package com.android.test.utils

import android.util.SparseLongArray
import android.view.View
import kotlin.math.abs

abstract class DebounceClickListener(private val minimumClicksIntervalMillis: Long = 800) : View.OnClickListener {

    private val clickedViewsIdsMap: SparseLongArray by lazy { SparseLongArray() }

    abstract fun onDebouncedClick(view: View)

    override fun onClick(clickedView: View?) {
        val keyNotFoundResultValue: Long = -1
        // Handling multiple clicks for the same View
        clickedView?.let {
            val previousClickTimestampForViewId = clickedViewsIdsMap.get(it.id, keyNotFoundResultValue)
            val currentTimestamp: Long = System.currentTimeMillis()

            val isFirstTimeToClickView = previousClickTimestampForViewId == keyNotFoundResultValue
            val clicksDifferenceBiggerThanInterval =
                abs(currentTimestamp - previousClickTimestampForViewId) > minimumClicksIntervalMillis
            // If the View is clicked for the first time, or it has been clicked for longer than minimumClickIntervalMillis
            clickedViewsIdsMap.put(it.id, currentTimestamp)
            if (isFirstTimeToClickView || clicksDifferenceBiggerThanInterval) onDebouncedClick(clickedView)
        }
    }
}