package com.zimmy.splitmoney.expense

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentEqualBinding
import com.zimmy.splitmoney.models.Friend


class EqualFragment : Fragment() {
    private var _equalBinding: FragmentEqualBinding? = null
    private val equalBinding get() = _equalBinding!!

    private var isFriend: Int = 0
    private lateinit var friendName: String
    private lateinit var friendPhone: String
    private var amount: Double = 0.00
    private lateinit var expenseEqual: HashMap<String, Boolean>
    private lateinit var phoneMap: HashMap<String, String>
    private lateinit var friendList: ArrayList<String>
    private var totalCheck: Int = 0
    private var totalMembers = 0
    private lateinit var friendDetailList: ArrayList<Friend>

    //todo mapping is done on the basis of names, hence update the mapping using the phone number
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFriend =
            requireActivity().intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        expenseEqual = HashMap()
        phoneMap = HashMap()
        friendList = ArrayList()

        amount = requireActivity().intent.getDoubleExtra(Konstants.AMOUNT, 0.00)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            val personalPreference =
                context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            friendName = requireActivity().intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = requireActivity().intent.getStringExtra(Konstants.PHONE).toString()
            Toast.makeText(context, "name $friendName", Toast.LENGTH_SHORT).show()
            phoneMap[friendName] = friendPhone
            phoneMap["Me"] = personalPreference?.getString(Konstants.PHONE, "9537830943").toString()

            expenseEqual[phoneMap["Me"]!!] = true
            expenseEqual[phoneMap[friendName]!!] = true
            totalCheck = 2
            totalMembers = 2
            friendList.add("Me")
            friendList.add(friendName);
        } else {//group
            friendDetailList =
                requireActivity().intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>

//            for (ele in friendDetailList) {
//                phoneMap[ele.name] = ele.phone!!
//                Toast.makeText(context, "name ${ele.name}", Toast.LENGTH_SHORT).show()
//            }

            for (ele in friendDetailList) {
                expenseEqual[phoneMap[ele.name]!!] = true
                friendList.add(ele.name)
            }
            totalCheck = friendDetailList.size
            totalMembers = friendDetailList.size
        }
    }

    private fun addFriends(linearLayout: LinearLayout, friendList: ArrayList<String>) {
        if (phoneMap == null) {
            Toast.makeText(context, "EXPENSE MAP IS NULL", Toast.LENGTH_SHORT).show()
        }
        for (friend in friendList) {
            val view = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val check = view.findViewById<CheckBox>(R.id.checkbox)
            linearLayout.addView(view)
            check.text = friend
            check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalCheck++
                    expenseEqual[phoneMap[friend]!!] = true
                } else {
                    totalCheck--
                    expenseEqual[phoneMap[friend]!!] = false
                }
                if (totalCheck > 0) {
//                    val contribution = amount / totalCheck
////                    Toast.makeText(context, "contribution each $contribution", Toast.LENGTH_SHORT)
////                        .show()
//                    for (ele in expenseMap) {
//                        if (expenseEqual[ele.key] == true) {
//                            expenseMap[ele.key] = contribution
//                        } else {
//                            expenseMap[ele.key] = 0.00
//                        }
//                    }
                } else {
                    check.isChecked = true
                    totalCheck++
                    expenseEqual[phoneMap[friend]!!] = true
//                    val contribution = amount / totalCheck
//                    Toast.makeText(context, "contribution each $contribution", Toast.LENGTH_SHORT)
//                        .show()
//                    for (ele in expenseMap) {
//                        if (expenseEqual[ele.key] == true) {
//                            expenseMap[ele.key] = contribution
//                        } else {
//                            expenseMap[ele.key] = 0.00
//                        }
//                    }
                    Toast.makeText(context, "atleast someone has to contribute", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _equalBinding = FragmentEqualBinding.inflate(inflater, container, false)
        val root = equalBinding.root

        addFriends(equalBinding.linearCheck, friendList)
        equalBinding.save.setOnClickListener {
            val intent = Intent()
            val expensePercent = HashMap<String, Double>()
            Toast.makeText(context, "total check $totalCheck", Toast.LENGTH_SHORT).show()
            for (ele in expenseEqual) {
                if (ele.value) {
                    expensePercent[ele.key] = 100.00 / totalCheck
                } else {
                    expensePercent[ele.key] = 0.00
                }
            }

            intent.putExtra(Konstants.EQUAL_PERCENT_MAP, expensePercent)
            intent.putExtra(Konstants.EXPENSE, isFriend)
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }

        return root
    }
}