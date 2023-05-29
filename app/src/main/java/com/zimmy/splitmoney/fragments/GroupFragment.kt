package com.zimmy.splitmoney.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.New.NewGroupActivity
import com.zimmy.splitmoney.adapters.GroupAdapter
import com.zimmy.splitmoney.appreference.AppPreference
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentGroupBinding
import com.zimmy.splitmoney.models.Group

class GroupFragment : Fragment() {

    private var _groupBinding: FragmentGroupBinding? = null;
    private val groupBinding get() = _groupBinding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var groupReference: DatabaseReference
    private lateinit var userReference: DatabaseReference

    private lateinit var myPhoneNumber: String
    private lateinit var myGroups: ArrayList<Group>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
        userReference = FirebaseDatabase.getInstance().reference.child(Konstants.USERS)

        val appPreference = context?.let { AppPreference(it) }
        myPhoneNumber = appPreference?.getString(Konstants.PHONE, "6352938170").toString()

        myGroups = ArrayList()

        userReference.child(myPhoneNumber).child(Konstants.GROUPS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snapshot1 in snapshot.children) {
                        val gcode = snapshot1.key
                        if (gcode != null) {
                            userReference.child(myPhoneNumber).child(Konstants.GROUPS).child(gcode)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        snapshot.getValue(Group::class.java)
                                            ?.let { myGroups.add(it) }
                                        groupBinding.groupRv.adapter?.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            context,
                                            "database error ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        }
                        groupBinding.groupRv.adapter = context?.let { GroupAdapter(myGroups, it) }
                        groupBinding.groupRv.layoutManager = LinearLayoutManager(context)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "database error ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _groupBinding = FragmentGroupBinding.inflate(inflater, container, false)
        val root = groupBinding.root

        return root
    }

    companion object {
        fun addNewGroup(context: Context) {
            Toast.makeText(context, "Group added", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, NewGroupActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}