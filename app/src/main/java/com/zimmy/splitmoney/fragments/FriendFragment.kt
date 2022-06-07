package com.zimmy.splitmoney.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.New.NewFriendActivity
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentFriendBinding
import com.zimmy.splitmoney.models.Friend

class FriendFragment : Fragment() {

    private var _friendBinding: FragmentFriendBinding? = null;
    private val friendBinding get() = _friendBinding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private lateinit var myPhoneNumber: String

    private lateinit var personalPreferences: SharedPreferences
    private val TAG = FriendFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        personalPreferences =
            requireContext().getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)

        myPhoneNumber = personalPreferences.getString(Konstants.PHONE, "6352938170").toString()

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        userReference = firebaseDatabase.reference.child(Konstants.USERS)
        Log.v(TAG, "friend here "+myPhoneNumber)

        userReference.child(myPhoneNumber)
            .child(Konstants.FRIENDS).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val phoneNumber = snapshot1.key.toString()
                        Log.v(TAG, "friend here too $phoneNumber")
                        userReference.child(myPhoneNumber).child(Konstants.FRIENDS).child(phoneNumber)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friend: Friend = snapshot.getValue(Friend::class.java)!!
                                    Log.v(TAG, "friend: - " + friend.name)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error here" + error.message)
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "some database error:- " + error.message)
                }

            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _friendBinding = FragmentFriendBinding.inflate(inflater, container, false)
        val root = friendBinding.root
        return root
    }

    companion object {
        fun addNewFriend(context: Context) {
            val intent = Intent(context, NewFriendActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "friend added", Toast.LENGTH_SHORT).show()
        }
    }
}