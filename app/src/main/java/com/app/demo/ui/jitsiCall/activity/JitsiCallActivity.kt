package com.app.demo.ui.jitsiCall.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.demo.databinding.ActivityJitsiCallBinding
import com.app.demo.ui.jitsiCall.viewmodel.JitsiViewModel
import com.app.demo.utils.JitsiManager
import com.facebook.react.modules.core.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView


@AndroidEntryPoint
class JitsiCallActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }
    // Example for handling different JitsiMeetSDK events
    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> {

                }
                BroadcastEvent.Type.PARTICIPANT_JOINED -> {

                }
                BroadcastEvent.Type.PARTICIPANT_LEFT -> {
                    JitsiMeetActivityDelegate.onBackPressed()
                    view.dispose()
                    finish()
                }
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    JitsiMeetActivityDelegate.onBackPressed()
                    view.dispose()
                    finish()
                }
                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        view.dispose()
    }

    private lateinit var jitsiManager: JitsiManager
    private val jitsiViewModel: JitsiViewModel by viewModels()
    private lateinit var binding: ActivityJitsiCallBinding
    var roomId = ""
    lateinit var view: JitsiMeetView
    var username = ""
    var isGroupCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJitsiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("RoomId")!!
        username = intent.getStringExtra("OpponentUserName")!!
        isGroupCall = intent.getBooleanExtra("isGroupCall", false)!!

        jitsiManager = JitsiManager(this)
        view = jitsiManager.startCustomVideoCall(roomId) as JitsiMeetView
        setContentView(view)
        registerForBroadcastMessages()
    }

    /**
     * request permission for call
     */
    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
    }


    /**
     * backpressd change status of room
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
//        if (view != null) {
//            jitsiViewModel.changeStatus(roomId, false)
//            view.leave()
//        }

    }


}