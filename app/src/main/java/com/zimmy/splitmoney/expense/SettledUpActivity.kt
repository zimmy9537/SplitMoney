package com.zimmy.splitmoney.expense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zimmy.splitmoney.HomeActivity
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Transaction_SettleUp


class SettledUpActivity : AppCompatActivity() {

    lateinit var done: Button
    lateinit var resultTv: TextView

    lateinit var transaction: Transaction_SettleUp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settled_up)
        transaction = intent.getSerializableExtra(Konstants.DATA) as Transaction_SettleUp

        resultTv = findViewById(R.id.resultTextView)
        done = findViewById(R.id.done)

        resultTv.text = "Your account with ${transaction.friendName} has been settled up"

        done.setOnClickListener {
            val intent = Intent(this@SettledUpActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}