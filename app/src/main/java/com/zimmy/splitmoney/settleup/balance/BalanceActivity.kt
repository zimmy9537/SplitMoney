package com.zimmy.splitmoney.settleup.balance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.appreference.AppPreference
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.ActivityBalanceBinding
import com.zimmy.splitmoney.expense.BeforeSettleUpActivity
import com.zimmy.splitmoney.expense.SettleUpActivity
import com.zimmy.splitmoney.models.Transaction_result
import com.zimmy.splitmoney.resultdata.ResultData
import com.zimmy.splitmoney.settleup.balance.viewmodel.BalanceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BalanceActivity : AppCompatActivity() {

    lateinit var binding: ActivityBalanceBinding
    private val viewModel: BalanceViewModel by viewModels()
    lateinit var groupCode: String
    var isFriend: Boolean = true
    lateinit var transactionResult: ArrayList<Transaction_result>
    lateinit var groupReference: DatabaseReference
    var TAG = "LOG" + BalanceActivity::class.java.simpleName
    lateinit var phoneMap: HashMap<String, String>
    lateinit var myPhone: String

    private var observer = Observer<ResultData<Transaction_result?>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                Log.d(TAG,"LOADING2")
                binding.progressPb.visibility = View.VISIBLE
            }
            is ResultData.Success -> {
                Log.d(TAG,"SUCCESS2")
                if (resultData.data != null) {
                    transactionResult.add(resultData.data)
                    Log.d(
                        TAG,
                        "${resultData.data.sender} sends ${resultData.data.receiver} an amount ${resultData.data.amount}"
                    )
                    binding.balancesRv.adapter?.notifyDataSetChanged()
                    binding.progressPb.visibility = View.GONE
                    binding.balancesRv.visibility =View.VISIBLE
                    binding.settleUpBt.visibility = View.VISIBLE
                }
            }
            is ResultData.Anonymous -> {
                Log.d(TAG,"ANONYMOUS2 ${resultData.message}")
                if (resultData.message != null) {
                    if (resultData.message == Konstants.ALREADY_SETTLED_UP) {
                        binding.balancesRv.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                    } else if (resultData.message == Konstants.SETTLE_UP_VISIBLE) {
                        binding.settleUpBt.visibility = View.VISIBLE
                        binding.balancesRv.visibility = View.VISIBLE
                        binding.progressPb.visibility = View.GONE
                    } else {
                        binding.balancesRv.visibility = View.GONE
                        binding.settleUpBt.visibility = View.GONE
                        binding.alreadySettledTv.visibility = View.GONE
                        binding.progressPb.visibility = View.GONE
                        Toast.makeText(
                            this@BalanceActivity,
                            resources.getString(R.string.F_ed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {
                Log.d(TAG,"FAILURE2")
                Log.d(TAG, resources.getString(R.string.no_transactions))
                //no transactions
                binding.balancesRv.visibility = View.GONE
                binding.progressPb.visibility = View.GONE
                binding.noTransactionsTv.visibility = View.VISIBLE
            }
        }
    }

    private var observerPhoneMap = Observer<ResultData<HashMap<String, String>>> { resultData ->
        when (resultData) {
            is ResultData.Loading -> {
                Toast.makeText(
                    this,
                    resources.getString(R.string.fetching_members),
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressPb.visibility = View.VISIBLE
                Log.d(TAG,"LOADING1")
            }
            is ResultData.Success -> {
                Log.d(TAG,"SUCCESS1")
                if (resultData.data != null)
                    phoneMap = resultData.data
                if (phoneMap.size > 1) {
                    GlobalScope.launch {
                        viewModel.getTransactionResult(
                            isFriend,
                            groupCode,
                            myPhone
                        )
                    }
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.alone_in_group),
                        Toast.LENGTH_SHORT
                    )
                }
            }
            else -> {
                Log.d(TAG,"FAILURE1")
                Toast.makeText(
                    this,
                    resources.getString(R.string.some_failure),
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.backBt.setOnClickListener {
            finish()
        }
    }

    private fun initialize() {
        isFriend = intent.getBooleanExtra(Konstants.FRIENDS, true)
        groupCode = intent.getStringExtra(Konstants.GROUP_CODE).toString()
        val appPreference = AppPreference(this)
        myPhone = appPreference.getString(AppPreference.PHONE, AppPreference.PREF_NO_NAME)!!
        Log.d(TAG, "phone $myPhone")
        phoneMap = HashMap()
        groupReference = FirebaseDatabase.getInstance().reference.child(Konstants.GROUPS)
        transactionResult = ArrayList()
        binding.balancesRv.layoutManager = LinearLayoutManager(this)
        binding.balancesRv.adapter =
            BalanceAdapter(transactionResult, this@BalanceActivity)
        viewModel.transactionResultLiveData.observe(this, observer)
        viewModel.memberLiveData.observe(this, observerPhoneMap)
        GlobalScope.launch {
            viewModel.getMemberList(groupCode)
        }
    }
}