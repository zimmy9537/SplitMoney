package com.zimmy.splitmoney

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.zimmy.splitmoney.New.NewExpenseActivity
import com.zimmy.splitmoney.adapters.ExpenseAdapter
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Expense

class IndividualExpenseActivity : AppCompatActivity() {

    lateinit var friendPhone: String
    var TAG = IndividualExpenseActivity::class.java.simpleName
    var resultAmount: Double = 0.0
    private lateinit var personalPreferences: SharedPreferences
    private lateinit var myPhone: String
    private lateinit var friendName: String
    private lateinit var expenseWithFriendList: ArrayList<Expense>

    lateinit var nameTextView: TextView
    lateinit var resultTextView: TextView
    lateinit var settleUp: Button
    lateinit var sendReminder: Button
    lateinit var expenseRv: RecyclerView
    lateinit var addExpense: FloatingActionButton
    lateinit var friendAmount: TextView

    lateinit var friendReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_expense)

        friendPhone = intent.getStringExtra(Konstants.PHONE).toString()
        friendName = intent.getStringExtra(Konstants.NAME).toString()

        nameTextView = findViewById(R.id.friendName)
        resultTextView = findViewById(R.id.result)
        settleUp = findViewById(R.id.settleUp)
        sendReminder = findViewById(R.id.sendReminder)
        expenseRv = findViewById(R.id.groupExpenseRv)
        addExpense = findViewById(R.id.addExpense)
        friendAmount = findViewById(R.id.friendAmount)

        nameTextView.text = friendName

        expenseWithFriendList = ArrayList()

        personalPreferences = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        myPhone = personalPreferences.getString(Konstants.PHONE, "6352938170").toString()

        friendReference =
            FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(myPhone)
                .child(Konstants.FRIENDS).child(friendPhone)

        friendReference.child(Konstants.RESULT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                resultAmount = snapshot.getValue(Double::class.java)!!
                runOnUiThread {
                    resultTextView.setText("$$resultAmount")
                    if (resultAmount > 0) {
                        resultTextView.setText("owes you")
                    } else {
                        resultTextView.setText("you owe")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v(TAG, "database Error ${error.message}")
            }

        })

        addExpense.setOnClickListener {
            val intent = Intent(this@IndividualExpenseActivity, NewExpenseActivity::class.java)
            intent.putExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)
            intent.putExtra(Konstants.PHONE, friendPhone)
            intent.putExtra(Konstants.NAME, friendName)
            startActivity(intent)
        }

        sendReminder.setOnClickListener {
            if (resultAmount > 0) {
                //send message
                val message = "Hey, pls pay me back the $$resultAmount you owe"
                sendSMS(message, friendPhone)
            } else if (resultAmount < 0) {
                Toast.makeText(
                    this@IndividualExpenseActivity,
                    "It seems, you owe money to you friend", Toast.LENGTH_SHORT
                ).show()
            }
        }

        //call adapters
        expenseRv.adapter =
            ExpenseAdapter(expenseWithFriendList, this@IndividualExpenseActivity)
        expenseRv.layoutManager =
            LinearLayoutManager(this@IndividualExpenseActivity)

        friendReference.child(Konstants.EXPENSE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val expenseCode = snapshot1.key.toString()
                        friendReference.child(Konstants.EXPENSE).child(expenseCode)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val expense = snapshot.getValue(Expense::class.java)!!
                                    expenseWithFriendList.add(expense)
                                    (expenseRv.adapter as ExpenseAdapter).notifyDataSetChanged()
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
    }


    private fun sendSMS(message: String, phone: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            val defaultSmsPackageName =
                Telephony.Sms.getDefaultSmsPackage(this) // Need to change the build to API 19
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra("address", phone)
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            if (defaultSmsPackageName != null) // Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName)
            }
            startActivity(sendIntent)
        } else  // For early versions, do what worked for you before.
        {
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra("address", "phoneNumber")
            smsIntent.putExtra("sms_body", "message")
            startActivity(smsIntent)
        }
    }
}