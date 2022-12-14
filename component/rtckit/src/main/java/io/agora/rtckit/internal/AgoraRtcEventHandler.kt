package io.agora.rtckit.internal

import android.os.Handler
import android.os.Looper
import io.agora.buddy.tool.logD
import io.agora.buddy.tool.logE
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtckit.annotation.RtcNetWorkQuality
import io.agora.rtckit.constants.RtcKitConstant
import io.agora.rtckit.open.status.RtcAudioChangeStatus
import io.agora.rtckit.open.status.RtcAudioVolumeIndicationStatus
import io.agora.rtckit.open.status.RtcAudioVolumeInfo
import io.agora.rtckit.open.status.RtcErrorStatus

/**
 * @author create by zhangwei03
 *
 */
internal class AgoraRtcEventHandler(var rtcListener: IRtcClientListener?) : IRtcEngineEventHandler() {

    companion object {
        const val TAG = "${RtcKitConstant.RTC_TAG} AgoraRtcEventHandler"
    }

    private var handler = Handler(Looper.getMainLooper())

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        "onJoinChannelSuccess channel:$channel,uid:$uid,elapsed:$elapsed".logD(TAG)
        rtcListener?.onJoinChannelSuccess(channel, uid, elapsed)
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        super.onLeaveChannel(stats)
        "onLeaveChannel stats:${stats?.totalDuration}".logE(RtcBaseClientEx.TAG)
        rtcListener?.onLeaveChannel()
    }

    override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
        super.onClientRoleChanged(oldRole, newRole)
        "onClientRoleChanged oldRole:${getClientRole(oldRole)},newRole:${getClientRole(newRole)}".logD(TAG)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        rtcListener?.onUserJoined(uid, true)
        "onUserJoined uid:$uid,elapsed:$elapsed".logD(TAG)
    }

    private fun getUserOfflineReason(reason: Int): String {
        return when (reason) {
            Constants.USER_OFFLINE_QUIT -> "When the user leaves the channel, the user sends a goodbye message. When this message is received, the SDK determines that the user leaves the channel."
            Constants.USER_OFFLINE_DROPPED -> "When no data packet of the user is received for a certain period of time, the SDK assumes that the user drops offline. A poor network connection may lead to false detection, so we recommend using the RTM SDK for reliable offline detection."
            Constants.USER_OFFLINE_BECOME_AUDIENCE -> "The user switches the user role from a broadcaster to an audience."
            else -> "Unknown"
        }
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        rtcListener?.onUserJoined(uid, false)
        "onUserOffline uid:$uid,reason:${getUserOfflineReason(reason)}".logD(TAG)
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        super.onNetworkQuality(uid, txQuality, rxQuality)
//        val status = RtcNetWorkStatus(
//            userId = uid.toString(),
//            txQuality = coverNetworkQuality(txQuality),
//            rxQuality = coverNetworkQuality(rxQuality)
//        )
//        if (0 == uid) {//??????
//            rtcListener?.onNetWorkStatus(status)
//            "onNetworkQuality uid:$uid,txQuality:${getNetWorkQualityValue(txQuality)}," +
//                    "rxQuality:${getNetWorkQualityValue(rxQuality)}".logD(TAG)
//        }
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        super.onUserMuteAudio(uid, muted)
        rtcListener?.onAudioStatus(RtcAudioChangeStatus.RemoteAudio(uid.toString(), muted))
        "onUserMuteAudio uid:$uid,muted:$muted".logD(TAG)
    }

    /**
     * ?????????????????????????????????????????????
     * state
     * ???????????????????????????
     * AUDIO_MIXING_STATE_PLAYING (710): ???????????????????????????
     * AUDIO_MIXING_STATE_PAUSED (711): ???????????????????????????
     * AUDIO_MIXING_STATE_STOPPED (713): ???????????????????????????
     * AUDIO_MIXING_STATE_FAILED (714): ?????????????????????SDK ?????? errorCode ???????????????????????????????????????
     * reasonCode
     * ????????????
     * AUDIO_MIXING_REASON_OK(0): ?????????
     * AUDIO_MIXING_REASON_CAN_NOT_OPEN (701): ???????????????????????????
     * AUDIO_MIXING_REASON_TOO_FREQUENT_CALL (702): ??????????????????????????????
     * AUDIO_MIXING_REASON_INTERRUPTED_EOF (703): ?????????????????????????????????
     * AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED(721): ???????????????????????????????????????
     * AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED(723): ???????????????????????????????????????
     * AUDIO_MIXING_REASON_STOPPED_BY_USER(724): ???????????? pauseAudioMixing ???????????????????????????
     */
    override fun onAudioMixingStateChanged(state: Int, reasonCode: Int) {
        super.onAudioMixingStateChanged(state, reasonCode)
        if (state == Constants.AUDIO_MIXING_STATE_STOPPED && reasonCode == Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED) {
            rtcListener?.onAudioMixingFinished()
        }
        "onAudioMixingStateChanged stat:$state,reasonCode:$reasonCode".logD(TAG)
    }

    override fun onLocalAudioStateChanged(state: Int, error: Int) {
        super.onLocalAudioStateChanged(state, error)
        "onLocalAudioStateChanged state:$state,error:$error".logD(TAG)
    }

    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        "onRemoteAudioStateChanged uid:$uid,state:$state,reason:$reason,elapsed:$elapsed".logD(TAG)
    }

    override fun onError(err: Int) {
        super.onError(err)
        rtcListener?.onError(RtcErrorStatus(err, "An error occurred during SDK runtime."))
        "onError err:$err,see:\n https://docs.agora.io/cn/voice-call-4.x/API%20Reference/java_ng/API/class_irtcengineeventhandler.html?platform=Android#callback_onerror".logE(
            TAG
        )
    }

    /**
     * state	??????????????????????????????
    CONNECTION_STATE_DISCONNECTED(1)?????????????????????
    CONNECTION_STATE_CONNECTING(2)????????????????????????
    CONNECTION_STATE_CONNECTED(3)??????????????????
    CONNECTION_STATE_RECONNECTING(4)??????????????????????????????
    CONNECTION_STATE_FAILED(5)?????????????????????
    reason	??????????????????????????????????????????????????????
    CONNECTION_CHANGED_CONNECTING(0)????????????????????????
    CONNECTION_CHANGED_JOIN_SUCCESS(1)?????????????????????
    CONNECTION_CHANGED_INTERRUPTED(2)?????????????????????
    CONNECTION_CHANGED_BANNED_BY_SERVER(3)????????????????????????????????????????????????????????????????????????????????????
    CONNECTION_CHANGED_JOIN_FAILED(4)?????????????????????
    CONNECTION_CHANGED_LEAVE_CHANNEL(5)???????????????
    CONNECTION_CHANGED_INVALID_APP_ID(6)?????????????????? APP ID????????????????????? APP ID ??????????????????
    CONNECTION_CHANGED_INVALID_CHANNEL_NAME(7)???????????????????????????????????????????????????????????????????????????
    CONNECTION_CHANGED_INVALID_TOKEN(8)???????????? Token ?????????????????????????????????
    ???????????????????????? App Certificate??????????????????????????? Token??????????????? App Certificate??????????????? Token
    ????????? joinChannel ???????????????????????? uid ????????? Token ???????????? uid ?????????
    CONNECTION_CHANGED_TOKEN_EXPIRED(9)?????????????????? Token ?????????????????????????????????????????????????????????????????? Token
    CONNECTION_CHANGED_REJECTED_BY_SERVER(10)?????????????????????????????????????????????????????????
    ??????????????????????????????????????????????????? API????????? joinChannel???
    ??????????????? startEchoTest ?????????????????????????????????????????????????????????????????????????????????????????????
    CONNECTION_CHANGED_SETTING_PROXY_SERVER(11)????????????????????????????????????SDK ????????????
    CONNECTION_CHANGED_RENEW_TOKEN(12)????????? Token ??????????????????????????????
    CONNECTION_CHANGED_CLIENT_IP_ADDRESS_CHANGED(13)???????????? IP ?????????????????????????????????????????????????????????????????? IP ???????????????????????????
    CONNECTION_CHANGED_KEEP_ALIVE_TIMEOUT(14)???SDK ?????????????????????????????????????????????????????????
     */
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        "onConnectionStateChanged state:$state,reason:$reason".logD(TAG)
    }

    /**
     * ???????????????????????????
     * ???????????????????????????????????? enableAudioVolumeIndication ???????????????
     * ?????????????????????????????????????????????SDK ???????????????????????? enableAudioVolumeIndication ?????????????????????????????? onAudioVolumeIndication ?????????
     * ????????????????????? onAudioVolumeIndication ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? 3 ??????????????????????????????
     */
    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        if (!speakers.isNullOrEmpty()) {
            val speakerInfoList = mutableListOf<RtcAudioVolumeInfo>()

            speakers.forEachIndexed { index, audioVolumeInfo ->
                speakerInfoList.add(
                    RtcAudioVolumeInfo(uid = audioVolumeInfo.uid, volume = audioVolumeInfo.volume)
                )
//                "onAudioVolumeIndication uid:${audioVolumeInfo.uid},volume:${audioVolumeInfo.volume}".logD(TAG)
            }
            rtcListener?.onAudioVolumeIndication(RtcAudioVolumeIndicationStatus(speakerInfoList))
        }
    }

    private fun getClientRole(role: Int): String {
        return if (role == Constants.CLIENT_ROLE_BROADCASTER) "BROADCASTER" else "AUDIENCE"
    }

    /**
     *  QUALITY_UNKNOWN(0)???????????????
    QUALITY_EXCELLENT(1)???????????????
    QUALITY_GOOD(2)????????????????????????????????????????????????????????????????????????
    QUALITY_POOR(3)????????????????????????????????????????????????
    QUALITY_BAD(4)??????????????????????????????
    QUALITY_VBAD(5)?????????????????????????????????????????????
    QUALITY_DOWN(6)??????????????????????????????????????????
     */
    @RtcNetWorkQuality
    private fun coverNetworkQuality(quality: Int): Int {
        return when (quality) {
            Constants.QUALITY_EXCELLENT -> RtcNetWorkQuality.QualityExcellent
            Constants.QUALITY_GOOD -> RtcNetWorkQuality.QualityGood
            Constants.QUALITY_POOR -> RtcNetWorkQuality.QualityPoor
            Constants.QUALITY_BAD -> RtcNetWorkQuality.QualityBad
            Constants.QUALITY_VBAD -> RtcNetWorkQuality.QualityVBad
            Constants.QUALITY_DOWN -> RtcNetWorkQuality.QualityDown
            else -> RtcNetWorkQuality.QualityUnknown
        }
    }

    private fun getNetWorkQualityValue(@RtcNetWorkQuality netWorkQuality: Int): String {
        return when (netWorkQuality) {
            RtcNetWorkQuality.QualityExcellent -> "quality excellent"
            RtcNetWorkQuality.QualityGood -> "quality good"
            RtcNetWorkQuality.QualityPoor -> "quality poor"
            RtcNetWorkQuality.QualityBad -> "quality bad"
            RtcNetWorkQuality.QualityVBad -> "quality vbad"
            RtcNetWorkQuality.QualityDown -> "qualitydown"
            else -> "quality unknown"
        }
    }

    fun destroy() {
        rtcListener = null
        handler.removeCallbacksAndMessages(null)
    }
}