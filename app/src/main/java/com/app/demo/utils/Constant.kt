package com.app.demo.utils

import com.app.demo.model.CallListModel

object Constant {

    // Firestore tables
    const val TABLE_USER = "USER"
    const val TABLE_MESSAGES = "Messages"
    const val TABLE_MESSAGE_COLLECTION = "message"
    const val TABLE_GROUPCALL = "GroupCall"
    const val TABLE_GROUPDETAIL = "GroupDetail"


    // Fields of firstore database
    const val FIELD_UID = "uid"
    const val FIELD_EMAIL = "email"
    const val FIELD_FIRST_NAME = "first_name"
    const val FIELD_LAST_NAME = "last_name"
    const val FIELD_PHONE = "phone"
    const val FIELD_PROFILE_IMAGE = "profile_image"
    const val FIELD_IS_ONLINE = "isOnline"

    const val FIELD_MESSAGE = "messageText"
    const val FIELD_RECEIVER_ID = "receiverId"
    const val FIELD_SENDER_ID = "senderId"
    const val FIELD_TIMESTAMP = "timestamp"
    const val FIELD_MESSAGE_TYPE = "message_type"
    const val FIELD_URL = "url"
    const val FIELD_VIDEO_URL = "video_url"
    const val FIELD_MESSAGE_STATUS = "status"

    const val FIELD_CALL_STATUS = "CallStatus"
    const val FIELD_HOST_ID = "HostId"
    const val FIELD_USERIDS = "userIds"
    const val FIELD_HOST_NAME = "HostName"


    const val FIELD_ADMIN_ID = "adminId"
    const val FIELD_ADMIN_NAME = "adminName"
    const val FIELD_GROUP_CREATED_AT = "createdAt"
    const val FIELD_GROUP_ICON = "groupIcon"
    const val FIELD_GROUP_ID = "id"
    const val FIELD_GEOUP_MEMBERS = "members"
    const val FIELD_GROUP_NAME = "name"



    // Validation Messages

    const val validation_error = "Oops Something went wrong.Please try again later"
    const val validation_OTP = "Please enter pin number"
    const val validation_mobile_number = "Please add valid mobile number"

    const val val_optExpired = "This code has expired. Please resend for a new code"
    const val val_optInvalid = "Invalid code. Please try again"

    const val MSG_NO_INTERNET_CONNECTION = "Kindly check the internet connection"
    const val MSG_NO_USER_AVAILABLE = "Users not available"
    const val MSG_UPDATE_SUCCESSFULL = "Profile updated successfully"


    const val PROFILE_IMAGE_PATH = "/images/users"
    const val CHAT_IMAGE_PATH = "/images/chat"
    const val CHAT_VIDEO_PATH = "/videos/chat"

    // Message type
    const val TYPE_MESSAGE = "TEXT"
    const val TYPE_IMAGE = "IMAGE"
    const val TYPE_VIDEO = "VIDEO"

    // Message status
    const val TYPE_SEND = "SEND"
    const val TYPE_READ = "READ"
    const val TYPE_UPLOADING = "UPLOADING"
    const val TYPE_START_UPLOAD = "START_UPLOAD"

    var listOfActiveCall = ArrayList<CallListModel>()
    var USER_NAME = ""
    var USER_PROFILE_IMAGE = ""


    const val PREF_FILE = "pref_topsdemo"
}
