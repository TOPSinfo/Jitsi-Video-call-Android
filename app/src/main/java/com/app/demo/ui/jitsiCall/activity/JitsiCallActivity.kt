package com.app.demo.ui.jitsiCall.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.demo.databinding.ActivityJitsiCallBinding
import com.app.demo.ui.jitsiCall.viewmodel.JitsiViewModel
import com.app.demo.utils.JitsiManager
import com.facebook.react.modules.core.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener

@AndroidEntryPoint
class JitsiCallActivity : AppCompatActivity(), JitsiMeetActivityInterface, JitsiMeetViewListener {


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
        view.listener = this
        setContentView(view)

    }

    /**
     * request permission for call
     */
    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
    }

    /**
     * Conference call already joined
     */
    override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
    }

    /**
     * Conference call terminate
     */
    override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
        jitsiViewModel.changeStatus(roomId, false)
        finish()

    }

    /**
     * Conference call join
     */
    override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
        jitsiViewModel.changeStatus(roomId, true)
    }

    /**
     * backpressd change status of room
     */
    override fun onBackPressed() {
        super.onBackPressed()
        if (view != null) {
            jitsiViewModel.changeStatus(roomId, false)
            view.leave()
        }

    }


}