package com.zimmy.splitmoney.settleup.balance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zimmy.splitmoney.models.TransactionResult
import com.zimmy.splitmoney.repositories.BalanceRepository
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BalanceViewModel @Inject constructor(private val balanceRepo: BalanceRepository) :
    ViewModel() {

    private val transactionResultMutableLiveData: MutableLiveData<ResultData<TransactionResult>> =
        MutableLiveData()
    val transactionResultLiveData: LiveData<ResultData<TransactionResult>>
        get() = transactionResultMutableLiveData

    private val memberListMutableLiveData: MutableLiveData<ResultData<HashMap<String, String>>> =
        MutableLiveData()
    val memberLiveData: LiveData<ResultData<HashMap<String, String>>>
        get() = memberListMutableLiveData

    fun getTransactionResult(
        isFriend: Boolean,
        groupCode: String,
        myPhone: String,
        phoneMap: HashMap<String, String>
    ) {
        viewModelScope.launch {
            balanceRepo.getTransactionResultList2(isFriend, groupCode, myPhone,phoneMap).onStart {
                emit(ResultData.Loading())
            }.collect {
                transactionResultMutableLiveData.value = it
            }
        }
    }


    fun getMemberList(groupCode: String) {
        viewModelScope.launch {
            balanceRepo.getMemberList(groupCode).onStart {
                emit(ResultData.Loading())
            }.collect {
                memberListMutableLiveData.value = it
            }
        }
    }

}