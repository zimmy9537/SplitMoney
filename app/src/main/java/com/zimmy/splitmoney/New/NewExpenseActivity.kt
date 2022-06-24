package com.zimmy.splitmoney.New

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.expense.PaidByActivity
import com.zimmy.splitmoney.expense.SplitActivity
import com.zimmy.splitmoney.models.Friend

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

    //intent
    var isFriend: Int = Konstants.INDIVIDUALEXPENSE

    //ifFriend
    lateinit var friendName: String
    lateinit var friendPhone: String
    var TAG=NewExpenseActivity::class.java.simpleName
    val requestCodeSplitTechnique = 1000
    val requestCodePaidBy = 1001
    lateinit var expenseMap: HashMap<String, Double>
    lateinit var friendDetailList: ArrayList<Friend>
    lateinit var expensePercent:HashMap<String,Double>

    var splitActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this@NewExpenseActivity, "result is ok", Toast.LENGTH_SHORT)
                    .show()
                val intent = result.data
                expensePercent=HashMap()
                if (intent != null) {
                    expensePercent =
                        intent.getSerializableExtra(Konstants.EQUAL_PERCENT_MAP) as HashMap<String, Double>

                    for(ele in expensePercent){
                        Log.v(TAG,"${ele.key} pays ${ele.value}%")
                    }
                }else {
                    Toast.makeText(this@NewExpenseActivity,"data null ",Toast.LENGTH_SHORT).show()
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)

        //todo write code for group expense here
        isFriend = intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = intent.getStringExtra(Konstants.PHONE).toString()
        } else {
            //case of group here
            //TODO NEED thia list from the previous activities also send Konstant.EXPENSE as group expense.
            friendDetailList = intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
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
            Toast.makeText(this, "will work on this now", Toast.LENGTH_SHORT).show()
        }

        if (isFriend == Konstants.INDIVIDUALEXPENSE)
            withTv.text = "With you and $friendName"
        else {
            //this ones for the group
        }

        currency.setOnClickListener {
            Toast.makeText(this, "This feature is coming soon", Toast.LENGTH_SHORT).show()
        }

        expenseMap = HashMap()

        paidBy.setOnClickListener {
            if (isFriend == Konstants.INDIVIDUALEXPENSE) {
                if (paidBy.text == friendName) {
                    paidBy.text = "You"
                } else
                    paidBy.text = friendName
            } else {
                val intent = Intent(this@NewExpenseActivity, PaidByActivity::class.java)
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
}