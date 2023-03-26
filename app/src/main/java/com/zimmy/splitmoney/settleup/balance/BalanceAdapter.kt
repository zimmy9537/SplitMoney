package com.zimmy.splitmoney.settleup.balance

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.zimmy.splitmoney.databinding.BalanceItemBinding
import com.zimmy.splitmoney.models.TransactionResult

class BalanceAdapter(private val balanceList: ArrayList<TransactionResult>, val context: Context) :
    RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        val binding = BalanceItemBinding.inflate(LayoutInflater.from(context))
        return BalanceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return balanceList.size
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        with(holder) {
            with(balanceList[position]) {
                binding.senderTv.text = this.sender
                binding.receiverTv.text = this.receiver
                binding.amountTv.text = "\u20B9${this.amount}"
            }
        }
    }

    class BalanceViewHolder(val binding: BalanceItemBinding) : ViewHolder(binding.root)
}