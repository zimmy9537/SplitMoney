package com.zimmy.splitmoney.repositories

import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.User
import com.zimmy.splitmoney.resultdata.ResultData
import com.zimmy.splitmoney.utils.GroupUtills
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository {
    private val TAG = UserRepository::class.java.simpleName

    fun findUser(phoneNumber: String): Flow<Boolean?> {
        return callbackFlow {
            val accountReference: DatabaseReference =
                FirebaseDatabase.getInstance().reference.child(Konstants.USERS)

            accountReference.child(phoneNumber)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            trySendBlocking(true).onFailure {
                                Log.d(TAG, "some error in onFailure")
                            }
                        } else {
                            trySend(false).onFailure {
                                Log.d(TAG, "some error in onFailure2.0")
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Database error")
                        trySend(null).onFailure {
                            Log.d(TAG, "some error in onFailure3.0")
                        }
                    }
                })

            awaitClose {
                Log.d(TAG, "await close")
            }
        }
    }


    fun authenticateWithGoogle(
        idToken: String,
        mAuth: FirebaseAuth,
        context: Activity,
        editor: Editor
    ): Flow<ResultData<Boolean>?> {
        return callbackFlow {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            mAuth.signInWithCredential(credential).addOnCompleteListener(context) { task ->
                if (task.isSuccessful) {
                    val account = GoogleSignIn.getLastSignedInAccount(context.applicationContext)
                    if (account != null) {
                        val firebaseDatabase = FirebaseDatabase.getInstance()
                        val accountReference = firebaseDatabase.reference.child(Konstants.USERS)

                        var firstTime: Boolean

                        firebaseDatabase.reference.child(Konstants.UIDS).child(mAuth.uid.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    firstTime = !snapshot.exists()
                                    if (firstTime) {

                                        trySendBlocking(ResultData.Success(true)).onFailure {
                                            Log.d(TAG, "Failure in first try")
                                        }

                                    } else {
                                        var user: User
                                        val phone: String = snapshot.getValue(String::class.java)!!

                                        accountReference.child(phone).child(Konstants.DATA)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    user = snapshot.getValue(User::class.java)!!
                                                    setPreferenceOnVerification(user, editor)
                                                    trySendBlocking(ResultData.Success(false)).onFailure {
                                                        Log.d(TAG, "Failure in 2nd try ")
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.v(TAG, "database here " + error.message)
                                                }

                                            })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                }

                            })
                    }
                } else {
                    trySendBlocking(ResultData.Failed("Account null")).onFailure {
                        Log.d(TAG, "Failure in null outcome")
                    }
                }
            }
            awaitClose { Log.d(TAG, "await close") }
        }
    }

    fun setPreferenceOnVerification(user: User, editor: Editor) {
        Log.d(
            TAG,
            "name-> ${user.name}, phone->${user.phoneNumber}, promo->${user.promocode}, email->${user.email}"
        )
        editor.putString(
            Konstants.PHONE,
            user.phoneNumber
        )
        editor.putString(Konstants.NAME, user.name)
        editor.putString(
            Konstants.PROMO,
            user.promocode
        )
        editor.putString(Konstants.EMAIL, user.email)
        editor.apply()
    }

    fun insertDatabaseOperation(
        mAuth: FirebaseAuth,
        phoneNumber: String,
        sharedPreferences: SharedPreferences
    ): Flow<ResultData<Boolean>> {
        return callbackFlow {

            val userReference = FirebaseDatabase.getInstance().reference
            userReference.child(Konstants.UIDS).child(mAuth.uid.toString()).setValue(phoneNumber)

            userReference.child(Konstants.GENERAL)
            val promocode = GroupUtills.createGroupCode(mAuth.uid.toString(), 5)

            val editor = sharedPreferences.edit()


            var userCount: Int
            userReference.child(Konstants.GENERAL).child(Konstants.USERCOUNT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userCount = snapshot.getValue(Int::class.java)!!
                        userCount += 1
                        userReference.child(Konstants.GENERAL).child(Konstants.USERCOUNT)
                            .setValue(userCount)
                        val user = User(
                            sharedPreferences.getString(Konstants.NAME, "zimmy")!!,
                            sharedPreferences.getString(Konstants.EMAIL, "noEmail"),
                            promocode,
                            phoneNumber,
                        )
                        editor.putString(Konstants.NAME, user.name)
                        editor.putString(Konstants.EMAIL, user.email)
                        editor.putString(Konstants.PHONE, user.phoneNumber)
                        editor.putString(Konstants.PROMO, user.promocode)
                        editor.apply()
                        userReference.child(Konstants.USERS).child(phoneNumber)
                            .child(Konstants.DATA)
                            .setValue(user)
                            .addOnCompleteListener {
                                Log.d(TAG, "Success call")
                                trySendBlocking(ResultData.Success(true)).onFailure {
                                    Log.d(TAG, "Something went wrong")
                                    trySendBlocking(ResultData.Failed())
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Database error occurred here")
                        trySendBlocking(ResultData.Failed(null, "database error")).onFailure {
                            Log.d(TAG, "something went wrong")
                            Log.d(TAG, "Failure call")
                        }
                    }
                })
            awaitClose { Log.d(TAG, "await close") }
        }
    }

}