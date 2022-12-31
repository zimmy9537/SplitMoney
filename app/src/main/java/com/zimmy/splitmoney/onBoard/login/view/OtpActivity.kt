package com.zimmy.splitmoney.onBoard.login.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.zimmy.splitmoney.HomeActivity
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityOtpBinding
import com.zimmy.splitmoney.onBoard.login.viewmodel.LoginViewModel
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.log


@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding

    @Inject
    lateinit var mAuth: FirebaseAuth
    private lateinit var phoneNumber: String
    private val TAG = OtpActivity::class.java.simpleName
    private var verificationId: String? = null
    private val viewModel: LoginViewModel by viewModels()


    private val mCallbacks = object : OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "Something went wrong")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@OtpActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "e-> ${e.message}")
            binding.progress.visibility = View.GONE
        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            Log.d(TAG, "verification Id $verificationId")
            this@OtpActivity.verificationId = verificationId
            binding.progress.visibility = View.GONE
            binding.resend.visibility = View.VISIBLE
            Toast.makeText(this@OtpActivity, "An Otp has been Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private val databaseOperationObserve = Observer<ResultData<Boolean?>> { resultData ->
        when (resultData) {
            ResultData.Loading() -> {
                binding.progress.visibility = View.VISIBLE
                Toast.makeText(this, "Catching you in...", Toast.LENGTH_SHORT).show()
            }

            ResultData.Success(true) -> {
                Log.d(TAG, "going home")
                binding.progress.visibility = View.GONE
                val firebaseUser = FirebaseAuth.getInstance().currentUser!!
//                firebaseUser.delete().addOnCompleteListener {
                val intent = Intent(this@OtpActivity, HomeActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
//                }
            }

            else -> {
                binding.progress.visibility = View.GONE
                Log.d(TAG, "TRY AGAIN LATER")
            }
        }
    }

    //timer
    private var timerFree = false
    private var timer = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //intent
        phoneNumber = intent.getStringExtra(Konstants.PHONE).toString()
        phoneNumber = "+91$phoneNumber"

        sendOtp(mCallbacks)
        timerFunc()

        binding.resend.setOnClickListener {
            if (!timerFree) {
                Toast.makeText(
                    this@OtpActivity,
                    "try resend after $timer seconds",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            sendOtp(mCallbacks)
            timerFunc()
            verificationId = null
        }


        //currently bypassing the otp check due to no free providers, otp check code in previous commit
        binding.verifyOtpBt.setOnClickListener {
            if (verificationId == null) {
                Toast.makeText(
                    this@OtpActivity,
                    "Please wait for the otp to be sent",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val otp = binding.otpEt.text.toString()
            if (otp.length < 6) {
                Toast.makeText(this@OtpActivity, "Otp must be of 6 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.insertLiveData.observe(this, databaseOperationObserve)
            GlobalScope.launch {
                viewModel.insertDatabaseOperation(
                    mAuth,
                    phoneNumber,
                    getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
                )
            }
        }
    }

    private fun sendOtp(mCallbacks: OnVerificationStateChangedCallbacks) {
        binding.progress.visibility = View.VISIBLE
        binding.resend.visibility = View.GONE
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(20L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun timerFunc() {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                if (timer > 0) {
                    timerFree = false
                    timer--
                    handler.postDelayed(this, 1000)
                } else {
                    //timer over
                    timer = 60
                    timerFree = true
                }
            }
        })
    }
}