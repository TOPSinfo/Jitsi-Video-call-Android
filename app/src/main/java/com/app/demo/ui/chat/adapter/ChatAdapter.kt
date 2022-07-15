package com.app.demo.ui.chat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.demo.R
import com.app.demo.model.chat.MessagesModel
import com.app.demo.ui.chat.activity.ChatActivity
import com.app.demo.utils.Constant
import com.app.demo.utils.Glide
import com.app.demo.utils.Utility
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth


class ChatAdapter(val context: ChatActivity, private val messageList: ArrayList<MessagesModel>,val isGroup:Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = layoutInflater.inflate(R.layout.item_chat_me, parent, false)
            MyMessagesViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_chat_other, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OtherViewHolder) {
            holder.bindItems(context, messageList[position],isGroup)
        } else if (holder is MyMessagesViewHolder) {
            holder.bindItems(context, messageList[position])
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId
                .equals(FirebaseAuth.getInstance().currentUser!!.uid)
        ) {
            0
        } else 1
    }


    class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var txtMessageOther: TextView = itemView.findViewById(R.id.txtMessageOther)
        private var txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        private var txtDateTime1: TextView = itemView.findViewById(R.id.txtDateTime1)
        private var llMessage: LinearLayout = itemView.findViewById(R.id.llMessage)
        private var imgMessage: ImageView = itemView.findViewById(R.id.imgMessage)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var cardViewImage: CardView = itemView.findViewById(R.id.cardViewImage)
        private var txtSenderName: TextView = itemView.findViewById(R.id.txtSenderName)
        private var txtMediaSenderName: TextView = itemView.findViewById(R.id.txtMediaSenderName)
        private var cardViewImageForSenderName: CardView = itemView.findViewById(R.id.cardViewImageForSenderName)

        fun bindItems(context: ChatActivity, chatModel: MessagesModel, isGroup: Boolean) {
            txtMessageOther.text = chatModel.message
            if (chatModel.timeStamp != null) {
                txtDateTime.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
                txtDateTime1.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
            }

            Log.e("","===============${chatModel.messageType} and ${chatModel.senderId} ")

            if (chatModel.messageType == Constant.TYPE_MESSAGE || chatModel.messageType == null) {
                llMessage.visibility = View.VISIBLE
                imgMessage.visibility = View.GONE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.GONE
                cardViewImageForSenderName.visibility = View.GONE

                if(isGroup) {
                    txtSenderName.visibility=View.VISIBLE
                    txtSenderName.text = chatModel.senderName
                }
                else
                {
                    txtSenderName.visibility=View.GONE
                }
            } else if (chatModel.messageType == Constant.TYPE_IMAGE) {

                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.VISIBLE
                if(isGroup) {
                    cardViewImageForSenderName.visibility = View.VISIBLE
                    txtMediaSenderName.visibility=View.VISIBLE
                    txtMediaSenderName.text = chatModel.senderName
                }
                else
                {
//                    cardViewImageForSenderName.visibility = View.GONE
                    txtMediaSenderName.visibility=View.GONE
                    txtMediaSenderName.visibility=View.GONE
                }
                Glide.loadImage(context, chatModel.url, imgMessage)

            } else if (chatModel.messageType == Constant.TYPE_VIDEO) {
                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.VISIBLE
                cardViewImage.visibility = View.VISIBLE
                if(isGroup) {
                    cardViewImageForSenderName.visibility = View.VISIBLE
                    txtMediaSenderName.visibility=View.VISIBLE
                    txtMediaSenderName.text = chatModel.senderName
                }
                else
                {
//                    cardViewImageForSenderName.visibility = View.GONE
                    txtMediaSenderName.visibility=View.GONE
                    txtMediaSenderName.visibility=View.GONE
                }
                Glide.loadImage(context, chatModel.url, imgMessage)
            }

            imgMessage.setOnClickListener {
                if (chatModel.messageType == Constant.TYPE_IMAGE) {
                    context.redirectImageViewActivity(chatModel.url.toString())
                } else {
                    context.redirectVideoPlayActivity(chatModel.videoUrl.toString())
                }
            }
        }
    }

    class MyMessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var txtMessage: TextView = itemView.findViewById(R.id.txtMessage)
        private var txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        private var txtDateTime1: TextView = itemView.findViewById(R.id.txtDateTime1)
        private var llMessage: LinearLayout = itemView.findViewById(R.id.llMessage)
        private var imgMessage: ImageView = itemView.findViewById(R.id.imgMessage)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)

        private var cardViewImage: CardView = itemView.findViewById(R.id.cardViewImage)
        private var imgMessageStatus: ImageView = itemView.findViewById(R.id.imgMessageStatus)
        private var imgMessageStatus1: ImageView = itemView.findViewById(R.id.imgMessageStatus1)
        private var cardViewImageForSenderName: CardView = itemView.findViewById(R.id.cardViewImageForSenderName)
        private var progressBarImage: CircularProgressIndicator =
            itemView.findViewById(R.id.pbImage)

        fun bindItems(context: ChatActivity, chatModel: MessagesModel) {
            txtMessage.text = chatModel.message


            if (chatModel.timeStamp != null) {
                txtDateTime.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
                txtDateTime1.text = Utility.getDate(chatModel.timeStamp!!.toDate().time)
            }

            if (chatModel.messageType == Constant.TYPE_MESSAGE || chatModel.messageType == null) {
                llMessage.visibility = View.VISIBLE
                imgMessage.visibility = View.GONE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.GONE
                cardViewImageForSenderName.visibility = View.GONE
            } else if (chatModel.messageType == Constant.TYPE_IMAGE) {

                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.GONE
                cardViewImage.visibility = View.VISIBLE
                cardViewImageForSenderName.visibility = View.VISIBLE
                Glide.loadImage(context, chatModel.url, imgMessage)

                if (chatModel.status == Constant.TYPE_START_UPLOAD) {
                    progressBarImage.visibility = View.VISIBLE
                    progressBarImage.progress = 20
                    context.uploadImageWithProgress(
                        chatModel.url.toString(),
                        chatModel,
                        progressBarImage
                    )

                    chatModel.status = Constant.TYPE_UPLOADING
                } else {
                    progressBarImage.visibility = View.GONE
                }

            } else if (chatModel.messageType == Constant.TYPE_VIDEO) {

                llMessage.visibility = View.GONE
                imgMessage.visibility = View.VISIBLE
                imgPlay.visibility = View.VISIBLE
                cardViewImage.visibility = View.VISIBLE
                cardViewImageForSenderName.visibility = View.VISIBLE
                Glide.loadImage(context, chatModel.url, imgMessage)

                if (chatModel.status == Constant.TYPE_START_UPLOAD) {
                    progressBarImage.visibility = View.VISIBLE
                    progressBarImage.progress = 20
                    context.uploadVideoWithProgress(
                        chatModel.url.toString(),
                        chatModel,
                        progressBarImage
                    )

                    chatModel.status = Constant.TYPE_UPLOADING
                } else {
                    progressBarImage.visibility = View.GONE
                }
            }

            if (chatModel.status == Constant.TYPE_READ) {
                imgMessageStatus.setImageResource(R.drawable.ic_read)
                imgMessageStatus1.setImageResource(R.drawable.ic_read)
            } else if (chatModel.status == Constant.TYPE_SEND) {
                imgMessageStatus.setImageResource(R.drawable.ic_check_black)
                imgMessageStatus1.setImageResource(R.drawable.ic_check_black)
            } else if (chatModel.status == Constant.TYPE_UPLOADING) {
                imgMessageStatus.setImageResource(R.drawable.ic_upload)
                imgMessageStatus1.setImageResource(R.drawable.ic_upload)
            }

            imgMessage.setOnClickListener {
                if (chatModel.messageType == Constant.TYPE_IMAGE) {
                    context.redirectImageViewActivity(chatModel.url.toString())
                } else {
                    context.redirectVideoPlayActivity(chatModel.videoUrl.toString())
                }
            }
        }
    }
}