package com.zimmy.splitmoney.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants

class GroupActivity : AppCompatActivity() {

    lateinit var settleUp: Button
    lateinit var balances: Button
    lateinit var addGroupMember: LinearLayout
    lateinit var shareGroupLink: LinearLayout
    lateinit var addExpense: FloatingActionButton
    lateinit var expenseRecyclerView: RecyclerView
    lateinit var aloneTv: TextView
    lateinit var groupQrTv: TextView

    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference

    lateinit var groupCode: String
    var totalMembers: Int = 0

    val TAG = GroupActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        settleUp = findViewById(R.id.settleUp)
        balances = findViewById(R.id.balances)
        addGroupMember = findViewById(R.id.addMemberLl)
        shareGroupLink = findViewById(R.id.groupInviteLl)
        addExpense = findViewById(R.id.addExpense)
        expenseRecyclerView = findViewById(R.id.groupExpenseRv)
        aloneTv = findViewById(R.id.aloneTv)
        groupQrTv = findViewById(R.id.groupQrTv)

        //complete dependency is on this gcode so intent it properly
        groupCode = intent.getStringExtra("gcode").toString()

        groupQrTv.setOnClickListener {
            val intent = Intent(this@GroupActivity, QrActivity::class.java)
            intent.putExtra("gcode",groupCode)
            startActivity(intent)
        }

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        groupReference = firebaseDatabase.reference.child(Konstants.GROUPS)

        groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.TOTALMEMBERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalMembers = snapshot.getValue(Int::class.java)!!
                    if (totalMembers == 1) {
                        showEmptyGroup()
                    } else {
                        showFilledGroup()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error " + error.message)
                }

            })

        addGroupMember.setOnClickListener {

        }

        shareGroupLink.setOnClickListener {

        }

        addExpense.setOnClickListener {
            Toast.makeText(this@GroupActivity, "coming soon", Toast.LENGTH_SHORT).show()
        }

    }

    fun showEmptyGroup() {
        expenseRecyclerView.visibility = View.GONE
        addGroupMember.visibility = View.VISIBLE
        shareGroupLink.visibility = View.VISIBLE
        aloneTv.visibility = View.VISIBLE
    }

    fun showFilledGroup() {
        addGroupMember.visibility = View.GONE
        shareGroupLink.visibility = View.GONE
        aloneTv.visibility = View.GONE
        expenseRecyclerView.visibility = View.VISIBLE
    }
}