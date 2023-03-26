package com.zimmy.splitmoney.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.settleup.balance.BalanceActivity
import com.zimmy.splitmoney.New.NewExpenseActivity
import com.zimmy.splitmoney.New.NewFriendActivity
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.adapters.GroupExpenseAdapter
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.settleup.beforesettleup.BeforeSettleUpActivity
import com.zimmy.splitmoney.models.ExpenseGroup
import com.zimmy.splitmoney.models.Friend

class GroupActivity : AppCompatActivity() {

    lateinit var settleUp: Button
    lateinit var balances: Button
    lateinit var addGroupMember: LinearLayout
    lateinit var shareGroupQr: LinearLayout
    lateinit var addExpense: FloatingActionButton
    lateinit var expenseRecyclerView: RecyclerView
    lateinit var aloneTv: TextView
    lateinit var groupQrTv: TextView
    lateinit var groupName: TextView
    lateinit var addMember: ImageView

    private lateinit var mAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference

    lateinit var groupNameString: String
    lateinit var groupCode: String
    var totalMembers: Int = 0
    lateinit var friendArrayList: ArrayList<Friend>
    lateinit var expenseList: ArrayList<ExpenseGroup>

    val TAG = GroupActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        settleUp = findViewById(R.id.settleUp)
        balances = findViewById(R.id.balances_rv)
        addGroupMember = findViewById(R.id.addMemberLl)
        shareGroupQr = findViewById(R.id.groupInviteLl)
        addExpense = findViewById(R.id.addExpense)
        expenseRecyclerView = findViewById(R.id.groupExpenseRv)
        aloneTv = findViewById(R.id.aloneTv)
        groupQrTv = findViewById(R.id.groupQrTv)
        groupName = findViewById(R.id.groupName)
        addMember = findViewById(R.id.addMember)

        //complete dependency is on this gcode so intent it properly
        groupCode = intent.getStringExtra("gcode").toString()

        friendArrayList = ArrayList()
        expenseList = ArrayList()

        groupQrTv.setOnClickListener {
            val intent = Intent(this@GroupActivity, QrActivity::class.java)
            intent.putExtra("gcode", groupCode)
            startActivity(intent)
        }

        balances.setOnClickListener {
            val intent = Intent(this@GroupActivity, BalanceActivity::class.java)
            intent.putExtra(Konstants.FRIENDS, false)
            intent.putExtra(Konstants.GROUP_CODE, groupCode)
            startActivity(intent)
        }

        settleUp.setOnClickListener {
            //todo change it to before settle up activity
            val intent = Intent(this@GroupActivity, BeforeSettleUpActivity::class.java)
            intent.putExtra(Konstants.FRIENDS, false)
            intent.putExtra(Konstants.GROUP_CODE, groupCode)
            startActivity(intent)
        }

        addMember.setOnClickListener {
            val intent = Intent(this, NewFriendActivity::class.java)
            intent.putExtra("group", true)
            intent.putExtra("name", groupNameString)
            intent.putExtra("code", groupCode)
            startActivity(intent)
        }

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        groupReference = firebaseDatabase.reference.child(Konstants.GROUPS)

        //set group name
        groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.GROUPNAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupName.text = snapshot.getValue(String::class.java)
                    groupNameString = snapshot.getValue(String::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error ${error.message}")
                }

            })

        //check for group empty
        groupReference.child(groupCode).child(Konstants.GROUPINFO).child(Konstants.TOTALMEMBERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalMembers = snapshot.getValue(Int::class.java)!!
                    if (totalMembers == 1) {
                        showEmptyGroup()
                    } else {
                        loadExpenses()
                        showFilledGroup()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error " + error.message)
                }

            })


        groupReference.child(groupCode).child(Konstants.MEMBERS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val phone = snapshot1.key
                        groupReference.child(groupCode).child(Konstants.MEMBERS).child(phone!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val friend = snapshot.getValue(Friend::class.java)
                                    Log.v(TAG, "friend ${friend!!.name}")
                                    friend.phone = phone
                                    friendArrayList.add(friend)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.v(TAG, "database error ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error ${error.message}")
                }

            })

        addGroupMember.setOnClickListener {
            val intent = Intent(this, NewFriendActivity::class.java)
            intent.putExtra("group", true)
            intent.putExtra("name", groupNameString)
            intent.putExtra("code", groupCode)
            startActivity(intent)
        }

        shareGroupQr.setOnClickListener {
            //todo work remaining here
        }

        addExpense.setOnClickListener {
            val intent = Intent(this@GroupActivity, NewExpenseActivity::class.java)
            intent.putExtra(Konstants.EXPENSE, Konstants.GROUPEXPENSE)
            intent.putExtra(Konstants.DATA, friendArrayList)
            intent.putExtra(Konstants.GROUPS, groupCode)
            startActivity(intent)
        }

    }

    fun loadExpenses() {
        expenseRecyclerView.adapter =
            GroupExpenseAdapter(expenseList, this@GroupActivity)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this@GroupActivity)
        groupReference.child(groupCode).child(Konstants.EXPENSE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (code in snapshot.children) {
                        val expenseCode = code.key
                        if (expenseCode != null) {
                            groupReference.child(groupCode).child(Konstants.EXPENSE)
                                .child(expenseCode)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        expenseList.add(snapshot.getValue(ExpenseGroup::class.java)!!)
                                        (expenseRecyclerView.adapter as GroupExpenseAdapter).notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun showEmptyGroup() {
        expenseRecyclerView.visibility = View.GONE
        addGroupMember.visibility = View.VISIBLE
        shareGroupQr.visibility = View.VISIBLE
        aloneTv.visibility = View.VISIBLE
    }

    fun showFilledGroup() {
        addGroupMember.visibility = View.GONE
        shareGroupQr.visibility = View.GONE
        aloneTv.visibility = View.GONE
        expenseRecyclerView.visibility = View.VISIBLE
    }
}