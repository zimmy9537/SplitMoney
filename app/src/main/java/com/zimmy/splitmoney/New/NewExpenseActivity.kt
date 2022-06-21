package com.zimmy.splitmoney.New

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.expense.PaidByActivity
import com.zimmy.splitmoney.expense.SplitActivity

class NewExpenseActivity : AppCompatActivity() {

    lateinit var cancel: Button
    lateinit var save: Button
    lateinit var expenseName: EditText
    lateinit var day: TextView

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)

        isFriend = intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = intent.getStringExtra(Konstants.PHONE).toString()
        } else {
            //case of group here
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

        save.setOnClickListener{
            Toast.makeText(this,"will work on this now",Toast.LENGTH_SHORT).show()
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
                    paidBy.text = "You"
                } else
                    paidBy.text = friendName
            } else {
                val intent = Intent(this@NewExpenseActivity, PaidByActivity::class.java)
                startActivity(intent)
            }
        }

        splitTechnique.setOnClickListener {
            val intent = Intent(this@NewExpenseActivity, SplitActivity::class.java)
            startActivity(intent)
        }
    }
}