package com.zimmy.splitmoney.settleup.balance

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.appreference.AppPreference
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityBalanceBinding
import com.zimmy.splitmoney.expense.BeforeSettleUpActivity
import com.zimmy.splitmoney.expense.SettleUpActivity
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.resultdata.ResultData
import com.zimmy.splitmoney.utils.ExpenseUtils
import java.math.BigDecimal

class BalanceActivity : AppCompatActivity() {

    lateinit var binding: ActivityBalanceBinding

    lateinit var groupCode: String
    var isFriend: Boolean = true
    lateinit var netBalance: ArrayList<Transaction>
    lateinit var members: ArrayList<Friend>
    lateinit var transactionResult: ArrayList<Transaction_result>
    lateinit var groupReference: DatabaseReference
    var TAG = BalanceActivity::class.java.simpleName
    var count: Long = 0
    lateinit var phoneMap: HashMap<String, String>
    lateinit var myPhone: String

    private var observer = Observer<ResultData<Transaction_result>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                binding.progressPb.visibility = View.VISIBLE
            }
            is ResultData.Success -> {
                if (resultData.data != null)
                    transactionResult.add(resultData.data)
            }
            is ResultData.Anonymous -> {
                if (resultData.message != null) {
                    if (resultData.message == Konstants.ALREADY_SETTLED_UP) {
                        binding.balancesLl.visibility = View.GONE
                        binding.settleUpBt.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                    } else if (resultData.message == Konstants.SETTLE_UP_VISIBLE) {
                        binding.settleUpBt.visibility = View.VISIBLE
                        binding.balancesLl.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.VISIBLE
                    } else {
                        binding.balancesLl.visibility = View.GONE
                        binding.settleUpBt.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.GONE
                        binding.progressPb.visibility = View.GONE
                        Toast.makeText(this@BalanceActivity, "F_ed up here", Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding.noTransactionsTv.visibility = View.GONE
                }
            }
            else -> {
                //no transactions
                binding.balancesLl.visibility = View.GONE
                binding.settleUpBt.visibility = View.GONE
                binding.alreadySettledTv.visibility = View.VISIBLE
                binding.progressPb.visibility = View.GONE
                binding.noTransactionsTv.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        initialize()
        binding.settleUpBt.setOnClickListener {
            if (isFriend) {
                val intent = Intent(this@BalanceActivity, SettleUpActivity::class.java)
                intent.putExtra(Konstants.FRIENDS, isFriend)
                startActivity(intent)
            } else {
                val intent = Intent(this@BalanceActivity, BeforeSettleUpActivity::class.java)
                intent.putExtra(Konstants.FRIENDS, isFriend)
                intent.putExtra(Konstants.GROUP_CODE, groupCode)
                startActivity(intent)
            }
        }
    }

    private fun initialize() {
        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)
        val appPreference = AppPreference(this)
        myPhone = appPreference.getString(AppPreference.PHONE, AppPreference.PREF_NO_NAME)!!
        phoneMap = HashMap()
        groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
    }
}