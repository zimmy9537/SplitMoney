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
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.groups.GroupActivity
import com.zimmy.splitmoney.models.Group

class GroupAdapter(
    groupItemList: ArrayList<Group>,
    context: Context,
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    var groupItemList: ArrayList<Group>
    var context: Context

    init {
        this.groupItemList = groupItemList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friends_trip_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.groupImage.setImageResource(R.drawable.ic_launcher_background)
        //todo change this with actual image later
        holder.groupName.setText(groupItemList[position].groupTitle)
        holder.groupAmount.setText(groupItemList[position].amount.toString())
        Picasso.get().load(groupItemList[position].imageUri).into(holder.groupImage)
        holder.whoOwe.setText(groupItemList[position].owe)

        holder.groupItemLl.setOnClickListener {
            //todo work on this intent part
            val intent = Intent(context, GroupActivity::class.java)
            intent.putExtra("gcode",groupItemList[position].groupCode)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return groupItemList.size
    }


    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var groupImage: ImageView
        var groupName: TextView
        var whoOwe: TextView
        var groupAmount: TextView
        var groupItemLl: LinearLayout

        init {
            groupImage = itemView.findViewById(R.id.friendImage)
            groupName = itemView.findViewById(R.id.friendName)
            whoOwe = itemView.findViewById(R.id.whoOwe)
            groupAmount = itemView.findViewById(R.id.friendAmount)
            groupItemLl = itemView.findViewById(R.id.friendItemLl)
        }
    }
}