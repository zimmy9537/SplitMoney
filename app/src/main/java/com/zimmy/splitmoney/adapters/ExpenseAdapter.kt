package com.zimmy.splitmoney.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Expense
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ExpenseAdapter(expenseList: ArrayList<Expense>, context: Context) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenseList: ArrayList<Expense>
    private var context: Context
    private var personalPreferences: SharedPreferences
    private var myPhone: String

    init {
        this.expenseList = expenseList
        this.context = context
        personalPreferences = context.getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        myPhone = personalPreferences.getString(Konstants.PHONE, "6352938170").toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        //todo needs to change here according to the changes in the Expense class
        holder.expenseName.text = expenseList[position].expenseName
//        if (expenseList[position].personPhone == myPhone) {
//            holder.expenseAdded.text = "Added by You"
//            holder.expenseResult.text = "Owes You"
//        } else {
//            holder.expenseAdded.text = "Added by ${expenseList[position].personName}"
//            holder.expenseResult.text = "You Owe"
//        }
        //"EEE, MMM d, ''yy"
        val fDate: String = SimpleDateFormat("EEE, MMM d, ''yy").format(Date())
        holder.expenseDate.text = fDate
        holder.expenseAmount.text = "$${expenseList[position].amount}"
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }


    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expenseImage: ImageView
        val expenseName: TextView
        val expenseAdded: TextView
        val expenseDate: TextView
        val expenseResult: TextView
        val expenseAmount: TextView

        init {
            expenseImage = itemView.findViewById(R.id.expenseImage)
            expenseName = itemView.findViewById(R.id.expenseName)
            expenseAdded = itemView.findViewById(R.id.expenseAddedBy)
            expenseDate = itemView.findViewById(R.id.expenseDate)
            expenseResult = itemView.findViewById(R.id.expenseResult)
            expenseAmount = itemView.findViewById(R.id.expenseAmount)
        }
    }
}