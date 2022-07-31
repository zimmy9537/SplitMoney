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
import com.zimmy.splitmoney.models.Expense_Group
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Group
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.streams.asSequence

class NewExpenseActivity : AppCompatActivity() {

    lateinit var cancel: Button
    lateinit var save: Button
    lateinit var expenseName: EditText
    lateinit var day: TextView

    //todo test the code and repeat for the percentage method
    //todo later work on the save button on click
    //todo this day is creating a fuck out of me
    //todo the equal fragments and percentage fragment names are not okay

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
    var TAG: String = NewExpenseActivity::class.java.simpleName
    lateinit var expenseMap: HashMap<String, Double>
    lateinit var friendDetailList: ArrayList<Friend>
    private lateinit var expensePercent: HashMap<String, Double>
    private lateinit var resultMap: HashMap<String, Boolean>
    private lateinit var groupCode: String
    private lateinit var myPhone: String


    //todo change the text in the split between text string according to the result of te result launcher
    private var splitActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
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
                        Toast.makeText(this@NewExpenseActivity, "data null ", Toast.LENGTH_SHORT)
                            .show()
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
                        intent.getSerializableExtra(Konstants.RESULT) as HashMap<String, Boolean>
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
        myPhone = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE).getString(
            Konstants.PHONE,
            "9537830943"
        ).toString()

        //todo write code for group expense here
        isFriend = intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        expensePercent = HashMap()
        resultMap = HashMap()
        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = intent.getStringExtra(Konstants.PHONE).toString()
            expensePercent[myPhone] = 50.0
            expensePercent[friendPhone] = 50.0
            resultMap[myPhone] = true
            resultMap[friendPhone] = false
        } else {
            //case of group here
            //TODO NEED thia list from the previous activities also send Konstant.EXPENSE as group expense.
            //ALSO send the group code
            friendDetailList = intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
            groupCode = intent.getStringExtra(Konstants.GROUPS).toString()
            val totalMembers = friendDetailList.size
            for (friend in friendDetailList) {
                expensePercent[friend.phone!!] = 100.00 / totalMembers
                resultMap[friend.phone!!] = friend.phone == myPhone
            }
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
                    resultMap[myPhone] = true
                    resultMap[friendPhone] = false
                    paidBy.text = "You"
                } else {
                    paidBy.text = friendName
                    resultMap[myPhone] = false
                    resultMap[friendPhone] = true
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

        //todo here is the solution for the minute part left of the expense
        var remainder: Double
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        var expenseTotal = 0.00
        for (ele in expensePercent) {
            val roundOff = df.format(amount * ele.value / 100)
            Log.v(TAG, "Individual pay $roundOff")
            expenseTotal += roundOff.toDouble()
        }
        remainder = amount - expenseTotal
        Log.v(TAG,"reminder 1. $remainder")
        remainder=df.format(remainder).toDouble()


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
        val ifPaid = amount / thosePaid

        //expenseMap and paidByMap if ok
        //todo some problem with the individual expense solution
        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            amount /= 2
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

            //mine
            var myResult = 0.00
            if (paidByMap[myPhone]!!) {
                myResult = ifPaid
            }
            myResult -= expenseMap[myPhone]!!
            //this is that minute reminder
            myResult -= remainder
            Log.v(TAG, "reminder is ${remainder}")
            expenseMap[myPhone!!] = expenseMap[myPhone]?.plus(remainder)!!
            myReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(myPhone!!)
                    .child(Konstants.FRIENDS)
            myReference.child(friendPhone).child(Konstants.EXPENSE).child(newExpenseCode)
                .setValue(expense)
            var result: Double = 0.00
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
            val expense = Expense_Group(
                newExpenseCode,
                expenseMap,
                expenseName.text.toString(),
                paidByMap,
                amount,
                desiredDateString
            )

            for (ele in expenseMap) {
                Log.v(TAG, "expense map ${ele.key} pays $${ele.value}")
                //expense map 1234567890 pays $33.333333333333336
            }
            Log.v(TAG, "here ${newExpenseCode}, ${amount}, $desiredDateString")
            //here ZPWH4, 100.0, July 31, 2022

            //individual person

            var netExpense: HashMap<String, Double>
            netExpense = HashMap()
            for (ele in paidByMap) {
                if (ele.value) {
                    netExpense[ele.key] = ifPaid - expenseMap[ele.key]!!
                } else {
                    netExpense[ele.key] = -expenseMap[ele.key]!!
                }
            }
            Log.v(TAG, "reminder is ${remainder}")
            //reminder is 0.010000000000005116

            var remainder2 = 0.00
            for (ele in netExpense) {
                netExpense[ele.key] = df.format(ele.value).toDouble()
                Log.v(TAG, "INDIVIDUAL net expense ${ele.value}")
                //INDIVIDUAL net expense -33.33
                remainder2 += df.format(ele.value).toDouble()
            }
            remainder2 = df.format(remainder2).toDouble()
            Log.v(TAG,"reminder is $remainder")
            //reminder is 0.010000000000005116
            Log.v(TAG, "remainder2 in netExpense $remainder2")
            //remainder2 in netExpense 0.0

            //add extra reminder
            remainder2 = -remainder2
            Log.v(TAG, "remainder2 in netExpense $remainder2")
            for (ele in netExpense) {
                if (netExpense[ele.key]!! > 0) {
                    netExpense[ele.key] = netExpense[ele.key]?.plus(remainder2)!!
                    break
                }
            }

            for (ele in expenseMap){
                expenseMap[ele.key]=df.format(ele.value).toDouble()
            }
            expense.expenseMap=expenseMap

            groupReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS).child(groupCode)
            groupReference.child(Konstants.EXPENSE).child(newExpenseCode).setValue(expense)

            for (ele in netExpense) {
                groupReference.child(Konstants.EXPENSE_GLOBAL).child(ele.key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var result = snapshot.getValue(Double::class.java)!!
                                result += df.format(netExpense[ele.key]!!).toDouble()
                                groupReference.child(Konstants.EXPENSE_GLOBAL).child(ele.key)
                                    .setValue(result)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.v(TAG, "database error ${error.message}")
                        }

                    })
            }

            for (ele in paidByMap) {
                val memberPhone = ele.key
                val isIn = paidByMap[memberPhone]
                var result: Double
                var owe = "i owe no one"
                //todo handle thw event of a neutral expense where net result is zero, rest data is getting updated
                if (isIn!!) {
                    result = amount / thosePaid - expenseMap[memberPhone]!!
                    for (friend in friendDetailList) {
                        if (!paidByMap[friend.phone]!!) {
                            owe = "${friend.name} owes you"
                            break
                        }
                    }
                } else {
                    result = -expenseMap[memberPhone]!!
                    for (friend in friendDetailList) {
                        if (paidByMap[friend.phone]!!) {
                            owe = "you owe ${friend.name}"
                            break
                        }
                    }
                }


                //new here
                friendReference =
                    FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(ele.key)
                friendReference.child(groupCode)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val group = snapshot.getValue(Group::class.java)
                                if (group != null) {
                                    var amount = group.amount
                                    amount += result
                                    group.amount = amount
                                    group.owe = owe
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.v(TAG, "database error ${error.message}")
                        }
                    })
            }
        }
    }
}