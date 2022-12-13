package com.zimmy.splitmoney.onBoard.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityPhoneNumberBinding
import com.zimmy.splitmoney.onBoard.login.viewmodel.LoginViewModel
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneNumberBinding
    private val TAG = PhoneNumberActivity::class.java.simpleName
    private val loginViewModel: LoginViewModel by viewModels()
    private val userRepoObserve = Observer<ResultData<Boolean?>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                Log.d(TAG, "Loading state")
                binding.progress.visibility = View.VISIBLE
            }
            is ResultData.Success -> {
                binding.progress.visibility = View.GONE
                val userExist = resultData.data
                if (userExist == null) {
                    Log.d(TAG, "Failure call 2.0(null)")
                    Toast.makeText(this, "Please Try Again!!", Toast.LENGTH_SHORT).show()
                } else if (userExist) {
                    Toast.makeText(
                        this,
                        "User Already exist, try some other number",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Success call")
                    //todo send the user to the google sign in screen
                    val intent = Intent(this@PhoneNumberActivity, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Log.d(TAG, "Success call")
                    val intent = Intent(this, OtpActivity::class.java)
                    var phoneNumber = binding.phoneEt.text.toString()
                    phoneNumber = "+91$phoneNumber"
                    intent.putExtra(Konstants.PHONE, phoneNumber)
                    startActivity(intent)
                }
                binding.phoneEt.isEnabled = true
            }
            else -> {
                binding.progress.visibility = View.GONE
                Log.d(TAG, "Failure call")
                Toast.makeText(this, "Please Try Again!!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        binding.sendBt.setOnClickListener {
            Log.d(TAG, "CLicked")
            val phoneNumber = binding.phoneEt.text.toString().trim()
            if (phoneNumber.length != 10) {
                Toast.makeText(this, "Enter a valid Number", Toast.LENGTH_SHORT).show()
                binding.phoneEt.setText("")
                return@setOnClickListener
            }
            binding.phoneEt.isEnabled = false
            checkExist(phoneNumber)
        }
    }

    private fun setObserver() {
        loginViewModel.userExistLiveData.observe(this, userRepoObserve)
    }

    private fun checkExist(phoneNumber: String) {
        GlobalScope.launch {
            loginViewModel.getUserStatus(phoneNumber)
        }
    }
}