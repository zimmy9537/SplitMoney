package com.zimmy.splitmoney.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.models.Activity

class ActivityAdapter(activityList: ArrayList<Activity>, context: Context) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    var activityList: ArrayList<Activity>
    var context: Context

    init {
        this.activityList = activityList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activites_item, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        Picasso.get().load(activityList.get(position).imageUri).into(holder.activityImage)
        holder.activityTitle.setText(activityList.get(position).title)
        holder.activityDescription.setText(activityList.get(position).description)
    }

    override fun getItemCount(): Int {
        return activityList.size
    }


    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var activityImage: ImageView
        var activityTitle: TextView
        var activityDescription: TextView

        init {
            activityImage = itemView.findViewById(R.id.activityImage)
            activityTitle = itemView.findViewById(R.id.activityTitle)
            activityDescription = itemView.findViewById(R.id.activityOwe)
        }
    }

}