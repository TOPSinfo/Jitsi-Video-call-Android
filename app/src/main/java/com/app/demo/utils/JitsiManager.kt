package com.app.demo.utils

import android.content.Context
import android.view.View
import com.app.demo.R
import org.jitsi.meet.sdk.*
import java.net.MalformedURLException
import java.net.URL

class JitsiManager(var context: Context) {

    private var defaultOptions: JitsiMeetConferenceOptions.Builder?

    init {
        val serverURL: URL = try {
            URL(context.getString(R.string.server_url))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException(context.getString(R.string.invalid_srver_url))
        }

        var userinfo = JitsiMeetUserInfo()
        userinfo.displayName = Constant.USER_NAME
        if(!Constant.USER_PROFILE_IMAGE.equals("")) {
            var url = URL(Constant.USER_PROFILE_IMAGE)
            userinfo.avatar = url
        }

        defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            .setSubject("")
            .setWelcomePageEnabled(false)
            .setUserInfo(userinfo)
//            .setFeatureFlag("meeting-password.enabled", false)
//            .setFeatureFlag("add-people.enabled", false)
//            .setFeatureFlag("invite.enabled", false)
//            .setFeatureFlag("chat.enabled", false)
//            .setFeatureFlag("kick-out.enabled", false)
//            .setFeatureFlag("live-streaming.enabled", false)
//            .setFeatureFlag("meeting-password.enabled", false)
//            .setFeatureFlag("video-share.enabled", false)
//            .setFeatureFlag("calendar.enabled", false)
//            .setFeatureFlag("lobby-mode.enabled", false)
//            .setFeatureFlag("help-view.enabled", false)
//            .setFeatureFlag("close-captions.enabled", false)
//            .setFeatureFlag("call-integration.enabled", false)
//            .setFeatureFlag("recording.enabled", true)
//            .setFeatureFlag("close-captions.enabled", false)
//            .setFeatureFlag("toolbox.alwaysVisible", false)
//            .setFeatureFlag("help.enabled", false)
//            .setFeatureFlag("raise-hand.enabled", false)
//            .setFeatureFlag("overflow-menu.enabled", true)
//            .setFeatureFlag("meeting-name.enabled", false)
//            .setFeatureFlag("android.screensharing.enabled",false)
//            .build()
//        JitsiMeet.setDefaultConferenceOptions(defaultOptions)
//            .setFeatureFlag("android.screensharing.enabled",false)
//            .setFeatureFlag("audio-mute.enabled", false)
//            .setFeatureFlag("pip.enabled",false)

    }

    /*
     * Start video call
     * */
    fun startVideoCall(roomId: String):JitsiMeetConferenceOptions {

        val options = defaultOptions!!
            .setRoom(roomId)
            .build()
        return options
//        JitsiMeetActivity.launch(context, options)
    }

    /*
     * start custom call using jitsi meet view
     * */
    fun startCustomVideoCall(roomId: String):View{

        val view=JitsiMeetView(context)
        val options = defaultOptions!!
            .setRoom(roomId)
            .build()
        view.join(options)
        return view
//        JitsiMeetActivity.launch(context, options)
    }
}