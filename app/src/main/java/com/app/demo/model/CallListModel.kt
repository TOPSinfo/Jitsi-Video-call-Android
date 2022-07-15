package com.app.demo.model

/**
 * Model call used to store data of video call
 */
class CallListModel(
    var callStatus: String="",
    var HostId: String="",
    var userIds: ArrayList<String> = ArrayList(),
    var docId:String="",
    var hostName:String=""
)

