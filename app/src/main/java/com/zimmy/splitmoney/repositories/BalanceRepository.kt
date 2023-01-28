package com.zimmy.splitmoney.repositories

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.resultdata.ResultData
import com.zimmy.splitmoney.settleup.balance.BalanceActivity
import com.zimmy.splitmoney.utils.ExpenseUtils
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.math.BigDecimal

class BalanceRepository {
    private val TAG = BalanceActivity::class.java.simpleName

    fun getTransactionResultList(
        isFriend: Boolean,
        groupCode: String,
        myPhone: String
    ): Flow<ResultData<Transaction_result>> {
        return callbackFlow {
            if (isFriend) {
                //todo check for true condition
            } else {
                val phoneMap = HashMap<String, String>()
                val netBalance = ArrayList<Transaction>()
                val transactionResult = ArrayList<Transaction_result>()
                var count: Long

                val groupReference =
                    FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
                groupReference.child(groupCode).child(Konstants.MEMBERS)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (phone in snapshot.children) {
                                groupReference.child(groupCode).child(Konstants.MEMBERS)
                                    .child(phone.key.toString())
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val friend = snapshot.getValue(Friend::class.java)
                                            phoneMap[phone.key.toString()] = friend!!.name
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.v(TAG, "database error ${error.message}")
                                        }

                                    })
                            }

                            groupReference.child(groupCode).child(Konstants.EXPENSE_GLOBAL)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            count = snapshot.childrenCount
                                            for (phone in snapshot.children) {
                                                groupReference.child(groupCode)
                                                    .child(Konstants.EXPENSE_GLOBAL)
                                                    .child(phone.key.toString())
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            netBalance.add(
                                                                Transaction(
                                                                    phone.key.toString(),
                                                                    snapshot.getValue(Double::class.java)!!
                                                                )
                                                            )
                                                            if (netBalance.size == count.toInt()) {

                                                                if (checkIfBalanced(netBalance)) {
                                                                    if (netBalance.isEmpty()) {
                                                                        trySendBlocking(
                                                                            ResultData.Failed(
                                                                                Konstants.NO_TRANSACTION
                                                                            )
                                                                        )
                                                                    } else {
                                                                        minCashFlowRec(
                                                                            netBalance,
                                                                            transactionResult,
                                                                            this@callbackFlow
                                                                        )
                                                                        var needSettleUp = false
                                                                        for (ele in transactionResult) {
                                                                            Log.v(
                                                                                TAG,
                                                                                "${ele.sender} will send ${ele.receiver} the amount ${ele.amount} "
                                                                            )
                                                                            if (ele.sender == myPhone || ele.receiver == myPhone) {
                                                                                needSettleUp = true
                                                                            }
                                                                        }
                                                                        if (!needSettleUp) {
                                                                            trySendBlocking(
                                                                                ResultData.Anonymous(
                                                                                    Konstants.ALREADY_SETTLED_UP
                                                                                )
                                                                            )
                                                                        } else {
                                                                            trySendBlocking(
                                                                                ResultData.Anonymous(
                                                                                    Konstants.SETTLE_UP_VISIBLE
                                                                                )
                                                                            )
                                                                        }
                                                                    }
                                                                } else {
                                                                    trySendBlocking(
                                                                        ResultData.Anonymous(
                                                                            Konstants.BALANCE_IMBALANCE
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                            Log.v(
                                                                TAG,
                                                                "database error ${error.message}"
                                                            )
                                                        }
                                                    })
                                            }

                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.v(TAG, "database error ${error.message}")
                                    }

                                })

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.v(TAG, "database error ${error.message}")
                        }

                    })


            }
        }
    }

    private fun minCashFlowRec(
        transactionList: ArrayList<Transaction>,
        transactionResult: ArrayList<Transaction_result>,
        producerScope: ProducerScope<ResultData<Transaction_result>>
    ) {
        val mxCredit = ExpenseUtils.getMaxAdvanced(transactionList)
        val mxDebit = ExpenseUtils.getMinAdvanced(transactionList)

        if (transactionList[mxCredit].amount == 0.0 && transactionList[mxDebit].amount == 0.0) {
            return
        }

        val minimumOfTwo =
            ExpenseUtils.minOf2Advanced(
                -transactionList[mxDebit].amount,
                transactionList[mxCredit].amount
            )
        transactionList[mxCredit].amount =
            BigDecimal(transactionList[mxCredit].amount.toString()).subtract(
                BigDecimal(minimumOfTwo.toString())
            ).toDouble()

        transactionList[mxDebit].amount =
            BigDecimal(transactionList[mxDebit].amount).add(BigDecimal(minimumOfTwo))
                .toDouble()

        Log.v(
            "TRANSACTION RESULT",
            "${transactionList[mxDebit].friendPhone} pays $minimumOfTwo to ${transactionList[mxCredit].friendPhone}\n"
        )

        producerScope.trySendBlocking(
            ResultData.Success(
                Transaction_result(
                    transactionList[mxCredit].friendPhone,
                    transactionList[mxDebit].friendPhone,
                    minimumOfTwo
                )
            )
        )

        transactionResult.add(
            Transaction_result(
                transactionList[mxCredit].friendPhone,
                transactionList[mxDebit].friendPhone,
                minimumOfTwo
            )
        )
        minCashFlowRec(transactionList, transactionResult, producerScope)
    }

    private fun checkIfBalanced(netBalance: ArrayList<Transaction>): Boolean {
        var totalBalance = 0.0
        for (ele in netBalance) {
            totalBalance =
                (BigDecimal(totalBalance.toString()).add(BigDecimal(ele.amount.toString())))
                    .toDouble()
        }
        return totalBalance == 0.0
    }
}