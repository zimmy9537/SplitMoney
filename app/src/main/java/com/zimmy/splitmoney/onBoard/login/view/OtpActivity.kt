package com.zimmy.splitmoney.onBoard.login.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zimmy.splitmoney.databinding.ActivityOtpBinding

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}