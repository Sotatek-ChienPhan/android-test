package com.android.test.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.android.test.R

import com.google.android.material.snackbar.Snackbar
import java.util.*

inline fun View.setOnDebouncedClickListener(
    minimumClicksIntervalMillis: Long = 800L,
    crossinline action: (view: View) -> Unit
) {
    setOnClickListener(object : DebounceClickListener(minimumClicksIntervalMillis) {
        override fun onDebouncedClick(view: View) {
            action.invoke(view)
        }
    })
}

fun Context.dpToPx(dp: Int) = dp * resources.displayMetrics.density

fun Context.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics)
        .toInt()
}

fun Context.screenWidth(): Int = this.resources.displayMetrics.widthPixels

fun Context.screenHeight(): Int = this.resources.displayMetrics.heightPixels

fun Context.scaledDensity(): Float = this.resources.displayMetrics.scaledDensity

fun Context.screenWidthInDp(): Float =
    this.resources.displayMetrics.let { it.widthPixels / it.density }

fun Context.screenHeightInDp(): Float =
    this.resources.displayMetrics.let { it.heightPixels / it.density }

fun Snackbar.showAboveNavBar() {
    val params = view.layoutParams as CoordinatorLayout.LayoutParams

    params.setMargins(
        params.leftMargin,
        params.topMargin,
        params.rightMargin,
        params.bottomMargin + context.navBarHeight()
    )

    view.layoutParams = params
    show()
}

fun Context.navBarHeight(): Int {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

@SuppressLint("ClickableViewAccessibility")
fun androidx.viewpager.widget.ViewPager.disableSwipe() {
    this.setOnTouchListener { _: View, _ -> true }
}

@SuppressLint("ClickableViewAccessibility")
fun androidx.viewpager.widget.ViewPager.enableSwipe() {
    this.setOnTouchListener { _, _ -> false }
}

@SuppressLint("ClickableViewAccessibility")
fun View.consumeAllClicks() {
    this.setOnTouchListener { _, _ -> true }
}

@SuppressLint("ClickableViewAccessibility")
fun TextView.setCompoundRightDrawableClickListener(onClick: ((TextView) -> Unit)) {
    setOnTouchListener { view, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            val drawableRight = (view as TextView).compoundDrawables[2]
            if (drawableRight != null && motionEvent.rawX >= view.right - drawableRight.bounds.width()) {
                onClick.invoke(view)
                return@setOnTouchListener true
            }
        }
        false
    }
}

fun Fragment.openBrowser(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Context.hasPermissions(vararg permissions: String): Boolean {
    for (permission in permissions) {
        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

inline fun Fragment.fragmentTransaction(block: FragmentTransaction.() -> Unit) {
    childFragmentManager.beginTransaction().apply(block).commit()
}

inline fun FragmentActivity.fragmentTransaction(block: FragmentTransaction.() -> Unit) {
    supportFragmentManager.beginTransaction().apply(block).apply {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) commit() else commitAllowingStateLoss()
    }
}

inline fun FragmentManager.fragmentTransaction(block: FragmentTransaction.() -> Unit) {
    beginTransaction().apply(block).commit()
}

fun FragmentActivity.replaceFragment(
    fragment: Fragment,
    @OptIn addToBackStack: Boolean = false,
    animation: Boolean = false
) {
    val tag = fragment.javaClass.canonicalName
    fragmentTransaction {
        replace(R.id.fragment_container, fragment, tag)
        if (addToBackStack) addToBackStack(tag)
    }
}

fun FragmentActivity.overlayFragment(
    fragment: Fragment,
    addToBackStack: Boolean = true,
    animation: Boolean = true
) {
    val tag = fragment.javaClass.canonicalName
    fragmentTransaction {
        add(R.id.fragment_container, fragment, tag)
        if (addToBackStack) addToBackStack(tag)
    }
}

fun FragmentActivity.clearALlFragment(
) {
    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun FragmentActivity.popToFragments(fragment: Fragment) {
    var currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
    while (fragment.javaClass.canonicalName != currentFragment?.javaClass?.canonicalName) {
        supportFragmentManager.popBackStackImmediate()
        currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
    }
}

fun FragmentActivity.containFragment(fragment: Fragment) : Boolean {
    var currentFragment = supportFragmentManager.findFragmentByTag(fragment.javaClass.canonicalName)
    return currentFragment != null
}


fun FragmentActivity.currentFragment() : Fragment?{
    return supportFragmentManager.findFragmentById(R.id.fragment_container)
}

fun FragmentActivity.backStackContain(fragment: Fragment): Boolean {
    for (i in 1 until supportFragmentManager.backStackEntryCount) {
        if (supportFragmentManager.getBackStackEntryAt(i).name == fragment.javaClass.canonicalName) {
            return true
        }
    }
    return false
}

fun setLocale(context: Context, locale: Locale): Context? {
    var context = context
    Locale.setDefault(locale)
    val res: Resources = context.resources
    val config = Configuration(res.configuration)
    config.setLocale(locale)
    context = context.createConfigurationContext(config)
    return context
}

val Context.keyboardHeight: Int
    get() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val method = imm.javaClass.getMethod("getInputMethodWindowVisibleHeight")
        method.isAccessible = true
        return method.invoke(imm) as Int
    }

fun TextView.showKeyboard() {
    requestFocus()
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun TextView.showKeyboardWithDelay(delayMillis: Long = 200L) {
    postDelayed({ showKeyboard() }, delayMillis)
}

fun TextView.hideKeyboard() {
    clearFocus()
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.hideKeyboard() {
    val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let {
        manager.hideSoftInputFromWindow(it.windowToken, 0)
        it.clearFocus()
    }
}

fun Fragment.hideKeyboard() = activity?.hideKeyboard()
fun Fragment.getDatePicker(
    onDateChange: (Date) -> Unit,
    maxDate: Date? = Date()
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val picker = DatePickerDialog(
        requireContext(),
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            run {
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                onDateChange.invoke(calendar.time)
            }
        }, calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    maxDate?.let {
        picker.datePicker.maxDate = it.time
    }
    return picker
}

//Use to animate Seekbar progress
fun SeekBar.animateProgress(newProgress: Int) {
    val anim = ValueAnimator.ofInt(progress, newProgress)
    anim.apply {
        duration = 500
        addUpdateListener { animation ->
            val animProgress = animation!!.animatedValue.toString().toInt()
            progress = animProgress
        }
    }.start()
}

fun Toolbar.setStyleTitle(@StringRes stringRes: Int) {
    setStyledTitle(context.getString(stringRes).capitalize())
}

fun Toolbar.setNavigationIconClick(@OptIn onClickNavigation: ((View) -> Unit)? = null) {
    if (onClickNavigation != null) {
        navigationIcon = context.getDrawable(R.drawable.ic_baseline_chevron_left_24)
        setNavigationOnClickListener(onClickNavigation)
    } else {
        navigationIcon = null
    }
}

fun Toolbar.setStyledTitle(title: String = "") {
    this.title = ""
    navigationIcon?.mutate()
    findViewById<TextView>(R.id.tvToolbarTitle)?.text = title
}

fun View.addActionBarRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.actionBarItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun NestedScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun Activity.setFullScreen(fullScreen: Boolean) {
    if (fullScreen) window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
}

fun Activity.setTranslucentStatusBar1(translucent: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val w = this.window
        if (translucent) w.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        else w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

fun Activity.setTranslucentStatusBar(translucent: Boolean) {
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

    if (translucent) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
            getWindow().setStatusBarColor(Color.TRANSPARENT)
        } else if (Build.VERSION.SDK_INT >= 23) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            getWindow().setStatusBarColor(Color.TRANSPARENT)
        }
    } else {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

fun Activity.setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
    val win: Window = activity.window
    val winParams: WindowManager.LayoutParams = win.getAttributes()
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.setAttributes(winParams)
}

fun setSystemBarTheme(pActivity: Activity, pIsDark: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Fetch the current flags.
        val lFlags = pActivity.window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        pActivity.window.decorView.systemUiVisibility =
            if (pIsDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

