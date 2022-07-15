package com.app.demo.model.chat

import com.google.firebase.Timestamp

/**
 * Model class is used to handle data of chat messages
 */
data class MessagesModel(
    var senderId: String? = null,
    var receiverId: String? = null,
    var timeStamp: Timestamp? = null,
    var message: String? = null,
    var messageType: String? = null,
    var url: String? = null,
    var videoUrl: String? = null,
    var status: String? = null,
    var messageId: String? = null,
    var senderName: String? = null,
)
