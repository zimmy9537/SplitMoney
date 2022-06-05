package com.zimmy.splitmoney.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.zimmy.splitmoney.New.MessageActivity
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.models.ContactModel
import java.util.*
import kotlin.collections.ArrayList

class PhoneAdapter(phoneList: ArrayList<ContactModel>, context: Context) :
    RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder>(), Filterable {

    private var phoneList: ArrayList<ContactModel>
    var context: Context
    private var phoneListAll: ArrayList<ContactModel>

    init {
        this.phoneList = phoneList
        this.context = context
        phoneListAll = ArrayList(phoneList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false)
        return PhoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        if (phoneList[position].imageUri == null) {
            holder.phoneImage.setImageResource(R.drawable.phone)
        } else {
            holder.phoneImage.setImageURI(Uri.parse(phoneList[position].imageUri))
        }
        holder.phoneName.text = phoneList[position].name
        holder.phoneNumber.text = phoneList[position].phone
        holder.phoneLl.setOnClickListener{
            val intent=Intent(context,MessageActivity::class.java)
            intent.putExtra("contact",phoneList[position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return phoneList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredList: MutableList<ContactModel> = ArrayList()
                if (constraint == null || constraint.length == 0) {
                    filteredList.addAll(phoneListAll)
                } else {
                    val filterPattern =
                        constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                    for (ele in phoneListAll) {
                        if (ele.name.lowercase(Locale.ROOT)
                                .contains(filterPattern) || ele.phone.toLowerCase(Locale.ROOT)
                                .contains(filterPattern)
                        ) {
                            filteredList.add(ele)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                phoneList.clear()
                phoneList.addAll(results.values as Collection<ContactModel>)
                notifyDataSetChanged()
            }
        }
    }


    inner class PhoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var phoneImage: ImageView
        var phoneName: TextView
        var phoneNumber: TextView
        var phoneLl:LinearLayout

        init {
            phoneImage = itemView.findViewById(R.id.phoneIv)
            phoneNumber = itemView.findViewById(R.id.contactNumber)
            phoneName = itemView.findViewById(R.id.contactName)
            phoneLl=itemView.findViewById(R.id.contact)
        }
    }
}