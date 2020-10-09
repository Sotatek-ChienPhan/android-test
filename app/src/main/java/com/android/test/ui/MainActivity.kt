package com.android.test.ui

import com.android.test.R
import com.android.test.base.BaseActivity
import com.android.test.base.BaseFragment
import com.android.test.utils.currentFragment
import com.android.test.utils.replaceFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(), BaseNavigationListener {
    override fun layoutId(): Int  = R.layout.activity_main

    override fun setupViews() {
        replaceFragment(LoginFragment())
    }

    override fun popFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun onBackPressed() {
        val currentFragment = currentFragment()
        if (currentFragment is BaseFragment<*> && currentFragment.enableBackPressed()) {
            val stackCount = supportFragmentManager.backStackEntryCount
            if (stackCount == 0) {
                finish()
            } else {
                super.onBackPressed()
            }
        }
    }

}

interface BaseNavigationListener {
    fun popFragment()

}