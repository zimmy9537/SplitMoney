package com.zimmy.splitmoney.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.New.NewFriendActivity
import com.zimmy.splitmoney.New.NewGroupActivity
import com.zimmy.splitmoney.databinding.FragmentGroupBinding
import com.zimmy.splitmoney.models.FriendOrTrip

class GroupFragment : Fragment() {

    private var _groupBinding: FragmentGroupBinding? = null;
    private val groupBinding get() = _groupBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _groupBinding = FragmentGroupBinding.inflate(inflater, container, false)
        val root = groupBinding.root

        return root
    }

    companion object{
        fun addNewGroup(context: Context){
            Toast.makeText(context,"Group added",Toast.LENGTH_SHORT).show()
            val intent = Intent(context, NewGroupActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}