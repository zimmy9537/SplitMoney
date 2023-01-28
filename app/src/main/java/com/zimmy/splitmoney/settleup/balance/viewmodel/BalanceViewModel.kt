package com.zimmy.splitmoney.settleup.balance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.repositories.BalanceRepository
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class BalanceViewModel @Inject constructor(private val balanceRepo: BalanceRepository) : ViewModel() {

    private val transactionResultMutableLiveData: MutableLiveData<ResultData<Transaction_result?>> =
        MutableLiveData()
    val transactionResultLiveData: LiveData<ResultData<Transaction_result?>>
        get() = transactionResultMutableLiveData

    suspend fun getTransactionResult(isFriend: Boolean, groupCode: String, myPhone: String) {
        balanceRepo.getTransactionResultList(isFriend, groupCode, myPhone).onStart {
            emit(ResultData.Loading())
        }.collect{
            transactionResultMutableLiveData.postValue(it)
        }
    }
}