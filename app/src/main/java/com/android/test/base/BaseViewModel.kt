package com.android.test.base


import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.android.test.ui.BaseNavigationListener
import com.android.test.R
import com.android.test.network.DomainError

open class BaseViewModel : ViewModel() {
    val progressLoadingLiveData = SingleLiveEvent<Boolean>()
    val showErrorLiveData = SingleLiveEvent<String>()
    val showMessageLiveData = SingleLiveEvent<String>()
    val showMessageSuccessLiveData = SingleLiveEvent<String>()
    val tokenExpiredLiveData = SingleLiveEvent<String>()
    var baseNavigationListener: BaseNavigationListener? = null

    fun onShowLoading() {
        progressLoadingLiveData.value = true
    }

    fun onHideLoading() {
        progressLoadingLiveData.value = false
    }

    /**
     * uses placeholder "Something went wrong" in case of empty error message
     */
    fun onDomainError(domainError: DomainError) {
        if (domainError is DomainError.NetworkException) {
            showErrorLiveData.postValue(convertResToString(domainError.errorResource))
        } else if (domainError.errorMessage.isEmpty()) {
            showErrorLiveData.postValue(convertResToString(R.string.error_something_went_wrong))
        } else {
            showErrorLiveData.postValue(domainError.errorMessage)
        }
    }

    fun convertResToString(@StringRes strRes: Int): String? {
        return BaseApplication.getInstance()?.getString(strRes)
    }
}