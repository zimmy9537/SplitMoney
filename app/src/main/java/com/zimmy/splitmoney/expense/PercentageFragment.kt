package com.zimmy.splitmoney.expense

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.FragmentPercentageBinding
import com.zimmy.splitmoney.models.Friend

class PercentageFragment : Fragment() {

    private var _percentageBinding: FragmentPercentageBinding? = null
    private val percentageBinding get() = _percentageBinding!!

    private var isFriend: Int = 0
    private lateinit var friendName: String
    private lateinit var friendPhone: String
    private var amount: Double = 0.00

    //i think equal map needed only in the case of group expense
    private lateinit var expensePercent: HashMap<String, Double>
    private lateinit var phoneMap: HashMap<String, String>
    private lateinit var friendList: ArrayList<String>
    private var totalMembers = 0
    private lateinit var friendDetailList: ArrayList<Friend>
    private var remainPercent = 100.00
    private var groupTotal = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFriend =
            requireActivity().intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        expensePercent = HashMap()
        phoneMap = HashMap()
        friendList = ArrayList()

        amount = requireActivity().intent.getDoubleExtra(Konstants.AMOUNT, 0.00)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            val personalPreference =
                context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            friendName = requireActivity().intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = requireActivity().intent.getStringExtra(Konstants.PHONE).toString()

            phoneMap[friendName] = friendPhone
            phoneMap["Me"] = personalPreference?.getString(Konstants.PHONE, "9537830943").toString()

            expensePercent[phoneMap["Me"]!!] = 0.00
            expensePercent[phoneMap[friendName]!!] = 0.00
            totalMembers = 2
            friendList.add("Me")
            friendList.add(friendName);
        } else {//group
            friendDetailList =
                requireActivity().intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>

            for (ele in friendDetailList) {
                if (ele.phone == context?.getSharedPreferences(
                        Konstants.PHONE,
                        Context.MODE_PRIVATE
                    )
                        ?.getString(Konstants.PHONE, "9537830943")
                        .toString()
                ) {
                    ele.name = "You"
                }
                phoneMap[ele.name] = ele.phone!!
            }

            for (ele in friendDetailList) {
                expensePercent[phoneMap[ele.name]!!] = 0.00
                friendList.add(ele.name)
            }
            totalMembers = friendDetailList.size
        }
    }

    private fun addFriends(linearLayout: LinearLayout, friendList: ArrayList<String>) {
        for (friend in friendList) {
            val view = layoutInflater.inflate(R.layout.percent_expense_item, null, false)
            val friendName = view.findViewById<TextView>(R.id.friendName)
            val friendAmount = view.findViewById<TextView>(R.id.friendAmount)
            val percentEt = view.findViewById<EditText>(R.id.friendPercent)

            friendAmount.text = "0.00"
            friendName.text = friend

            percentEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (percentEt.text.isEmpty()) {
                        friendAmount.text = "0.0"
                        return
                    }

                    var percent = percentEt.text.toString().toDouble()
                    var temp = remainPercent
                    temp += percent
                    if (percent > temp) {
                        Log.v("crossed", "percent $percent , remain $remainPercent");
                        Toast.makeText(context, "Limits crossed", Toast.LENGTH_SHORT).show()
                        percentEt.setText("")
                        expensePercent[phoneMap[friend]!!] = 0.00
                        friendAmount.text = "0.0"
                        percentEt.hint = "0.00"
                    } else {
                        expensePercent[phoneMap[friend]!!] = percent
                        remainPercent -= percent
                        percent = amount * percent / 100
                        friendAmount.text = percent.toString()
                    }
                    percent = 0.00
                    for (ele in expensePercent) {
                        percent += ele.value
                    }
                    if (percent > 100.00) {
                        Toast.makeText(context, "percentage imbalance ", Toast.LENGTH_SHORT).show()
                    }
                    remainPercent = 100 - percent
                    percentageBinding.percentLeft.text = "${remainPercent}% left"
                    percentageBinding.percentDetails.text = "${percent}% of 100%"
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })
            percentageBinding.linear.addView(view)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _percentageBinding = FragmentPercentageBinding.inflate(inflater, container, false)
        val root = percentageBinding.root

        addFriends(percentageBinding.linear, friendList)
        percentageBinding.save.setOnClickListener {
            var percent = 0.00
            for (ele in expensePercent) {
                percent += ele.value
            }
            if (percent > 100.00) {
                Toast.makeText(context, "percentage imbalance $percent", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent()
            intent.putExtra(Konstants.EQUAL_PERCENT_MAP, expensePercent)
            intent.putExtra(Konstants.EXPENSE, isFriend)
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }

        return root
    }

}