package com.android.test.base

import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.android.test.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


fun Fragment.showSuccessSnackbar(
    text: CharSequence
) = showSnackbar(text, android.R.color.holo_green_dark)

@JvmOverloads
fun Fragment.showErrorSnackbar(
    text: CharSequence,
    offsetY: Int = 0
) = showSnackbar(text, android.R.color.holo_red_dark)

const val ERROR_DISPLAY_DURATION = 4000

fun Fragment.showWarningSnackbar(
    text: CharSequence
) = showSnackbar(text)

@JvmOverloads
fun Fragment.showSnackbar(
    text: CharSequence,
    duration: Int = 1000,
    @ColorRes bgColorRes: Int? = null,
    offsetY: Int = 0
): Snackbar? = view?.makeSnackbar(text, duration)?.apply {
    if (bgColorRes != null) view.setBackgroundColor(ContextCompat.getColor(context, bgColorRes))
    if (offsetY > 0) setBottomOffset(offsetY)
    show()
}

private fun View.makeSnackbar(
    text: CharSequence,
    duration: Int = BaseTransientBottomBar.LENGTH_SHORT
): Snackbar {
    return Snackbar.make(this, text, Snackbar.LENGTH_SHORT).also {
        findViewById<TextView>(R.id.snackbar_text)?.let { snackTextView ->
            val resources = snackTextView.context.resources
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                snackTextView,
                resources.getDimension(R.dimen.text_smallest).toInt(),
                resources.getDimension(R.dimen.text_large).toInt(),
                1,
                TypedValue.COMPLEX_UNIT_PX
            )
        }
    }
}

private fun Snackbar.setBottomOffset(offsetY: Int) {
    view.layoutParams.let { params ->
        when (params) {
            is LinearLayout.LayoutParams -> params.bottomMargin += offsetY
            is FrameLayout.LayoutParams -> params.bottomMargin += offsetY
            is RelativeLayout.LayoutParams -> params.bottomMargin += offsetY
            is ConstraintLayout.LayoutParams -> params.bottomMargin += offsetY
        }
    }
}

