package com.zimmy.splitmoney.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.ExpenseGroup
import kotlin.math.abs

class GroupExpenseAdapter(
    private val expenseList: ArrayList<ExpenseGroup>,
    private val context: Context
) :
    RecyclerView.Adapter<GroupExpenseAdapter.GroupExpenseViewHolder>() {

    private val personalPreference: SharedPreferences =
        context.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
    private val myPhone: String =
        personalPreference.getString(Konstants.PHONE, "not saved").toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupExpenseViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.group_expense_item, parent, false
        )
        return GroupExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupExpenseViewHolder, position: Int) {
        holder.expenseName.text = expenseList[position].expenseName
        holder.expenseDate.text = expenseList[position].payment_time
        var totalPaid = 0
        //paid false
        if (!expenseList[position].paidByMap?.get(myPhone)!!) {
            if (expenseList[position].expenseMap?.get(myPhone) != 0.0) {
                holder.lentTv.text = "You Borrowed"
                holder.amountTv.text = "${expenseList[position].expenseMap?.get(myPhone)}"
            } else {
                holder.lentTv.text = "Neutral"
                holder.amountTv.text = "0.00"
            }
        } else {
            for (paid in expenseList[position].paidByMap!!) {
                if (paid.value) {
                    totalPaid++
                }
            }
            if (expenseList[position].expenseMap?.get(myPhone) != 0.0) {
                val diff =
                    (expenseList[position].amount / totalPaid) - expenseList[position].expenseMap?.get(
                        myPhone
                    )!!
                if (diff > 0.0) {
                    holder.lentTv.text = "You Lent"
                    holder.amountTv.text = "${abs(diff)}"
                } else {
                    holder.lentTv.text = "You Borrowed"
                    holder.amountTv.text = "${abs(diff)}"
                }
            } else {
                holder.lentTv.text = "You Lent"
                holder.amountTv.text = "${
                    (expenseList[position].amount.div(
                        totalPaid
                    ))
                }"
            }
        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    class GroupExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var expenseName: TextView
        var expenseDate: TextView
        var lentTv: TextView
        var amountTv: TextView

        init {
            expenseName = itemView.findViewById(R.id.expenseName)
            expenseDate = itemView.findViewById(R.id.expenseDate)
            lentTv = itemView.findViewById(R.id.expenseLent)
            amountTv = itemView.findViewById(R.id.expenseAmount)
        }
    }
}