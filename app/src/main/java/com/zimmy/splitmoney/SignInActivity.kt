package com.zimmy.splitmoney

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.User


class SignInActivity : AppCompatActivity() {

    val GOOGLE_SIGN_IN = 64
    val TAG = SignInActivity::class.simpleName
    lateinit var mAuth: FirebaseAuth
    lateinit var signInButton: SignInButton
    lateinit var gso: GoogleSignInOptions
    lateinit var gsc: GoogleSignInClient
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var accountReference: DatabaseReference
    lateinit var generalReference: DatabaseReference
    lateinit var phoneEt: EditText
    lateinit var maleRadio: RadioButton
    lateinit var femaleRadio: RadioButton
    lateinit var personalPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var isFemale: Boolean = true

    val WEB_CLIENT_ID =
        "787696245563-4s9lsmhv9292p4h6divbprhjffkecfjj.apps.googleusercontent.com"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        personalPreference = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        editor = personalPreference.edit()

        mAuth = FirebaseAuth.getInstance()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso)

        signInButton = findViewById(R.id.signInBt)
        phoneEt = findViewById(R.id.phNumEt)
        maleRadio = findViewById(R.id.male)
        femaleRadio = findViewById(R.id.female)

        signInButton.setOnClickListener {
            if (!checkPhoneNumber()) {
                return@setOnClickListener
            }
            signIn()
        }

        phoneEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (phoneEt.text.length >= 10) {
                    hideKeyboard(this@SignInActivity as Activity)
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (phoneEt.text.length == 10) {
                    hideKeyboard(this@SignInActivity as Activity)
                }
            }

        })
    }

    fun checkPhoneNumber(): Boolean {
        if (phoneEt.text.toString().length != 10) {
            Toast.makeText(baseContext, "Enter a valid phoneNumber", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            95
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    private fun signIn() {
        val intent = gsc.signInIntent
        startActivityForResult(intent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(baseContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
                if (account != null) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    //database operation
                    databaseOperation(account)
                }
            } else {
                Toast.makeText(baseContext, "Error!" + task.exception!!.message, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun databaseOperation(account: GoogleSignInAccount) {
        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        accountReference = firebaseDatabase.reference.child(Konstants.USERS)
        var firstTime: Boolean

        accountReference.child(phoneEt.text.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    firstTime = !snapshot.exists()
                    if (firstTime) {
                        databaseInsertOperation(account)
                        Toast.makeText(
                            baseContext,
                            "Hello, " + account.displayName,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        var user:User
                        accountReference.child(phoneEt.text.toString()).addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                user= snapshot.getValue(User::class.java)!!
                                Log.v(TAG,"here user "+user.name+", "+user.phoneNumber)
                                editor.putString(Konstants.PHONE,user.phoneNumber)
                                editor.putString(Konstants.NAME,user.name)
                                editor.putString(Konstants.PROMO,user.promocode)
                                user.isFemale?.let { editor.putBoolean(Konstants.PROMO, it) }
                                editor.apply()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.v(TAG,"database here "+error.message)
                            }

                        })

                        Toast.makeText(baseContext, "welcome back", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun databaseInsertOperation(account: GoogleSignInAccount) {
        generalReference = firebaseDatabase.reference.child(Konstants.GENERAL)
        var promocode: String
        promocode = if (mAuth.toString().length < 5) {
            account.email!!
        } else {
            account.email?.substring(0, 5)!!
        }

        isFemale = !maleRadio.isChecked

        var userCount: Int
        generalReference.child(Konstants.USERCOUNT)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userCount = snapshot.getValue(Int::class.java)!!
                    userCount += 1
                    promocode = promocode.plus(userCount.toString())
                    generalReference.child(Konstants.USERCOUNT).setValue(userCount)
                    val user = User(
                        account.displayName!!,
                        account.email!!,
                        promocode,
                        phoneEt.text.toString(),
                        isFemale
                    )
                    editor.putString(Konstants.NAME, user.name)
                    editor.putString(Konstants.PHONE, user.phoneNumber)
                    editor.putString(Konstants.PROMO, user.promocode)
                    user.isFemale?.let { editor.putBoolean(Konstants.FEMALE, it) }
                    editor.apply()
                    accountReference.child(phoneEt.text.toString()).child(Konstants.DATA)
                        .setValue(user)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}