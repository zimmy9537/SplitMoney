package com.zimmy.splitmoney.settleup.beforesettleup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.appreference.AppPreference
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityBeforeSettleUpBinding
import com.zimmy.splitmoney.expense.SettleUpActivity
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Transaction
import com.zimmy.splitmoney.models.Transaction_SettleUp
import com.zimmy.splitmoney.models.TransactionResult
import com.zimmy.splitmoney.resultdata.ResultData
import com.zimmy.splitmoney.settleup.balance.viewmodel.BalanceViewModel
import com.zimmy.splitmoney.utils.ExpenseUtils
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import kotlin.math.absoluteValue

@AndroidEntryPoint
class BeforeSettleUpActivity : AppCompatActivity(), BeforeSettleUpAdapter.SettleUpCallBack {


    lateinit var binding: ActivityBeforeSettleUpBinding
    private val viewModel: BalanceViewModel by viewModels()
    var isFriend: Boolean = true

    lateinit var groupCode: String
    lateinit var netBalance: ArrayList<Transaction>
    lateinit var transactionResult: ArrayList<TransactionResult>
    var TAG = BeforeSettleUpActivity::class.java.simpleName
    private lateinit var phoneMap: HashMap<String, String>
    lateinit var myPhone: String
    lateinit var transactionSettleUp: ArrayList<Transaction_SettleUp>

    private var observerPhoneMap = Observer<ResultData<HashMap<String, String>>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.fetching_members),
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressPb.visibility = View.VISIBLE
            }
            is ResultData.Success -> {
                if (resultData.data != null)
                    phoneMap = resultData.data
                if (phoneMap.size > 1) {
                    viewModel.getTransactionResult(
                        isFriend,
                        groupCode,
                        myPhone,
                        phoneMap
                    )
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.alone_in_group),
                        Toast.LENGTH_SHORT
                    )
                }
            }
            else -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.some_failure),
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    private var observer = Observer<ResultData<TransactionResult>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                binding.progressPb.visibility = View.VISIBLE
            }
            is ResultData.Success -> {
                if (resultData.data != null) {
                    //SETUP ADAPTER CLASS HERE
                    transactionResult.add(resultData.data)
                    binding.progressPb.visibility = View.GONE
                    binding.balancesRv.visibility = View.VISIBLE
                    processTransaction(resultData.data)
                    binding.balancesRv.adapter?.notifyDataSetChanged()
                }
            }
            is ResultData.Anonymous -> {
                if (resultData.status != null) {
                    if (resultData.status == Konstants.ALREADY_SETTLED_UP) {
                        binding.balancesRv.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                    } else if (resultData.status == Konstants.SETTLE_UP_VISIBLE) {
                        binding.balancesRv.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                    } else {
                        binding.balancesRv.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.GONE
                        binding.progressPb.visibility = View.GONE
                        Toast.makeText(
                            this@BeforeSettleUpActivity,
                            resources.getString(R.string.F_ed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {
                //no transactions
                binding.balancesRv.visibility = View.GONE
                binding.progressPb.visibility = View.GONE
                binding.noTransactionsTv.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeforeSettleUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)
        groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()
        val appPreference = AppPreference(this)
        myPhone = appPreference.getString(AppPreference.PHONE, AppPreference.PREF_NO_NAME)!!
        phoneMap = HashMap()
        netBalance = ArrayList()
        transactionResult = ArrayList()
        viewModel.memberLiveData.observe(this@BeforeSettleUpActivity,observerPhoneMap)
        viewModel.transactionResultLiveData.observe(this@BeforeSettleUpActivity,observer)
        viewModel.getMemberList(groupCode)
    }

    private fun processTransaction(transaction: TransactionResult){
        if (transaction.sender == myPhone) {
            //money gone RED
            transactionSettleUp.add(
                Transaction_SettleUp(
                    phoneMap[transaction.receiver]!!,
                    transaction.receiver.toString(),
                    -transaction.amount,
                    transaction.receiver.toString()
                )
            )
            Log.v(
                "DETAILS i am sender",
                " send to ${phoneMap[transaction.receiver]}, having phone no. ${transaction.receiver}, amount ${-transaction.amount}"
            )
        } else if (transaction.receiver == myPhone) {
            //money received GREEN
            transactionSettleUp.add(
                Transaction_SettleUp(
                    phoneMap[transaction.sender]!!,
                    transaction.sender.toString(),
                    transaction.amount,
                    transaction.sender.toString()
                )
            )
            Log.v(
                "DETAILS i am receiver",
                " send to me by ${phoneMap[transaction.sender]}, having phone no. ${transaction.sender}, amount ${transaction.amount}"
            )
        }
    }

    override fun callItemClick(transaction: Transaction_SettleUp) {
        val intent = Intent(this@BeforeSettleUpActivity, SettleUpActivity::class.java)
        intent.putExtra(Konstants.FRIENDS, false)
        intent.putExtra(Konstants.DATA, transaction)
        intent.putExtra(Konstants.GROUP_CODE, groupCode)
        startActivity(intent)
    }
}