package io.agora.secnceui.ui.mic

import android.content.Context
import io.agora.config.ConfigConstants
import io.agora.secnceui.R
import io.agora.secnceui.annotation.*
import io.agora.secnceui.bean.*

internal object RoomMicConstructor {

    fun builderDefault2dMicList(): MutableList<MicInfoBean> {
        return mutableListOf(
            MicInfoBean(index = 0),
            MicInfoBean(index = 1),
            MicInfoBean(index = 2),
            MicInfoBean(index = 3),
            MicInfoBean(index = 4),
            MicInfoBean(index = 5)
        )
    }

    fun builderDefault2dBotMicList(context: Context, isUserBot: Boolean = false): MutableList<BotMicInfoBean> {
        val blueBot = MicInfoBean(
            index = 6,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolumeType = ConfigConstants.VolumeType.Volume_None,
            userInfo = RoomUserInfoBean().apply {
                username = context.getString(R.string.chatroom_agora_blue)
                userAvatar = "icon_chatroom_blue_robot"
            }
        )
        val redBot = MicInfoBean(
            index = 7,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolumeType = ConfigConstants.VolumeType.Volume_None,
            userInfo = RoomUserInfoBean().apply {
                username = context.getString(R.string.chatroom_agora_red)
                userAvatar = "icon_chatroom_red_robot"
            }
        )
        return mutableListOf(BotMicInfoBean(blueBot, redBot))
    }

    fun builderDefault3dMicMap(context: Context, isUserBot: Boolean = false): Map<Int, MicInfoBean> {
        return mutableMapOf(
            ConfigConstants.MicConstant.KeyIndex0 to MicInfoBean(index = 0),
            ConfigConstants.MicConstant.KeyIndex1 to MicInfoBean(index = 1),
            ConfigConstants.MicConstant.KeyIndex2 to MicInfoBean(index = 5),
            ConfigConstants.MicConstant.KeyIndex3 to MicInfoBean(index = 6),
            // mic4 中间座位
            ConfigConstants.MicConstant.KeyIndex4 to MicInfoBean(index = 4),
            ConfigConstants.MicConstant.KeyIndex5 to MicInfoBean(
                index = 2,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                userInfo = RoomUserInfoBean().apply {
                    username = context.getString(R.string.chatroom_agora_blue)
                    userAvatar = "icon_chatroom_blue_robot"
                }
            ),
            ConfigConstants.MicConstant.KeyIndex6 to MicInfoBean(
                index = 3,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolumeType = ConfigConstants.VolumeType.Volume_None,
                userInfo = RoomUserInfoBean().apply {
                    username = context.getString(R.string.chatroom_agora_red)
                    userAvatar = "icon_chatroom_red_robot"
                }
            ),
        )
    }

    /**
     * 房主点击麦位管理
     */
    fun builderOwnerMicMangerList(
        context: Context, micInfo: MicInfoBean, isMyself: Boolean
    ): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // 正常
            MicStatus.Normal -> {
                if (isMyself) {
                    mutableListOf(MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute))
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.ForceMute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Lock)
                    )
                }
            }
            // 闭麦
            MicStatus.Mute -> {
                if (isMyself) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.UnMute)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.ForceUnMute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Lock)
                    )
                }
            }
            // 禁言 :有人、没人
            MicStatus.ForceMute -> {
                if (micInfo.userInfo == null) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_invite), true, MicClickAction.Invite),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.ForceUnMute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Lock)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.ForceUnMute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Lock)
                    )
                }
            }
            // 锁麦
            MicStatus.Lock -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.ForceMute),
                    MicManagerBean(context.getString(R.string.chatroom_unblock), true, MicClickAction.UnLock)
                )
            }
            // 锁麦和禁言
            MicStatus.LockForceMute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.ForceUnMute),
                    MicManagerBean(context.getString(R.string.chatroom_unblock), true, MicClickAction.UnLock)
                )
            }
            // 空闲
            MicStatus.Idle -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), true, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.ForceMute),
                    MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Lock)
                )
            }
            else -> mutableListOf()
        }
    }

    /**
     * 嘉宾点击麦位管理
     */
    fun builderGuestMicMangerList(context: Context, micInfo: MicInfoBean): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // 有⼈-正常
            MicStatus.Normal -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }
            // 有⼈-关麦
            MicStatus.Mute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.UnMute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }
            // 有⼈-禁麦（被房主强制静音）
            MicStatus.ForceMute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_unmute), false, MicClickAction.ForceUnMute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }
            // 其他情况 nothing
            else -> {
                mutableListOf()
            }

        }
    }
}