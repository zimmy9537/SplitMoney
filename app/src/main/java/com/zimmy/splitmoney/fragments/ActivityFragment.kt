package com.zimmy.splitmoney.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.databinding.FragmentActivityBinding

class ActivityFragment : Fragment() {

    private var _activityBinding: FragmentActivityBinding? = null
    private val activityBinding get() = _activityBinding!!
    private var container:ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container=container
        _activityBinding = FragmentActivityBinding.inflate(inflater, container, false)
        val root = activityBinding.root

        return root
    }

    companion object{
        fun addNewActivity(context: Context){
            Toast.makeText(context,"activity added",Toast.LENGTH_SHORT).show()
        }
    }
}