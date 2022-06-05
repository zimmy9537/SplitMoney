package com.zimmy.splitmoney.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.models.FriendOrTrip

class FriendOrTripAdapter(friendOrTripList: ArrayList<FriendOrTrip>, context: Context) :
    RecyclerView.Adapter<FriendOrTripAdapter.FriendViewHolder>() {

    var friendOrTripList: ArrayList<FriendOrTrip>
    var context: Context

    init {
        this.friendOrTripList = friendOrTripList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friends_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.friendName.setText(friendOrTripList[position].title)
        holder.friendAmount.setText(friendOrTripList[position].amount.toString())
        Picasso.get().load(friendOrTripList[position].imageUri).into(holder.friendImage)
        holder.whoOwe.setText(friendOrTripList[position].owe)
    }

    override fun getItemCount(): Int {
        return friendOrTripList.size
    }


    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var friendImage: ImageView
        var friendName: TextView
        var whoOwe: TextView
        var friendAmount: TextView

        init {
            friendImage = itemView.findViewById(R.id.friendsRv)
            friendName = itemView.findViewById(R.id.friendName)
            whoOwe = itemView.findViewById(R.id.whoOwe)
            friendAmount = itemView.findViewById(R.id.friendAmount)
        }
    }
}