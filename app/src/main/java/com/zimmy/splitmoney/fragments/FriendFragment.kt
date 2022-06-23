package com.zimmy.splitmoney.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.New.NewFriendActivity
import com.zimmy.splitmoney.adapters.FriendOrTripAdapter
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentFriendBinding
import com.zimmy.splitmoney.models.Expense
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.FriendItem

class FriendFragment : Fragment() {

    private var _friendBinding: FragmentFriendBinding? = null;
    private val friendBinding get() = _friendBinding!!

    private lateinit var friendItemArray: ArrayList<FriendItem>
    private lateinit var friendArray: ArrayList<Friend>
    private lateinit var expenseArrayList: ArrayList<Expense>
    private val TAG = FriendFragment::class.java.simpleName

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private lateinit var expenseOutReference: DatabaseReference
    private lateinit var expenseInReference: DatabaseReference
    private lateinit var myPhoneNumber: String

    private lateinit var personalPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        personalPreferences =
            requireContext().getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)

        myPhoneNumber = personalPreferences.getString(Konstants.PHONE, "6352938170").toString()

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        userReference = firebaseDatabase.reference.child(Konstants.USERS)
        expenseOutReference = firebaseDatabase.reference.child(Konstants.USERS).child(myPhoneNumber)
            .child(Konstants.EXPENSEOUT)
        expenseInReference = firebaseDatabase.reference.child(Konstants.USERS).child(myPhoneNumber)
            .child(Konstants.EXPENSEIN)

        friendArray = ArrayList()
        friendItemArray = ArrayList()
        expenseArrayList = ArrayList()//todo this is useless here it seems

        userReference.child(myPhoneNumber)
            .child(Konstants.FRIENDS).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val phoneNumber = snapshot1.key.toString()
                        Log.v(TAG, "friend here too $phoneNumber")
                        userReference.child(myPhoneNumber).child(Konstants.FRIENDS)
                            .child(phoneNumber)
                            .child(Konstants.DATA)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friend: Friend = snapshot.getValue(Friend::class.java)!!
                                    Log.v(TAG, "friend: - " + friend.name)
                                    friendArray.add(friend)
                                    val oweString: String = if (friend.amount!! > 0) {
                                        "owes you"
                                    } else {
                                        "you owe"
                                    }
                                    Toast.makeText(context,"friend "+friend.name,Toast.LENGTH_SHORT).show()
                                    friendItemArray.add(
                                        FriendItem(
                                            "noImage",
                                            friend.name,
                                            oweString,
                                            friend.amount,
                                            friend.phone
                                        )
                                    )
                                    friendBinding.friendsRv.adapter?.notifyDataSetChanged()
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
        friendBinding.friendsRv.adapter =
            context?.let { FriendOrTripAdapter(friendItemArray, it)}
        friendBinding.friendsRv.layoutManager = LinearLayoutManager(context)
        val root = friendBinding.root

//        friendBinding.friendsRv.adapter = FriendOrTripAdapter(friendItemArray)

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