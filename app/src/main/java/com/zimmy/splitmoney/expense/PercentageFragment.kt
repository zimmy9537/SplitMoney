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
    private lateinit var myName: String
    private lateinit var myPhone: String
    private var amount: Double = 0.00

    private lateinit var expensePercent: HashMap<String, Double>
    private lateinit var editMap: HashMap<EditText, String>
    private var totalMembers = 0
    private lateinit var friendDetailList: ArrayList<Friend>
    private var remainPercent = 100.00
    private var groupTotal = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFriend =
            requireActivity().intent.getIntExtra(Konstants.EXPENSE, Konstants.INDIVIDUALEXPENSE)

        expensePercent = HashMap()

        myName = context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            ?.getString(Konstants.NAME, "Zimmy").toString()
        myPhone = context?.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
            ?.getString(Konstants.PHONE, "9537830943").toString()

        amount = requireActivity().intent.getDoubleExtra(Konstants.AMOUNT, 0.00)

        if (isFriend == Konstants.INDIVIDUALEXPENSE) {
            friendName = requireActivity().intent.getStringExtra(Konstants.NAME).toString()
            friendPhone = requireActivity().intent.getStringExtra(Konstants.PHONE).toString()

            friendDetailList = ArrayList()
            friendDetailList.add(Friend(friendName, friendPhone))
            friendDetailList.add(Friend(myName, myPhone))

            expensePercent[myPhone] = 0.00
            expensePercent[friendPhone] = 0.00
            totalMembers = 2

        } else {//group
            friendDetailList =
                requireActivity().intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>

            for (ele in friendDetailList) {
                expensePercent[ele.phone!!] = 0.00
            }
            totalMembers = friendDetailList.size
        }
    }

    private fun addFriends(friendDetailList: ArrayList<Friend>) {
        editMap = HashMap()
        for (friend in friendDetailList) {
            val view = layoutInflater.inflate(R.layout.percent_expense_item, null, false)
            val friendName = view.findViewById<TextView>(R.id.friendName)
            val friendAmount = view.findViewById<TextView>(R.id.friendAmount)
            val percentEt = view.findViewById<EditText>(R.id.friendPercent)

            friendAmount.text = "0.00"
            if (friend.phone == myPhone) {
                friendName.text = "Me"
            } else {
                friendName.text = friend.name
            }
            editMap[percentEt] = friend.phone!!

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
                        expensePercent[editMap[percentEt]!!] = 0.00
                        friendAmount.text = "0.0"
                        percentEt.hint = "0.00"
                    } else {
                        expensePercent[editMap[percentEt]!!] = percent
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

        addFriends(friendDetailList)
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