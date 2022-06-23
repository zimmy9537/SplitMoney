package com.zimmy.splitmoney.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zimmy.splitmoney.IndividualExpenseActivity
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.groups.GroupActivity
import com.zimmy.splitmoney.models.FriendItem

class FriendOrTripAdapter(
    friendItemList: ArrayList<FriendItem>,
    context: Context,
) :
    RecyclerView.Adapter<FriendOrTripAdapter.FriendViewHolder>() {

    var friendItemList: ArrayList<FriendItem>
    var context: Context

    init {
        this.friendItemList = friendItemList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friends_trip_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.friendImage.setImageResource(R.drawable.ic_launcher_background)
        //todo change this with actual image later
        holder.friendName.setText(friendItemList[position].title)
        holder.friendAmount.setText(friendItemList[position].amount.toString())
        Picasso.get().load(friendItemList[position].imageUri).into(holder.friendImage)
        holder.whoOwe.setText(friendItemList[position].owe)

        holder.friendItemLl.setOnClickListener {
                val intent = Intent(context, IndividualExpenseActivity::class.java)
                intent.putExtra(Konstants.PHONE, friendItemList[position].phone)
                intent.putExtra(Konstants.NAME,friendItemList[position].title)
                context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return friendItemList.size
    }


    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var friendImage: ImageView
        var friendName: TextView
        var whoOwe: TextView
        var friendAmount: TextView
        var friendItemLl: LinearLayout

        init {
            friendImage = itemView.findViewById(R.id.friendImage)
            friendName = itemView.findViewById(R.id.friendName)
            whoOwe = itemView.findViewById(R.id.whoOwe)
            friendAmount = itemView.findViewById(R.id.friendAmount)
            friendItemLl = itemView.findViewById(R.id.friendItemLl)
        }
    }
}