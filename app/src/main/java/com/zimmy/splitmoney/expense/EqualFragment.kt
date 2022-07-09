package com.zimmy.splitmoney.expense

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var myName: String
    private var isFriend: Int = 0
    private lateinit var friendName: String
    private lateinit var friendPhone: String
    private var amount: Double = 0.00
    private lateinit var expenseEqual: HashMap<String, Boolean>
    private lateinit var checkMap: HashMap<CheckBox, String>
    private var totalCheck: Int = 0
    private var totalMembers = 0
    private lateinit var friendDetailList: ArrayList<Friend>
    private lateinit var myPhone: String

    //todo mapping is done on the basis of names, hence update the mapping using the phone number
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFriend =
            requireActivity().intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        expenseEqual = HashMap()
        checkMap = HashMap()

        myPhone = context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            ?.getString(Konstants.PHONE, "9537830943")
            .toString()
        Log.v("MY PHONE NUMBER IS ", " it  is ${myPhone}")

        amount = requireActivity().intent.getDoubleExtra(Konstants.AMOUNT, 0.00)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = requireActivity().intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = requireActivity().intent.getStringExtra(Konstants.PHONE).toString()
            Toast.makeText(context, "name $friendName", Toast.LENGTH_SHORT).show()

            expenseEqual[myPhone] = true
            expenseEqual[friendPhone] = true
            totalCheck = 2
            totalMembers = 2


        } else {//group
            friendDetailList =
                requireActivity().intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
            totalCheck = friendDetailList.size
            totalMembers = friendDetailList.size
        }
    }

    private fun addFriends(linearLayout: LinearLayout, friendDetailList: ArrayList<Friend>) {

        checkMap = HashMap()
        for (friend in friendDetailList) {
            val view = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val check = view.findViewById<CheckBox>(R.id.checkbox)
            linearLayout.addView(view)
            expenseEqual[friend.phone!!] = true
            if (friend.phone == myPhone) {
                check.text = "Me"
            } else {
                check.text = friend.name
            }
            checkMap[check] = friend.phone!!
            check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalCheck++
                    expenseEqual[checkMap[check]!!] = true
                } else {
                    totalCheck--
                    expenseEqual[checkMap[check]!!] = false
                }
                if (totalCheck <= 0) {
                    check.isChecked = true
                    totalCheck++
                    expenseEqual[checkMap[check]!!] = true
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

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            val view = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val checkMe = view.findViewById<CheckBox>(R.id.checkbox)
            checkMe.text = "Me"
            checkMap[checkMe] = myPhone
            equalBinding.linearCheck.addView(view)
            val view2 = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val checkFriend = view.findViewById<CheckBox>(R.id.checkbox)
            checkFriend.text = friendName
            checkMap[checkFriend] = friendPhone
            equalBinding.linearCheck.addView(view2)

            checkMe.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalCheck++
                    expenseEqual[checkMap[checkMe!!]!!] = true
                } else {
                    totalCheck--
                    expenseEqual[checkMap[checkMe]!!] = false
                }
                if (totalCheck <= 0) {
                    checkMe.isChecked = true
                    totalCheck++
                    expenseEqual[checkMap[checkMe]!!] = true
                    Toast.makeText(context, "atleast someone has to contribute", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            checkFriend.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalCheck++
                    expenseEqual[checkMap[checkFriend]!!] = true
                } else {
                    totalCheck--
                    expenseEqual[checkMap[checkFriend]!!] = false
                }
                if (totalCheck <= 0) {
                    checkFriend.isChecked = true
                    totalCheck++
                    expenseEqual[checkMap[checkFriend]!!] = true
                    Toast.makeText(context, "atleast someone has to contribute", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            addFriends(equalBinding.linearCheck, friendDetailList)
        }
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