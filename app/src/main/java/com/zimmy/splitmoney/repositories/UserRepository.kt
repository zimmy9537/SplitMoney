package com.zimmy.splitmoney.repositories

import android.util.Log
import com.google.firebase.database.*
import com.zimmy.splitmoney.constants.Konstants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

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
                Log.d(TAG,"await close")
            }
        }
    }
}