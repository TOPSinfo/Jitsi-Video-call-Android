package com.app.demo.ui.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.demo.R
import com.app.demo.model.CallListModel
import com.app.demo.ui.dashboard.activity.DashboardActivity
import com.app.demo.ui.dashboard.listener.ClickListeners

class ActiveCallListAdapter(
    val context: DashboardActivity,
    var activeCallList: ArrayList<CallListModel>,
    var listener: ClickListeners,
) : RecyclerView.Adapter<ActiveCallListAdapter.UserViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var listOfSelectedUser: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = layoutInflater.inflate(R.layout.item_call_list, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.txtCallName.text = "Call From " + activeCallList[position].hostName
        holder.itemView.setOnClickListener {
            listener.startCall(activeCallList[position].docId)
        }

    }

    override fun getItemCount(): Int {
        return activeCallList.size
    }


    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCallName: TextView = itemView.findViewById(R.id.txtCallName)

    }



}