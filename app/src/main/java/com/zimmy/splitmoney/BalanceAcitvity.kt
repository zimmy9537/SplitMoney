package com.zimmy.splitmoney

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.utils.ExpenseUtils

class BalanceAcitvity : AppCompatActivity() {

    lateinit var groupCode: String
    var isFriend: Boolean = true
    lateinit var netBalance: ArrayList<Transaction>
    lateinit var members: ArrayList<Friend>
    lateinit var transactionResult: ArrayList<Transaction_result>
    lateinit var groupReference: DatabaseReference
    var TAG = BalanceAcitvity::class.java.simpleName

    lateinit var balanceLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_acitvity)
        balanceLinearLayout = findViewById(R.id.balances)

        groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)

        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)
        if (isFriend) {
        } else {
            groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()
            members = ArrayList()
            netBalance = ArrayList()
            transactionResult = ArrayList()

            groupReference.child(groupCode).child(Konstants.EXPENSE_GLOBAL)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (phone in snapshot.children) {
                            groupReference.child(groupCode).child(Konstants.EXPENSE_GLOBAL)
                                .child(phone.key.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        netBalance.add(
                                            Transaction(
                                                phone.key.toString(),
                                                snapshot.getValue(Double::class.java)!!
                                            )
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.v(TAG, "database error ${error.message}")
                                    }

                                })
                        }
                        if (checkIfBalanced(netBalance)) {
                            //balanced we can proceed
                            if (transactionResult.isEmpty()) {
                                Toast.makeText(
                                    this@BalanceAcitvity,
                                    "No transactions till now",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                            ExpenseUtils.minCashFlowRec(netBalance, transactionResult)
                            for (ele in transactionResult) {
                                Log.v(
                                    TAG,
                                    "${ele.sender} will send ${ele.receiver} the amount ${ele.amount} "
                                )
                            }
                            fillTheLinearLayout(transactionResult)

                        } else {
                            Log.v(TAG, "the arrayList is not balanced")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.v(TAG, "database error ${error.message}")
                    }

                })
        }
    }

    fun checkIfBalanced(netBalance: ArrayList<Transaction>): Boolean {
        var totalBalance = 0.0
        for (ele in netBalance)
            totalBalance += ele.amount
        return totalBalance == 0.0
    }

    fun fillTheLinearLayout(transactionResult: ArrayList<Transaction_result>) {
        for (ele in transactionResult) {
            val view = LayoutInflater.from(this@BalanceAcitvity)
                .inflate(R.layout.transaction_result_item, null, false)
            val resultTextView = view.findViewById<TextView>(R.id.resultTextView)
            resultTextView.setText("${ele.sender} pays ${ele.receiver} $${ele.amount}")
            balanceLinearLayout.addView(view)
        }
    }
}