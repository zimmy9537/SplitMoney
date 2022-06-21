package com.zimmy.splitmoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.databinding.FragmentPercentageBinding

class PercentageFragment : Fragment() {

    private var _percentageBinding: FragmentPercentageBinding? = null
    private val percentageBinding get() = _percentageBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _percentageBinding = FragmentPercentageBinding.inflate(inflater, container, false)
        val root = percentageBinding.root
        return root
    }

}