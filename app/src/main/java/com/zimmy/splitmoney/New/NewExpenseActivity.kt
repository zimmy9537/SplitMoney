package com.zimmy.splitmoney.New

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.expense.PaidByActivity
import com.zimmy.splitmoney.expense.SplitActivity
import com.zimmy.splitmoney.models.Expense
import com.zimmy.splitmoney.models.Friend
import java.text.SimpleDateFormat
import java.util.*
import kotlin.streams.asSequence

class NewExpenseActivity : AppCompatActivity() {

    lateinit var cancel: Button
    lateinit var save: Button
    lateinit var expenseName: EditText
    lateinit var day: TextView

    //todo test the code and repeat for the percentage method
    //todo later work on the save button on click

    //todo this day is creating a fuck out of me
    lateinit var withTv: TextView
    lateinit var expenseImageView: ImageView
    lateinit var expenseAmount: EditText
    lateinit var currency: TextView
    lateinit var paidBy: TextView
    lateinit var splitTechnique: TextView

    lateinit var myReference: DatabaseReference
    lateinit var friendReference: DatabaseReference
    lateinit var groupReference: DatabaseReference

    //intent
    private var isFriend: Int = Konstants.INDIVIDUALEXPENSE

    //ifFriend
    lateinit var friendName: String
    lateinit var friendPhone: String
    var TAG = NewExpenseActivity::class.java.simpleName
    lateinit var expenseMap: HashMap<String, Double>
    lateinit var friendDetailList: ArrayList<Friend>
    private lateinit var expensePercent: HashMap<String, Double>
    private lateinit var resultMap: HashMap<String, Boolean>
    private lateinit var groupCode: String

    var splitActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this@NewExpenseActivity, "result is ok", Toast.LENGTH_SHORT)
                    .show()
                val intent = result.data
                expensePercent = HashMap()
                if (intent != null) {
                    expensePercent =
                        intent.getSerializableExtra(Konstants.EQUAL_PERCENT_MAP) as HashMap<String, Double>

                    for (ele in expensePercent) {
                        Log.v(TAG, "${ele.key} pays ${ele.value}%")
                    }
                } else {
                    Toast.makeText(this@NewExpenseActivity, "data null ", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@NewExpenseActivity,
                    "result is not ok",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    var paidActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == RESULT_OK) {
                //handle result
                val intent = result.data
                if (intent != null) {
                    resultMap = HashMap()
                    resultMap =
                        intent.getSerializableExtra(Konstants.DATA) as HashMap<String, Boolean>
                }
            } else {
                Toast.makeText(this, "result not ok", Toast.LENGTH_SHORT).show()
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)
        resultMap = HashMap()

        //todo write code for group expense here
        isFriend = intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = intent.getStringExtra(Konstants.PHONE).toString()
            resultMap[getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE).getString(
                Konstants.PHONE,
                "9537830943"
            ).toString()] = true
            resultMap[friendPhone] = false
        } else {
            //case of group here
            //TODO NEED thia list from the previous activities also send Konstant.EXPENSE as group expense.
            //ALSO send the group code
            friendDetailList = intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
            groupCode = intent.getStringExtra(Konstants.GROUPS).toString()
        }

        cancel = findViewById(R.id.cancel)
        save = findViewById(R.id.save)
        expenseName = findViewById(R.id.expenseName)
        day = findViewById(R.id.dateTv)
        expenseImageView = findViewById(R.id.expenseImage)
        withTv = findViewById(R.id.withTextView)
        expenseAmount = findViewById(R.id.expenseAmount)
        currency = findViewById(R.id.currency)
        paidBy = findViewById(R.id.paidBy)
        splitTechnique = findViewById(R.id.splitTechnique)


        cancel.setOnClickListener {
            finish()
        }

        save.setOnClickListener {
            if (expenseAmount.text.isEmpty()) {
                Toast.makeText(this, "Enter some valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            savePayment(expensePercent, resultMap)
        }

        if (isFriend == Konstants.INDIVIDUALEXPENSE)
            withTv.text = "With you and $friendName"
        else {
            //this ones for the group
        }

        currency.setOnClickListener {
            Toast.makeText(this, "This feature is coming soon", Toast.LENGTH_SHORT).show()
        }

        paidBy.setOnClickListener {
            if (isFriend == Konstants.INDIVIDUALEXPENSE) {
                if (paidBy.text == friendName) {
//                    paidByPhone =
//                        getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE).getString(
//                            Konstants.PHONE,
//                            "95378300943"
//                        ).toString()
                    resultMap[getSharedPreferences(
                        Konstants.PERSONAL,
                        Context.MODE_PRIVATE
                    ).getString(
                        Konstants.PHONE,
                        "95378300943"
                    ).toString()] = true
                    resultMap[friendPhone] = false
                    paidBy.text = "You"
                } else {
                    paidBy.text = friendName
                    resultMap[friendPhone] = true
                    resultMap[getSharedPreferences(
                        Konstants.PERSONAL,
                        Context.MODE_PRIVATE
                    ).getString(
                        Konstants.PHONE,
                        "95378300943"
                    ).toString()] = true
//                    paidByPhone = friendPhone
                }
            } else {
                val intent = Intent(this@NewExpenseActivity, PaidByActivity::class.java)
                intent.putExtra(Konstants.DATA, friendDetailList)
                intent.putExtra(Konstants.RESULT_MAP, resultMap)
                paidActivityResultLauncher.launch(intent)
            }
        }

        splitTechnique.setOnClickListener {

            if (expenseAmount.text.isEmpty()) {
                Toast.makeText(
                    this@NewExpenseActivity,
                    "enter some valid amount",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intent = Intent(this@NewExpenseActivity, SplitActivity::class.java)
            if (isFriend == Konstants.INDIVIDUALEXPENSE) {
                intent.putExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)
                intent.putExtra(Konstants.PHONE, friendPhone)
                intent.putExtra(Konstants.NAME, friendName)
                intent.putExtra(Konstants.AMOUNT, expenseAmount.text.toString().toDouble())
            } else {
                intent.putExtra(Konstants.EXPENSE, Konstants.GROUPEXPENSE)
                intent.putExtra(Konstants.DATA, friendDetailList)
            }
            splitActivityResultLauncher.launch(intent)
        }
    }

    fun expenseCodeGenerator(): String {
        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        return Random().ints(5, 0, source.length).asSequence()
            .map(source::get)
            .joinToString("")
    }

    fun savePayment(expensePercent: HashMap<String, Double>, paidByMap: HashMap<String, Boolean>) {
        expenseMap = HashMap()
        var amount = expenseAmount.text.toString().toDouble()
        for (ele in expensePercent) {
            expenseMap[ele.key] = amount * ele.value / 100
            Log.v(TAG, "${ele.key} pays $${amount * ele.value / 100}")
        }
        for (ele in paidByMap) {
            Log.v(TAG, "${ele.key} is paying ${ele.value}")
        }
        //todo input this time according to the calender view
        //todo somehow try to get time as Date object for that particular day later use the function in expense Utils class
        //currently it is saving string of current day with desired format
        val newExpenseCode = expenseCodeGenerator()
        val date = Date()
        val fmt = SimpleDateFormat("MMMM d, yyyy")
        val desiredDateString = fmt.format(date)
        var thosePaid = 0
        for (ele in paidByMap) {
            if (ele.value) {
                thosePaid++
            }
        }

        //expenseMap and paidByMap if ok
        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            amount /= 2
            val myPhone = getSharedPreferences(
                Konstants.PERSONAL,
                Context.MODE_PRIVATE
            ).getString(Konstants.PHONE, "9537830943")
            val isIn = paidByMap[myPhone]
            val expense = Expense(
                isIn!!,
                newExpenseCode,
                expenseMap,
                expenseName.text.toString(),
                paidByMap,
                expenseAmount.text.toString().toDouble(),
                desiredDateString
            )
            val ifPaid = amount / thosePaid

            //mine
            var myResult = 0.00
            if (paidByMap[myPhone]!!) {
                myResult = ifPaid
            }
            myResult -= expenseMap[myPhone]!!
            myReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(myPhone!!)
                    .child(Konstants.FRIENDS)
            myReference.child(friendPhone).child(Konstants.EXPENSE).child(newExpenseCode)
                .setValue(expense)
            var result: Double
            myReference.child(friendPhone).child(Konstants.RESULT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        result = snapshot.getValue(Double::class.java)!!
                        result += myResult
                        myReference.child(friendPhone).child(Konstants.RESULT).setValue(result)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.v(TAG, "database error ${error.message}")
                    }

                })

            //friend
            var friendResult = 0.00
            if (paidByMap[friendPhone]!!) {
                friendResult = ifPaid
            }
            friendResult -= expenseMap[friendPhone]!!
            expense.expenseIn = paidByMap[friendPhone]!!
            friendReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(friendPhone)
                    .child(Konstants.FRIENDS)
            friendReference.child(myPhone).child(Konstants.EXPENSE).child(newExpenseCode)
                .setValue(expense)
            friendReference.child(myPhone).child(Konstants.RESULT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        result = snapshot.getValue(Double::class.java)!!
                        if (result != null) {
                            result += friendResult
                            friendReference.child(myPhone).child(Konstants.RESULT).setValue(result)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.v(TAG, "database error ${error.message}")
                    }

                })
        } else { //group
            var expense = Expense(
                newExpenseCode,
                expenseMap,
                expenseName.text.toString(),
                paidByMap,
                amount,
                desiredDateString
            )
            groupReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS).child(groupCode)
            groupReference.child(Konstants.EXPENSE).child(newExpenseCode).setValue(expense)

            //individual person
            for (ele in paidByMap) {
                val memberPhone = ele.key
                val isIn = paidByMap[memberPhone]
                val expense = Expense(
                    isIn!!,
                    newExpenseCode,
                    expenseMap,
                    expenseName.text.toString(),
                    paidByMap,
                    expenseAmount.text.toString().toDouble(),
                    desiredDateString
                )
                var result: Double
                result = if (isIn) {
                    amount / thosePaid - expenseMap[memberPhone]!!
                } else {
                    -expenseMap[memberPhone]!!
                }
                friendReference = FirebaseDatabase.getInstance().reference.child(Konstants.USERS)
                    .child(memberPhone).child(Konstants.GROUPS)
                friendReference.child(groupCode).child(Konstants.EXPENSE).child(newExpenseCode)
                    .setValue(expense)
                friendReference.child(groupCode).child(Konstants.RESULT).setValue(result)
            }
        }
    }
}