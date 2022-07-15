package com.app.demo.ui.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.demo.R
import com.app.demo.model.user.UserModel
import com.app.demo.ui.dashboard.activity.DashboardActivity

class UserListAdapter(
    val context: DashboardActivity,
    var usersArrayList: ArrayList<UserModel>
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var listOfSelectedUser: ArrayList<String> = ArrayList()
    var isEnableSelection: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = layoutInflater.inflate(R.layout.item_user_list, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {


        if (isEnableSelection) {
            if (usersArrayList[position].isGroup) {
                holder.itemView.isEnabled = false
                holder.itemView.alpha = 0.5f
            }
        } else {
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f
        }

        if (usersArrayList[position].isGroup) {
            holder.txtUserName.text = usersArrayList[position].groupDetailModel!!.name

            com.app.demo.utils.Glide.loadGlideProfileImage(
                context,
                usersArrayList[position].groupDetailModel!!.groupIcon,
                holder.imgUser
            )
            holder.imgStatusOnlineOffline.visibility = View.GONE
        } else {
            holder.txtUserName.text = context.getString(
                R.string.concat_string,
                usersArrayList[position].firstName, usersArrayList[position].lastName
            )

            com.app.demo.utils.Glide.loadGlideProfileImage(
                context,
                usersArrayList[position].profileImage,
                holder.imgUser
            )
            holder.imgStatusOnlineOffline.visibility = View.VISIBLE
        }

        if (usersArrayList[position].isSelectedForCall) {
            holder.imgSelectedUser.visibility = View.VISIBLE
        } else {
            holder.imgSelectedUser.visibility = View.INVISIBLE
        }

        if (usersArrayList[position].isOnline) {
            holder.txtPresence.text = context.getString(R.string.online)
            holder.txtPresence.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
            holder.imgStatusOnlineOffline.setImageResource(R.drawable.circle_light_green)
        } else {
            holder.txtPresence.text = context.getString(R.string.offline)
            holder.txtPresence.setTextColor(ContextCompat.getColor(context, R.color.red))
            holder.imgStatusOnlineOffline.setImageResource(R.drawable.circle_grey)

        }

        holder.itemView.setOnClickListener {
            if (isEnableSelection) {
                if (listOfSelectedUser.contains(usersArrayList[position].uid!!)) {
                    listOfSelectedUser.remove(usersArrayList[position].uid!!)
                    usersArrayList[position].isSelectedForCall = false
                } else {
                    listOfSelectedUser.add(usersArrayList[position].uid!!)
                    usersArrayList[position].isSelectedForCall = true
                }
                notifyDataSetChanged()
            } else {
                context.redirectChatActivity(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        val imgSelectedUser: ImageView = itemView.findViewById(R.id.imgSelected)
        val txtPresence: TextView = itemView.findViewById(R.id.txtPresence)
        val imgStatusOnlineOffline: ImageView = itemView.findViewById(R.id.imgStatusOnlineOffline)

    }

    fun getSelectedUserList(): ArrayList<String> {
        return listOfSelectedUser
    }

    fun resetUserList() {
        listOfSelectedUser.clear()
        for (i in 0..usersArrayList.size - 1) {
            usersArrayList[i].isSelectedForCall = false
        }
        notifyDataSetChanged()
    }

    fun getIsSelectionAvailable(): Boolean {
        return isEnableSelection
    }

    fun setIsSelectionEnable(b: Boolean) {
        isEnableSelection = b
        notifyDataSetChanged()
    }
}