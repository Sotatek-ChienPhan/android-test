package com.android.test.ui

import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.android.test.R
import com.android.test.base.BaseFragment
import com.android.test.utils.setOnDebouncedClickListener
import com.android.test.utils.setStyledTitle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.toolbar_normal.*

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginViewModel>() {
    override fun layoutId(): Int  = R.layout.fragment_login

    override fun getViewModelClass(): Class<LoginViewModel>  = LoginViewModel::class.java

    override fun setupViews() {
        toolbar.setStyledTitle("Login")
        toolbar.navigationIcon = null
        viewModel.loadingLiveData.observe(viewLifecycleOwner, Observer {
            showProgress(it)
        })
        viewModel.authRuleLiveData.observe(viewLifecycleOwner, {
            tvErrorUserName.text = if (it.userName.isNullOrEmpty()) getString(R.string.error_empty_username) else ""
            tvErrorPassword.text = if (it.password.isNullOrEmpty()) getString(R.string.error_empty_username) else ""
            btnRegister.setOnDebouncedClickListener { _->
                if (!it.userName.isNullOrEmpty() && !it.password.isNullOrEmpty()) {
                    viewModel.login(it.getInput())
                }
            }
        })
        edtUserName.doAfterTextChanged {
            viewModel.userNameLiveData.postValue(it.toString().trim())
        }
        edtPassword.doAfterTextChanged {
            viewModel.passwordLiveData.postValue(it.toString().trim())
        }
    }
}