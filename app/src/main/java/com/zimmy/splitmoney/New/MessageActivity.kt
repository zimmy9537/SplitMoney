package com.zimmy.splitmoney.New

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.ContactModel
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Group
import com.zimmy.splitmoney.models.User


class MessageActivity : AppCompatActivity() {

    lateinit var send: Button
    lateinit var personalPreference: SharedPreferences
    lateinit var name: String
    lateinit var phone: String
    lateinit var contact: ContactModel
    var group: Boolean = false
    var groupName: String = "TestGroup"
    var groupCode: String = "ABCD"

    var TAG = MessageActivity::class.java.simpleName

    lateinit var mAuth: FirebaseAuth
    lateinit var userDatabase: FirebaseDatabase
    lateinit var userReference: DatabaseReference
    lateinit var groupReference: DatabaseReference

    //todo sending message is creating duplicate users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        group = intent.getBooleanExtra("group", false)
        if (group) {
            groupName = intent.getStringExtra("name").toString()
            groupCode = intent.getStringExtra("code").toString()
        }

        contact = intent.getSerializableExtra("contact") as ContactModel
        contact.phone = contact.phone.replace("\\s".toRegex(), "")
        //todo make changes in the phone number by trimming the space between the digits
        personalPreference = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        name = personalPreference.getString(Konstants.NAME, "Fena").toString()
        phone = personalPreference.getString(Konstants.PHONE, "9537830943").toString()
        send = findViewById(R.id.send)
        send.setOnClickListener {
            val message: String = if (!group) {
                "Hi! ${contact.name} please add me as a friend on SplitMoney app!!\nYour Friend, $name"
            } else {
                "Hi ${contact.name} you are added to a new group $groupName. Happy expensing"
            }
            sendSMS(message)
            if (group) {
                groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
                groupReference.child(groupCode).child(Konstants.GROUPINFO)
                    .child(Konstants.TOTALMEMBERS)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var members = snapshot.getValue(Int::class.java)!!
                            members++
                            groupReference.child(groupCode).child(Konstants.GROUPINFO)
                                .child(Konstants.TOTALMEMBERS).setValue(members)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.v(TAG, "database error ${error.message}")
                        }
                    })
                val friend = Friend(contact.name, null, null, null)
                groupReference.child(groupCode).child(Konstants.MEMBERS).child(contact.phone)
                    .setValue(friend)

                groupReference.child(groupCode).child(Konstants.EXPENSE_GLOBAL).child(contact.phone)
                    .setValue(0.00)

                groupReference.child(groupCode).child(Konstants.GROUPINFO)
                    .child(Konstants.GROUPNAME)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val groupName: String? = snapshot.getValue(String::class.java)
                            val group = groupName?.let {
                                Group(
                                    "null",
                                    groupCode,
                                    it,
                                    "you owe no one",
                                    0.00
                                )
                            }
                            userReference.child(contact.phone).child(Konstants.GROUPS)
                                .child(groupCode).setValue(group)
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.v(TAG, "database error " + error.message)
                        }

                    })
            } else {
                addFriend()
            }
        }

        mAuth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance()
        userReference = userDatabase.reference.child(Konstants.USERS)
    }

    private fun addFriend() {
        userReference.child(contact.phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val user = User(contact.name, null, null, contact.phone, null)
                        userReference.child(contact.phone).setValue(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(
                        MessageActivity::class.java.simpleName,
                        "some error here, " + error.message
                    )
                }
            })

        val friend = Friend(contact.name, null, contact.phone, 0.0)
        //this one is for me
        userReference.child(phone).child(Konstants.FRIENDS).child(contact.phone)
            .child(Konstants.DATA).setValue(friend)
        userReference.child(phone).child(Konstants.FRIENDS).child(contact.phone)
            .child(Konstants.RESULT).setValue(0.00)
        //this one is my friend
        userReference.child(contact.phone).child(Konstants.FRIENDS).child(phone)
            .child(Konstants.DATA).setValue(Friend(name, null, phone, 0.0))
        userReference.child(contact.phone).child(Konstants.FRIENDS).child(phone)
            .child(Konstants.RESULT).setValue(0.00)
    }

    private fun sendSMS(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            val defaultSmsPackageName =
                Telephony.Sms.getDefaultSmsPackage(this) // Need to change the build to API 19
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra("address", contact.phone)
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            if (defaultSmsPackageName != null) // Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName)
            }
            startActivity(sendIntent)
        } else  // For early versions, do what worked for you before.
        {
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.type = "vnd.android-dir/mms-sms"
            smsIntent.putExtra("address", "phoneNumber")
            smsIntent.putExtra("sms_body", "message")
            startActivity(smsIntent)
        }
    }
}