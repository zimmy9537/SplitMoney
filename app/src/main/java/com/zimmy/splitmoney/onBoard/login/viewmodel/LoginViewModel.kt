package com.zimmy.splitmoney.onBoard.login.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zimmy.splitmoney.repositories.UserRepository
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val TAG = LoginViewModel::class.java.simpleName

    private val userExistMutableLiveData: MutableLiveData<ResultData<Boolean?>> = MutableLiveData()
    val userExistLiveData: LiveData<ResultData<Boolean?>>
        get() = userExistMutableLiveData

    suspend fun getUserStatus(phoneNumber: String) {
        userRepository.findUser(phoneNumber = phoneNumber).onStart {
            emit(null)
        }.collect {
            when (it) {
                true -> {
                    Log.d(TAG, "Number found")
                    userExistMutableLiveData.postValue(ResultData.Success(true))
                }
                false -> {
                    Log.d(TAG, "No Number found")
                    userExistMutableLiveData.postValue(ResultData.Success(false))
                }
                else -> {
                    Log.d(TAG, "Loading")
                    userExistMutableLiveData.postValue(ResultData.Loading())
                }
            }
        }
    }
}