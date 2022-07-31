package com.zimmy.splitmoney.expense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zimmy.splitmoney.HomeActivity
import com.zimmy.splitmoney.R


class SettledUpActivity : AppCompatActivity() {

    lateinit var done: Button
    lateinit var resultTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settled_up)

        resultTv = findViewById(R.id.resultTextView)
        done = findViewById(R.id.done)
        done.setOnClickListener {
            val intent = Intent(this@SettledUpActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}