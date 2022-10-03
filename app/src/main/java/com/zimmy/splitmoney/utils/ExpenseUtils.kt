package com.zimmy.splitmoney.utils

import android.util.Log
import com.zimmy.splitmoney.fragments.FriendFragment
import com.zimmy.splitmoney.models.Expense
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_result
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ExpenseUtils {
    companion object {

        val TAG: String = FriendFragment::class.java.simpleName

        private fun getMinAdvanced(transactionList: ArrayList<Transaction>): Int {
            var minIndex: Int = 0
            for (i in transactionList.indices) {
                if (transactionList[i].amount < transactionList[minIndex].amount) {
                    minIndex = i
                }
            }
            return minIndex
        }

        private fun getMaxAdvanced(transactionList: ArrayList<Transaction>): Int {
            var maxIndex = 0;
            for (i in transactionList.indices) {
                if (transactionList[i].amount > transactionList[maxIndex].amount) {
                    maxIndex = i
                }
            }
            return maxIndex
        }

        private fun minOf2Advanced(x: Double, y: Double): Double {
            return if (x < y) x else y
        }

        fun minCashFlowRec(
            transactionList: ArrayList<Transaction>,
            transactionResult: ArrayList<Transaction_result>
        ) {
            val mxCredit = getMaxAdvanced(transactionList)
            val mxDebit = getMinAdvanced(transactionList)

            if (transactionList[mxCredit].amount == 0.0 && transactionList[mxDebit].amount == 0.0) {
                return
            }

            val minimumOfTwo =
                minOf2Advanced(-transactionList[mxDebit].amount, transactionList[mxCredit].amount)
//            transactionList[mxCredit].amount -= minimumOfTwo
            transactionList[mxCredit].amount =
                BigDecimal(transactionList[mxCredit].amount.toString()).subtract(
                    BigDecimal(minimumOfTwo.toString())
                ).toDouble()

//            transactionList[mxDebit].amount += minimumOfTwo
            transactionList[mxDebit].amount =
                BigDecimal(transactionList[mxDebit].amount).add(BigDecimal(minimumOfTwo))
                    .toDouble()

            Log.v(
                "TRANSACTION RESULT",
                "${transactionList[mxDebit].friendPhone} pays $minimumOfTwo to ${transactionList[mxCredit].friendPhone}\n"
            )
            transactionResult.add(
                Transaction_result(
                    transactionList[mxCredit].friendPhone,
                    transactionList[mxDebit].friendPhone,
                    minimumOfTwo
                )
            )
            minCashFlowRec(transactionList, transactionResult)
        }


        class TimeComparator : Comparator<Expense> {
            override fun compare(p0: Expense?, p1: Expense?): Int {
                if (p0 == null || p1 == null) {
                    return 0;
                }
                return p0.payment_time.compareTo(p1.payment_time)
            }
        }
    }
}