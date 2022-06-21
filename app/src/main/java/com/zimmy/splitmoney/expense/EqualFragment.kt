package com.zimmy.splitmoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TableRow
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.databinding.FragmentEqualBinding


class EqualFragment : Fragment() {
    private var _equalBinding: FragmentEqualBinding? = null
    private val equalBinding get() = _equalBinding!!

    private var count: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        for (i in 0 until count) {
//            val row = TableRow(context)
//            row.id = i
//            row.layoutParams = LinearLayoutCompat.LayoutParams(
//                Li nearLayoutCompat.LayoutParams.FILL_PARENT,
//                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
//            )
//            val checkBox = CheckBox(context)
//            checkBox.setOnCheckedChangeListener(context)
//            checkBox.id = i
//            checkBox.setText(Str_Array.get(i))
//            row.addView(checkBox)
//            my_layout.addView(row)
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _equalBinding = FragmentEqualBinding.inflate(inflater, container, false)
        val root = equalBinding.root
        return root
    }
}