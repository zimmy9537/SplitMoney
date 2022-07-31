package com.zimmy.splitmoney.expense

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Transaction_SettleUp
import kotlin.math.absoluteValue

class SettleUpActivity : AppCompatActivity() {

    lateinit var transaction: Transaction_SettleUp
    var isFriend: Boolean = false
    lateinit var groupCode: String
    lateinit var myName: String
    lateinit var myPhone: String

    lateinit var oweTv: TextView
    lateinit var senderTv: TextView
    lateinit var receiverTv: TextView
    lateinit var settleUp: Button

    lateinit var groupReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settle_up)

        oweTv = findViewById(R.id.oweTv)
        senderTv = findViewById(R.id.senderTv)
        receiverTv = findViewById(R.id.receiverTv)
        settleUp = findViewById(R.id.settleUp)

        myName = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE).getString(
            Konstants.NAME,
            "not working"
        ).toString()

        myPhone = getSharedPreferences(
            Konstants.PERSONAL,
            Context.MODE_PRIVATE
        ).getString(Konstants.PHONE, "9537830943").toString()

        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)
        if (isFriend) {
        } else {
            transaction = intent.getSerializableExtra(Konstants.DATA) as Transaction_SettleUp
            groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()

            if (transaction.amount > 0) {
                oweTv.text = "${transaction.friendName} owes you $${transaction.amount}"
                senderTv.text = transaction.friendName[0].toString()
                receiverTv.text = myName[0].toString()
            } else {
                oweTv.text = "You owe ${transaction.friendName} $${transaction.amount}"
                senderTv.text = myName[0].toString()
                receiverTv.text = transaction.friendName[0].toString()
            }
        }

        settleUp.setOnClickListener {
            if (isFriend) {
            } else {
                groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
                    .child(groupCode);
                if (transaction.amount > 0) {
                    //make changes in expense global
                    //i get credit, friend's account is debited
                    //my total_expense decrease by transaction.amount, his negative amount become positive by transaction.amount

                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (phone in snapshot.children) {
                                    if (phone.key.toString() == myPhone) {
                                        groupReference.child(Konstants.EXPENSE_GLOBAL)
                                            .child(phone.key.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var currentValue =
                                                        snapshot.getValue(Double::class.java)!!
                                                    currentValue -= transaction.amount.absoluteValue
                                                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                                                        .child(phone.key.toString())
                                                        .setValue(currentValue)

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.v(
                                                        SettleUpActivity::class.java.simpleName,
                                                        "database error ${error.message}"
                                                    )
                                                }

                                            })
                                    } else if (phone.key.toString() == transaction.friendPhone) {
                                        groupReference.child(Konstants.EXPENSE_GLOBAL)
                                            .child(phone.key.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var currentValue =
                                                        snapshot.getValue(Double::class.java)!!
                                                    currentValue += transaction.amount.absoluteValue
                                                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                                                        .child(phone.key.toString())
                                                        .setValue(currentValue)

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.v(
                                                        SettleUpActivity::class.java.simpleName,
                                                        "database error ${error.message}"
                                                    )
                                                }

                                            })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                } else {
                    //make changes in expense global
                    //friend's get credit, my account is debited
                    //friend's total_expense decrease by transaction.amount, my negative amount become positive by transaction.amount

                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (phone in snapshot.children) {
                                    if (phone.key.toString() == myPhone) {
                                        groupReference.child(Konstants.EXPENSE_GLOBAL)
                                            .child(phone.key.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var currentValue =
                                                        snapshot.getValue(Double::class.java)!!
                                                    currentValue += transaction.amount.absoluteValue
                                                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                                                        .child(phone.key.toString())
                                                        .setValue(currentValue)

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.v(
                                                        SettleUpActivity::class.java.simpleName,
                                                        "database error ${error.message}"
                                                    )
                                                }

                                            })
                                    } else if (phone.key.toString() == transaction.friendPhone) {
                                        groupReference.child(Konstants.EXPENSE_GLOBAL)
                                            .child(phone.key.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var currentValue =
                                                        snapshot.getValue(Double::class.java)!!
                                                    currentValue -= transaction.amount.absoluteValue
                                                    groupReference.child(Konstants.EXPENSE_GLOBAL)
                                                        .child(phone.key.toString())
                                                        .setValue(currentValue)

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.v(
                                                        SettleUpActivity::class.java.simpleName,
                                                        "database error ${error.message}"
                                                    )
                                                }

                                            })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                val intent = Intent(this@SettleUpActivity, SettledUpActivity::class.java)
                intent.putExtra(Konstants.DATA, transaction)
                startActivity(intent)
            }
        }
    }
}