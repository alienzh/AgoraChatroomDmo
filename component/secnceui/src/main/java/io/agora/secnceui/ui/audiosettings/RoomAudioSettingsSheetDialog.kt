package io.agora.secnceui.ui.audiosettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
import io.agora.baseui.dialog.BaseSheetDialog
import io.agora.buddy.tool.ToastTools
import io.agora.buddy.tool.doOnProgressChanged
import io.agora.config.ConfigConstants
import io.agora.secnceui.R
import io.agora.secnceui.bean.RoomAudioSettingsBean
import io.agora.secnceui.constants.ScenesConstant.DISABLE_ALPHA
import io.agora.secnceui.constants.ScenesConstant.ENABLE_ALPHA
import io.agora.secnceui.databinding.DialogChatroomAudioSettingBinding

class RoomAudioSettingsSheetDialog constructor() : BaseSheetDialog<DialogChatroomAudioSettingBinding>() {

    companion object {
        const val KEY_AUDIO_SETTINGS_INFO = "audio_settings"
    }

    private val audioSettingsInfo: RoomAudioSettingsBean by lazy {
        arguments?.getSerializable(KEY_AUDIO_SETTINGS_INFO) as RoomAudioSettingsBean
    }

    var audioSettingsListener: OnClickAudioSettingsListener? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogChatroomAudioSettingBinding {
        return DialogChatroomAudioSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            setOnApplyWindowInsets(root)
            if (audioSettingsInfo.roomType == ConfigConstants.RoomType.Common_Chatroom) {
                ivSpatialAudio.isVisible = false
                mtSpatialAudio.isVisible = false
                mtSpatialAudioArrow.isVisible = false
            }
            if (audioSettingsInfo.enable) {
                mcbAgoraBot.alpha = ENABLE_ALPHA
                pbAgoraBotVolume.alpha = ENABLE_ALPHA
                mtAgoraBotVolumeValue.alpha = ENABLE_ALPHA
                mcbAgoraBotDisable.isVisible = false
            } else {
                mcbAgoraBot.alpha = DISABLE_ALPHA
                pbAgoraBotVolume.alpha = DISABLE_ALPHA
                mtAgoraBotVolumeValue.alpha = DISABLE_ALPHA
                mcbAgoraBotDisable.isVisible = true
            }
            mcbAgoraBot.isEnabled = audioSettingsInfo.enable
            pbAgoraBotVolume.isEnabled = audioSettingsInfo.enable

            mcbAgoraBot.isChecked = audioSettingsInfo.botOpen
            pbAgoraBotVolume.progress = audioSettingsInfo.botVolume
            mtAgoraBotVolumeValue.text = audioSettingsInfo.botVolume.toString()
            mtBestSoundEffectArrow.text =
                RoomAudioSettingsConstructor.getSoundEffectName(view.context, audioSettingsInfo.soundSelection)
            mtNoiseSuppressionArrow.text =
                RoomAudioSettingsConstructor.getAINSName(view.context, audioSettingsInfo.anisMode)
            mtSpatialAudioArrow.text = view.context.getString(R.string.chatroom_off)

            mcbAgoraBot.setOnCheckedChangeListener { button, isChecked ->
                audioSettingsListener?.onBotCheckedChanged(button, isChecked)
            }
            mcbAgoraBotDisable.setOnClickListener {
                activity?.let {
                    ToastTools.showTips(it, getString(R.string.chatroom_only_host_can_change_robot))
                }
            }
            mtBestSoundEffectArrow.setOnClickListener {
                audioSettingsListener?.onSoundEffect(audioSettingsInfo.soundSelection, audioSettingsInfo.enable)
            }
            mtNoiseSuppressionArrow.setOnClickListener {
                audioSettingsListener?.onNoiseSuppression(audioSettingsInfo.anisMode, audioSettingsInfo.enable)
            }
            mtSpatialAudioArrow.setOnClickListener {
                audioSettingsListener?.onSpatialAudio(audioSettingsInfo.spatialOpen, audioSettingsInfo.enable)
            }
            pbAgoraBotVolume.doOnProgressChanged { seekBar, progress, fromUser ->
                mtAgoraBotVolumeValue.text = progress.toString()
                audioSettingsListener?.onBotVolumeChange(seekBar, progress, fromUser)
            }
        }
    }

    /**
     * ???????????????ui
     */
    fun updateBoxCheckBoxView(openBot: Boolean) {
        if (audioSettingsInfo.botOpen == openBot) return
        audioSettingsInfo.botOpen = openBot
        binding?.mcbAgoraBot?.isChecked = audioSettingsInfo.botOpen
    }

    interface OnClickAudioSettingsListener {

        /**???????????????*/
        fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)

        /**???????????????*/
        fun onBotVolumeChange(seekBar: SeekBar?, progress: Int, fromUser: Boolean)

        /**????????????*/
        fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean)

        /**AI??????*/
        fun onNoiseSuppression(ainsMode: Int, isEnable: Boolean)

        /**????????????*/
        fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean)
    }
}