package com.zimmy.splitmoney.settleup.beforesettleup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.databinding.BeforeSettleItemBinding
import com.zimmy.splitmoney.models.Transaction_SettleUp

class BeforeSettleUpAdapter(
    val context: Context,
    val beforeSettleUpList: ArrayList<Transaction_SettleUp>,
    val callBack: SettleUpCallBack
) :
    RecyclerView.Adapter<BeforeSettleUpAdapter.BeforeSettleUpViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeforeSettleUpViewHolder {
        val binding = BeforeSettleItemBinding.inflate(LayoutInflater.from(context))
        return BeforeSettleUpViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return beforeSettleUpList.size
    }

    override fun onBindViewHolder(holder: BeforeSettleUpViewHolder, position: Int) {
        with(holder) {
            with(beforeSettleUpList[position]) {
                binding.friendName.text = this.friendName
                if (this.friendEmail == null)
                    binding.friendContact.visibility = View.GONE
                else
                    binding.friendContact.text = this.friendEmail
                binding.oweAmount.text = "\u20B9${this.amount}"
                if (this.amount > 0) {
                    binding.oweString.text = Konstants.OWES_YOU
                } else {
                    binding.oweString.text = Konstants.YOU_OWE
                }

                binding.root.setOnClickListener {
                    callBack.callItemClick(this)
                }
            }
        }
    }

    interface SettleUpCallBack {
        fun callItemClick(transaction: Transaction_SettleUp)
    }

    class BeforeSettleUpViewHolder(val binding: BeforeSettleItemBinding) : ViewHolder(binding.root)
}