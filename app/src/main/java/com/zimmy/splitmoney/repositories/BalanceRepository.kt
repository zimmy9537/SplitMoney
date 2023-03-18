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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

class BalanceRepository {
    private val TAG = BalanceRepository::class.java.simpleName

    suspend fun getMemberList(
        groupCode: String
    ): Flow<ResultData<HashMap<String, String>>> {
        return callbackFlow {
            val phoneMap: HashMap<String, String> = HashMap()
            val groupReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
            groupReference.child(groupCode).child(Konstants.MEMBERS)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val memberSize = snapshot.childrenCount
                        var count = 0L
                        for (phone in snapshot.children) {
                            phone.ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friend = snapshot.getValue(Friend::class.java)
                                    if (friend == null) {
                                        Log.d(TAG, "null here")
                                        return
                                    }
                                    phoneMap[phone.key.toString()] = friend.name
//                                        count++
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                    trySendBlocking(ResultData.Failed())
                                }

                            })
                        }
//                        while (count != memberSize) {
//                            //wait
//                        }
                        trySendBlocking(ResultData.Success(phoneMap))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "database error ${error.message}")
                        trySendBlocking(ResultData.Failed())
                    }
                })
            awaitClose {
                Log.d(TAG, "await close")
            }
        }
    }


    fun getTransactionResultList2(
        isFriend: Boolean,
        groupCode: String,
        myPhone: String
    ): Flow<ResultData<Transaction_result>> {
        return callbackFlow {
            if (isFriend) {
                //does code even reach here??
            } else {
                Log.d("Anonymous", "call view repository")
                var count: Long
                val netBalance = ArrayList<Transaction>()
                val transactionResult = ArrayList<Transaction_result>()
                val groupReference =
                    FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
                groupReference.child(groupCode).child(Konstants.EXPENSE_GLOBAL)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Log.d("Anonymous", "call view snapshot")
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
                                                    Log.d(
                                                        "Anonymous",
                                                        "call view net balance equal"
                                                    )
                                                    if (checkIfBalanced(netBalance)) {
                                                        if (netBalance.isEmpty()) {
                                                            trySend(
                                                                ResultData.Failed(
                                                                    Konstants.NO_TRANSACTION
                                                                )
                                                            )
                                                        } else {
                                                            printNetBalance(netBalance)
                                                            minCashFlowRec(
                                                                netBalance,
                                                                transactionResult
                                                            )
                                                            var needSettleUp = false
                                                            for (ele in transactionResult) {
                                                                Log.d(
                                                                    "Anonymous",
                                                                    "call view success call"
                                                                )
                                                                val result = Transaction_result(
                                                                    ele.receiver,
                                                                    ele.sender,
                                                                    ele.amount
                                                                )
                                                                trySend(
                                                                    ResultData.Success(result)
                                                                )
                                                            }
                                                            for (ele in transactionResult) {
                                                                Log.v(
                                                                    TAG,
                                                                    "${ele.sender} will send ${ele.receiver} the amount ${ele.amount} "
                                                                )
                                                                if (ele.sender == myPhone || ele.receiver == myPhone) {
                                                                    needSettleUp = true
                                                                }
                                                                Log.d(
                                                                    "Anonymous",
                                                                    "call view need settle up $needSettleUp"
                                                                )
                                                            }
                                                            if (!needSettleUp) {
                                                                trySend(
                                                                    ResultData.Anonymous(
                                                                        Konstants.ALREADY_SETTLED_UP
                                                                    )
                                                                )
                                                            } else {
                                                                trySend(
                                                                    ResultData.Anonymous(
                                                                        Konstants.SETTLE_UP_VISIBLE
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        trySend(
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
            awaitClose { Log.d(TAG, "await close") }
        }
    }

    private fun printNetBalance(netBalance: ArrayList<Transaction>) {
        for (transaction in netBalance) {
            Log.d(TAG, "balance:- ${transaction.friendPhone}, ${transaction.amount}")
        }
    }

    private fun minCashFlowRec(
        transactionList: ArrayList<Transaction>,
        transactionResult: ArrayList<Transaction_result>
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

        val creditBigDecimal = BigDecimal.valueOf(transactionList[mxCredit].amount)
        val debitBigDecimal = BigDecimal.valueOf(transactionList[mxDebit].amount)
        val minBigDecimal = BigDecimal.valueOf(minimumOfTwo)

        transactionList[mxCredit].amount =
            (creditBigDecimal.subtract(minBigDecimal)).toDouble()

        transactionList[mxDebit].amount =
            debitBigDecimal.add(minBigDecimal).toDouble()

        transactionResult.add(
            Transaction_result(
                transactionList[mxCredit].friendPhone,
                transactionList[mxDebit].friendPhone,
                minimumOfTwo
            )
        )
        minCashFlowRec(transactionList, transactionResult)
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