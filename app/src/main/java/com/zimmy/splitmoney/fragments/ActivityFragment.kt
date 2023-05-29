package com.zimmy.splitmoney.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.zimmy.splitmoney.adapters.ActivityAdapter
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentActivityBinding
import com.zimmy.splitmoney.models.Activity

class ActivityFragment : Fragment() {

    private var _activityBinding: FragmentActivityBinding? = null
    private val activityBinding get() = _activityBinding!!
    private var container: ViewGroup? = null
    private val TAG = ActivityFragment::class.java.simpleName

    lateinit var activityList: ArrayList<Activity>

    private lateinit var activityReference: DatabaseReference
    private lateinit var myPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityList=ArrayList()
        myPhone = context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            ?.getString(Konstants.PHONE, "not working")
            .toString()

        activityReference =
            FirebaseDatabase.getInstance().reference.child(Konstants.USERS).child(myPhone)
                .child(Konstants.ACTIVITES)
        activityReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (activity in snapshot.children) {
                    Log.v(TAG, "activity:- "+activity.getValue(String::class.java).toString())
                    activity.getValue(Activity::class.java)?.let { activityList.add(it) }
                }
                if (activityList.isNotEmpty()) {
                    activityBinding.activityTv.visibility = View.GONE
                    activityBinding.activityRv.visibility = View.VISIBLE

                    //proceed filling rv
                    val linearLayoutManager = LinearLayoutManager(context)
                    val activityAdapter = context?.let { ActivityAdapter(activityList, it) }
                    activityBinding.activityRv.layoutManager = linearLayoutManager
                    activityBinding.activityRv.adapter = activityAdapter

                } else {
                    activityBinding.activityTv.visibility = View.VISIBLE
                    activityBinding.activityRv.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v(TAG, "Database error happened")
            }

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        _activityBinding = FragmentActivityBinding.inflate(inflater, container, false)
        val root = activityBinding.root

        return root
    }

    companion object {
        fun refreshActivities(context: Context) {
            Toast.makeText(context, "activity refreshed", Toast.LENGTH_SHORT).show()
        }
    }
}