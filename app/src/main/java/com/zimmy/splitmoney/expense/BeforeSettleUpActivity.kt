package com.zimmy.splitmoney.expense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_SettleUp
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.utils.ExpenseUtils
import kotlin.math.absoluteValue
import kotlin.math.sign

class BeforeSettleUpActivity : AppCompatActivity() {


    //TODO TODAY CALL MADE TO GET TRANSACTION LIST NOW SHOW DATA ACCORDINGLY AND PROCEED TO THE SETTLE UP ACTIVITY

    lateinit var settleUpLl: LinearLayout

    lateinit var groupCode: String
    lateinit var groupReference: DatabaseReference
    lateinit var netBalance: ArrayList<Transaction>
    lateinit var transactionResult: ArrayList<Transaction_result>
    var TAG = BeforeSettleUpActivity::class.java.simpleName
    var count: Long = 0
    lateinit var phoneMap: HashMap<String, String>
    lateinit var myPhone: String
    lateinit var transactionSettleUp: ArrayList<Transaction_SettleUp>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_settle_up_activtity)
        groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()
        Log.v(TAG, "groupCode $groupCode")

        myPhone = getSharedPreferences(
            Konstants.PERSONAL,
            Context.MODE_PRIVATE
        ).getString(Konstants.PHONE, "9537830943").toString()

        groupReference =
            FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS).child(groupCode)
        phoneMap = HashMap()
        netBalance = ArrayList()
        transactionResult = ArrayList()

        groupReference.child(Konstants.MEMBERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (phone in snapshot.children) {
                        groupReference.child(Konstants.MEMBERS)
                            .child(phone.key.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friend = snapshot.getValue(Friend::class.java)
                                    if (phone.key != null) {

                                    }
                                    phoneMap[phone.key.toString()] = friend!!.name
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                }

                            })
                    }

                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    count = snapshot.childrenCount
                                    for (phone in snapshot.children) {
                                        groupReference
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
                                                                    this@BeforeSettleUpActivity,
                                                                    "No transactions till now",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                ExpenseUtils.minCashFlowRec(
                                                                    netBalance,
                                                                    transactionResult
                                                                )
                                                                for (ele in transactionResult) {
                                                                    Log.v(
                                                                        TAG,
                                                                        "${ele.sender} will send ${ele.receiver} the amount ${ele.amount} "
                                                                    )
                                                                }
                                                                //code here to show result
//                                                                fillTheLinearLayout(
//                                                                    transactionResult
//                                                                )
                                                                processTransactionResult(
                                                                    transactionResult
                                                                )
                                                            }
                                                        } else {
                                                            Log.v(
                                                                TAG,
                                                                "the arrayList is not balanced"
                                                            )
                                                            Toast.makeText(
                                                                this@BeforeSettleUpActivity,
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


        settleUpLl = findViewById(R.id.settleUp_ll)
    }

    fun checkIfBalanced(netBalance: ArrayList<Transaction>): Boolean {
        var totalBalance = 0.0
        for (ele in netBalance)
            totalBalance += ele.amount
        return totalBalance == 0.0
    }

    fun processTransactionResult(transactionResult: ArrayList<Transaction_result>) {
        //transaction_result=>  var receiver: String?, var sender: String?, var amount: Double
        transactionSettleUp = ArrayList()
        for (ele in transactionResult) {
            if (ele.sender == myPhone) {
                //money gone RED
                transactionSettleUp.add(
                    Transaction_SettleUp(
                        phoneMap[ele.receiver]!!,
                        ele.receiver.toString(),
                        -ele.amount,
                        ele.receiver.toString()
                    )
                )
                Log.v(
                    "DETAILS i am sender",
                    " send to ${phoneMap[ele.receiver]}, having phone no. ${ele.receiver}, amount ${-ele.amount}"
                )
            } else if (ele.receiver == myPhone) {
                //money received GREEN
                transactionSettleUp.add(
                    Transaction_SettleUp(
                        phoneMap[ele.sender]!!,
                        ele.sender.toString(),
                        ele.amount,
                        ele.sender.toString()
                    )
                )
                Log.v(
                    "DETAILS i am receiver",
                    " send to me by ${phoneMap[ele.sender]}, having phone no. ${ele.sender}, amount ${ele.amount}"
                )
            }
        }
        //transactionSettleUp
        for (ele in transactionSettleUp) {
            val view = layoutInflater.inflate(R.layout.before_settle_item, null, false)
            val friendName = view.findViewById<TextView>(R.id.friendName)
            val friendContact = view.findViewById<TextView>(R.id.friendContact)
            val oweString = view.findViewById<TextView>(R.id.oweString)
            val oweAmount = view.findViewById<TextView>(R.id.oweAmount)
            friendName.text = ele.friendName
            friendContact.text = ele.friendEmail
            if (ele.amount > 0) {
                oweString.text = "Owes You"
                Log.v("money", "profit")
            } else {
                oweString.text = "You Owe"
                Log.v("money", "lost")
            }
            oweAmount.text = "$${ele.amount.absoluteValue.toString()}"
            settleUpLl.addView(view)

            view.setOnClickListener {
                val intent = Intent(this@BeforeSettleUpActivity, SettleUpActivity::class.java)
                intent.putExtra(Konstants.FRIENDS, false)
                intent.putExtra(Konstants.DATA, ele)
                intent.putExtra(Konstants.GROUP_CODE, groupCode)
                startActivity(intent)
            }
        }
    }
}