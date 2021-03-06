package com.android.test.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.test.base.BaseViewModel
import com.android.test.base.SingleLiveEvent
import com.android.test.network.Resource
import com.android.test.utils.Constant
import com.android.test.utils.SharedPreferenceManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import setel.com.android.repository.LoginRepository

class LoginViewModel @ViewModelInject constructor(private val loginRepository: LoginRepository) : BaseViewModel() {
    val userNameLiveData = MutableLiveData<String>()
    val passwordLiveData = MutableLiveData<String>()
    val loadingLiveData = SingleLiveEvent<Boolean>()
    val authRuleLiveData = MediatorLiveData<LoginRule>().apply {
        value = LoginRule()
        addSource(userNameLiveData) {
            it ?: return@addSource
            value?.copy(userName = it).takeIf { new -> new != value }?.let { new -> value = new }
        }
        addSource(passwordLiveData) {
            it ?: return@addSource
            value?.copy(password = it).takeIf { new -> new != value }?.let { new -> value = new }
        }
    }

    fun login(request: JsonObject){
        viewModelScope.launch(Dispatchers.Main) {
            loginRepository.login(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        loadingLiveData.postValue(false)
                        val response = resource.data.body()
                        if (response?.errorCode == "00") {
                            showMessageSuccessLiveData.postValue(response.errorMessage)
                            loginRepository.saveUser(response.user)
                            val xAcc = resource.data.headers()[Constant.X_ACC]
                            SharedPreferenceManager.put(Constant.X_ACC, xAcc)
                        } else {
                            showMessageSuccessLiveData.postValue("Your password is incorrect.")
                        }
                    }
                    is Resource.Failure -> {
                        loadingLiveData.postValue(false)
                        onDomainError(resource.error)
                    }
                    is Resource.Loading -> {
                        loadingLiveData.postValue(true)
                    }
                }
            }
        }
    }
}