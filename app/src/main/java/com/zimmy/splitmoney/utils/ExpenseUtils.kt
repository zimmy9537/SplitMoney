package com.zimmy.splitmoney.utils

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.zimmy.splitmoney.fragments.FriendFragment
import com.zimmy.splitmoney.models.Expense
import com.zimmy.splitmoney.models.Friend
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class ExpenseUtils {
    companion object {

        //function to return time string as June 26, 2022
        fun getDateString(date: Date):String {
            val fmt = SimpleDateFormat("MMMM d, yyyy")
            return fmt.format(date)
        }


        val TAG: String = FriendFragment::class.java.simpleName

        fun findExpenses(
            friendArray: ArrayList<Friend>,
            expenseOutReference: DatabaseReference,
            expenseInReference: DatabaseReference
        ) {

            val expenseArrayList = ArrayList<Expense>()

            //todo this friendArray was never used
            expenseOutReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val expenseCode = snapshot1.key.toString()
                        expenseOutReference.child(expenseCode)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val expense = snapshot.getValue(Expense::class.java)!!
                                    expenseArrayList.add(expense)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error ${error.message}")
                }

            })

            expenseInReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val expenseCode = snapshot1.key.toString()
                        expenseInReference.child(expenseCode)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val expense = snapshot.getValue(Expense::class.java)!!
                                    expenseArrayList.add(expense)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                }

                            })
                    }
                    val comparator = TimeComparator()
                    expenseArrayList.sortWith(comparator)
                    //we have sorted list here
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error ${error.message}")
                }

            })
        }

    }
}

class TimeComparator : Comparator<Expense> {
    override fun compare(p0: Expense?, p1: Expense?): Int {
        if (p0 == null || p1 == null) {
            return 0;
        }
        return p0.timestamp.compareTo(p1.timestamp)
    }

}