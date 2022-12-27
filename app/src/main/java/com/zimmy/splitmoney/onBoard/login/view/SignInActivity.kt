package com.zimmy.splitmoney.onBoard.login.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.zimmy.splitmoney.HomeActivity
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityOtpBinding
import com.zimmy.splitmoney.databinding.ActivitySignInBinding
import com.zimmy.splitmoney.models.User
import com.zimmy.splitmoney.onBoard.login.viewmodel.LoginViewModel
import com.zimmy.splitmoney.resultdata.ResultData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    //TODO apply a check for unique phone number before the registration

    private lateinit var binding: ActivitySignInBinding
    private val GOOGLE_SIGN_IN = 64
    private val TAG = SignInActivity::class.simpleName
    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var mAuth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var accountReference: DatabaseReference
    private lateinit var generalReference: DatabaseReference
    private lateinit var personalPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var WEB_CLIENT_ID =
        "44259169007-rqadlqgrc4u4mbrtabd9b6gd4r3b56ql.apps.googleusercontent.com"

    private val userRepoObserve = Observer<ResultData<Boolean>> { resultData ->
        when (resultData) {

            is ResultData.Loading -> {
                binding.progress.visibility = View.VISIBLE
            }

            is ResultData.Success -> {
                binding.progress.visibility = View.GONE
                if (resultData.data == null) {
                    Toast.makeText(
                        this,
                        "Something Went wrong, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (resultData.data) {
                    //1st time
                    val intent = Intent(this, PhoneNumberActivity::class.java)
                    startActivity(intent)
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            }

            else -> {
                binding.progress.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Something Went wrong, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personalPreference = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        editor = personalPreference.edit()

        mAuth = FirebaseAuth.getInstance()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail().build()
        gsc = GoogleSignIn.getClient(this@SignInActivity, gso)

        //todo cheching for the existent number creates trouble in re sign in
        //particular code hence commented
        binding.signInBt.setOnClickListener {
            signIn()
        }
        loginViewModel.googleLiveData.observe(this, userRepoObserve)
    }

    //todo take this function to phone number activity
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun signIn() {
        val intent = gsc.signInIntent
        startActivityForResult(intent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                GlobalScope.launch {
                    loginViewModel.authenticateUserWithGoogle(
                        account.idToken!!,
                        mAuth,
                        this@SignInActivity,
                        editor
                    )
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(
                    this@SignInActivity,
                    "some fucking error, ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.v(TAG, "error stack ${e.stackTrace}")
            }
        }
    }
}