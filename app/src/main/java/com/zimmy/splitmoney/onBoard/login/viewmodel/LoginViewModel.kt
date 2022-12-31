package com.zimmy.splitmoney.onBoard.login.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zimmy.splitmoney.repositories.UserRepository
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    mAuth: FirebaseAuth
) : ViewModel() {

    private val TAG = LoginViewModel::class.java.simpleName

    private val userExistMutableLiveData: MutableLiveData<ResultData<Boolean?>> = MutableLiveData()
    val userExistLiveData: LiveData<ResultData<Boolean?>>
        get() = userExistMutableLiveData


    private val googleMutableLiveData: MutableLiveData<ResultData<Boolean>> = MutableLiveData()
    val googleLiveData: LiveData<ResultData<Boolean>>
        get() = googleMutableLiveData

    private val insertMutableLiveData: MutableLiveData<ResultData<Boolean>> = MutableLiveData()
    val insertLiveData: LiveData<ResultData<Boolean>>
        get() = insertMutableLiveData

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

    suspend fun authenticateUserWithGoogle(
        idToken: String,
        mAuth: FirebaseAuth,
        context: Activity,
        editor: SharedPreferences.Editor
    ) {
        userRepository.authenticateWithGoogle(idToken, mAuth, context, editor).onStart {
            emit(ResultData.Loading())
        }.collect {
            when (it) {
                ResultData.Success(true) -> {
                    //case of 1st time
                    googleMutableLiveData.postValue(ResultData.Success(true))
                }

                ResultData.Failed() -> {
                    //show some error
                    googleMutableLiveData.postValue(ResultData.Success(false))
                }

                ResultData.Loading() -> {
                    //case to load
                    googleMutableLiveData.postValue(ResultData.Loading())
                }

                else -> {
                    googleMutableLiveData.postValue(ResultData.Failed())
                }
            }
        }
    }

    suspend fun insertDatabaseOperation(
        mAuth: FirebaseAuth,
        phoneNumber: String,
        sharedPreferences: SharedPreferences
    ) {
        userRepository.insertDatabaseOperation(mAuth, phoneNumber, sharedPreferences).onStart {
            emit(ResultData.Loading())
        }.collect {
            when (it) {
                ResultData.Success(true) -> {
                    insertMutableLiveData.postValue(ResultData.Success())
                }
                else -> {
                    Log.d(TAG,"Failure")
                    insertMutableLiveData.postValue(ResultData.Failed())
                }
            }
        }
    }

}