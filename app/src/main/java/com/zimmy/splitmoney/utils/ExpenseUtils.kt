package com.zimmy.splitmoney.utils

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.zimmy.splitmoney.fragments.FriendFragment
import com.zimmy.splitmoney.models.Expense
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_result
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ExpenseUtils {
    companion object {

        //function to return time string as June 26, 2022
        fun getDateString(date: Date): String {
            val fmt = SimpleDateFormat("MMMM d, yyyy")
            return fmt.format(date)
        }

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
                return;
            }

            val minimumOfTwo =
                minOf2Advanced(-transactionList[mxDebit].amount, transactionList[mxCredit].amount)
            transactionList[mxCredit].amount -= minimumOfTwo
            transactionList[mxDebit].amount += minimumOfTwo

            print("${transactionList[mxDebit].friend} pays ${minimumOfTwo} to ${transactionList[mxCredit].friend}\n")
            transactionResult.add(
                Transaction_result(
                    transactionList[mxCredit].friend,
                    transactionList[mxDebit].friend,
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
                return p0.timestamp.compareTo(p1.timestamp)
            }

        }
    }
}