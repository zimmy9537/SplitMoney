package com.zimmy.splitmoney.New

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.groups.GroupActivity
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.utills.GroupUtils

class NewGroupActivity : AppCompatActivity() {

    lateinit var create: Button
    lateinit var cancel: Button
    lateinit var nameEt: EditText
    lateinit var simplifyDebts: CheckBox

    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference
    lateinit var generalReference: DatabaseReference

    private var groupCount: Int = 0
    lateinit var groupCode: String
    val TAG = NewGroupActivity::class.java.simpleName
    lateinit var personalPreference: SharedPreferences
    lateinit var name: String
    var isFemale: Boolean = true
    lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)

        //TODO image part work is left

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        groupReference = firebaseDatabase.reference.child(Konstants.GROUPS)
        generalReference = firebaseDatabase.reference.child(Konstants.GENERAL)

        personalPreference = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        name = personalPreference.getString(Konstants.NAME, "Zimmy").toString()
        isFemale = personalPreference.getBoolean(Konstants.FEMALE, true)
        phone = personalPreference.getString(Konstants.PHONE, "9537830943").toString()

        generalReference.child(Konstants.GROUPCOUNT)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupCount = snapshot.getValue(Int::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error " + error.message)
                }

            })


        groupCode = GroupUtils.createGroupCode(mAuth.uid.toString(), groupCount + 1)



        create = findViewById(R.id.createGroup)
        cancel = findViewById(R.id.cancelGroup)
        nameEt = findViewById(R.id.nameEt)
        simplifyDebts = findViewById(R.id.simplifyCheck)

        create.setOnClickListener {
            if (nameEt.text.isEmpty()) {
                Toast.makeText(baseContext, "Enter the name of the group", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            groupCount++
            generalReference.child(Konstants.GROUPCOUNT).setValue(groupCount)

            val friend = Friend(name, isFemale)
            groupReference.child(groupCode).child(Konstants.MEMBERS).child(phone).setValue(friend)
            groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.GROUPNAME)
                .setValue(nameEt.text.toString())
            groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.TOTALMEMBERS)
                .setValue(1)
            groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.GROUPLEADER)
                .setValue(phone)
            val intent = Intent(this@NewGroupActivity, GroupActivity::class.java)
            intent.putExtra("gcode", groupCode)
            startActivity(intent)
            finish()
        }

        cancel.setOnClickListener {
            finish()
        }

        simplifyDebts.setOnClickListener {
            Toast.makeText(baseContext, "This feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}