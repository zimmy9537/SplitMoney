package com.zimmy.splitmoney

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.firebase.database.*
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.expense.BeforeSettleUpActivity
import com.zimmy.splitmoney.expense.SettleUpActivity
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
    var count: Long = 0
    lateinit var phoneMap: HashMap<String, String>
    lateinit var myPhone: String

    lateinit var alreadySettleUp: TextView
    lateinit var progress: ProgressBar
    lateinit var settleUp: Button
    lateinit var balanceLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_acitvity)
        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)

        myPhone = getSharedPreferences(
            Konstants.PERSONAL,
            Context.MODE_PRIVATE
        ).getString(Konstants.PHONE, "no phone").toString()

        balanceLinearLayout = findViewById(R.id.balances)
        settleUp = findViewById(R.id.settleUp)
        alreadySettleUp = findViewById(R.id.alreadySettled)
        progress = findViewById(R.id.progress)

        phoneMap = HashMap()

        groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)

        settleUp.setOnClickListener {
            if (isFriend) {
                val intent = Intent(this@BalanceAcitvity, SettleUpActivity::class.java)
                intent.putExtra(Konstants.FRIENDS, isFriend)
                startActivity(intent)
            } else {
                val intent = Intent(this@BalanceAcitvity, BeforeSettleUpActivity::class.java)
                intent.putExtra(Konstants.FRIENDS, isFriend)
                intent.putExtra(Konstants.GROUP_CODE, groupCode)
                startActivity(intent)
            }
        }


        if (isFriend) {
        } else {
            groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()
            members = ArrayList()
            netBalance = ArrayList()
            transactionResult = ArrayList()

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
                                                                //balanced we can proceed
                                                                if (netBalance.isEmpty()) {
                                                                    Toast.makeText(
                                                                        this@BalanceAcitvity,
                                                                        "No transactions till now",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                } else {
                                                                    ExpenseUtils.minCashFlowRec(
                                                                        netBalance,
                                                                        transactionResult
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
                                                                        alreadySettleUp.visibility =
                                                                            View.VISIBLE
                                                                    } else {
                                                                        settleUp.visibility =
                                                                            View.VISIBLE
                                                                    }
                                                                    progress.visibility = View.GONE
                                                                    fillTheLinearLayout(
                                                                        transactionResult
                                                                    )
                                                                }
                                                            } else {
                                                                Log.v(
                                                                    TAG,
                                                                    "the arrayList is not balanced"
                                                                )
                                                                Toast.makeText(
                                                                    this@BalanceAcitvity,
                                                                    "balance imbalance",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
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
            val text = "${phoneMap[ele.sender]} pays ${phoneMap[ele.receiver]} $${ele.amount}"
            resultTextView.text = text
            balanceLinearLayout.addView(view)
        }
    }
}