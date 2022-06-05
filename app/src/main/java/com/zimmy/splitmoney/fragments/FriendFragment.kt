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
import androidx.recyclerview.widget.RecyclerView
import com.zimmy.splitmoney.New.NewFriendActivity
import com.zimmy.splitmoney.databinding.FragmentFriendBinding

class FriendFragment : Fragment() {

    private var _friendBinding: FragmentFriendBinding? = null;
    private val friendBinding get() = _friendBinding!!

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
            val intent=Intent(context,NewFriendActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context,"friend added",Toast.LENGTH_SHORT).show()
        }
    }
}