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

class ParticipantsAdapter(
    val context: Context,
    var usersArrayList: ArrayList<UserModel>,
) : RecyclerView.Adapter<ParticipantsAdapter.UserViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = layoutInflater.inflate(R.layout.item_participants, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.txtUserName.text = context.getString(
            R.string.concat_string,
            usersArrayList[position].firstName, usersArrayList[position].lastName
        )

        com.app.demo.utils.Glide.loadGlideProfileImage(
            context,
            usersArrayList[position].profileImage,
            holder.imgUser
        )

    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }


    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
    }


}