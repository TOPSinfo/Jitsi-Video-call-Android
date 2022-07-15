package com.app.demo.ui.groupChat.createGroup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.demo.R
import com.app.demo.model.user.UserModel
import com.app.demo.ui.groupChat.createGroup.activity.SelectUserActivity

class SelectUserAdapter(
    val context: SelectUserActivity,
    var usersArrayList: ArrayList<UserModel>
) : RecyclerView.Adapter<SelectUserAdapter.UserViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var listOfSelectedUser: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = layoutInflater.inflate(R.layout.item_user_list, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.imgStatusOnlineOffline.visibility = View.GONE
        holder.txtUserName.text = context.getString(
            R.string.concat_string,
            usersArrayList[position].firstName, usersArrayList[position].lastName
        )

        com.app.demo.utils.Glide.loadGlideProfileImage(
            context,
            usersArrayList[position].profileImage,
            holder.imgUser
        )

        if (usersArrayList[position].isSelectedForCall) {
            holder.imgSelectedUser.visibility = View.VISIBLE
        } else {
            holder.imgSelectedUser.visibility = View.INVISIBLE
        }


        holder.itemView.setOnClickListener {

            if (listOfSelectedUser.contains(usersArrayList[position].uid!!)) {
                listOfSelectedUser.remove(usersArrayList[position].uid!!)
                usersArrayList[position].isSelectedForCall = false
            } else {
                listOfSelectedUser.add(usersArrayList[position].uid!!)
                usersArrayList[position].isSelectedForCall = true
            }
            notifyDataSetChanged()

        }
    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }

    fun getSelectedUserList(): ArrayList<String> {
        return listOfSelectedUser
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        val imgSelectedUser: ImageView = itemView.findViewById(R.id.imgSelected)
        val txtPresence: TextView = itemView.findViewById(R.id.txtPresence)
        val imgStatusOnlineOffline: ImageView = itemView.findViewById(R.id.imgStatusOnlineOffline)

    }


}