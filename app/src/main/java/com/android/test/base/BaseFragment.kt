package com.android.test.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.test.ui.BaseNavigationListener
import com.android.test.R
import com.android.test.utils.ProgressDialogFragment
import com.android.test.utils.consumeAllClicks
import com.android.test.utils.hideKeyboard
import com.android.test.utils.setNavigationIconClick

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {
    abstract fun layoutId(): Int
    protected abstract fun getViewModelClass(): Class<VM>
    abstract fun setupViews()
    protected lateinit var viewModel: VM
    protected lateinit var baseNavigationListener: BaseNavigationListener
    open fun onCreateViewModel() {
        viewModel = ViewModelProvider(this).get(getViewModelClass())
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseNavigationListener = context as BaseNavigationListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateViewModel()
        view.consumeAllClicks()
        hideKeyboard()
        viewModel.baseNavigationListener = baseNavigationListener
        viewModel.showErrorLiveData.observe(viewLifecycleOwner, Observer {
            showErrorSnackbar(it)
        })
        viewModel.showMessageLiveData.observe(viewLifecycleOwner, Observer {
            showSnackbar(it)
        })
        viewModel.showMessageSuccessLiveData.observe(viewLifecycleOwner, Observer {
            showSuccessSnackbar(it)
        })
        if (enableNavigationIcon()) view.findViewById<Toolbar>(R.id.toolbar)?.setNavigationIconClick({ view -> baseNavigationListener.popFragment() })
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                baseNavigationListener.popFragment()
            }
        })
        setupViews()
    }

    protected open fun showProgress(
        visible: Boolean,
        @OptIn fullScreen: Boolean = false,
        @OptIn @StringRes messageResId: Int = 0
    ) {
        val fragment = childFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_TAG)
        if (fragment != null && !visible) {
            (fragment as ProgressDialogFragment).dismissAllowingStateLoss()
        } else if (fragment == null && visible) {
            val backgroundColorResId =
                if (fullScreen) android.R.color.white else android.R.color.transparent
            ProgressDialogFragment.newInstance(backgroundColorResId, messageResId)
                .show(childFragmentManager, ProgressDialogFragment.PROGRESS_TAG)
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }



    open fun visibleChange(visible: Boolean) {

    }

    open fun enableBackPressed() = true

    open fun enableNavigationIcon() = true
}