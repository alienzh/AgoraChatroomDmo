package io.agora.chatroom.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.ValueCallBack;
import io.agora.baseui.general.callback.ResultCallBack;
import io.agora.baseui.general.net.Resource;
import io.agora.buddy.tool.LogToolsKt;
import io.agora.buddy.tool.ThreadManager;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chatroom.bean.RoomKitBean;
import io.agora.chatroom.controller.RtcRoomController;
import io.agora.chatroom.general.livedatas.SingleSourceLiveData;
import io.agora.chatroom.general.repositories.ChatroomRepository;
import io.agora.chatroom.general.repositories.NetworkOnlyResource;
import io.agora.chatroom.general.repositories.ProfileManager;
import kotlin.Pair;
import tools.DefaultValueCallBack;
import tools.bean.VRoomBean;
import tools.bean.VRoomInfoBean;

public class ChatroomViewModel extends AndroidViewModel {

    private static final String TAG = "ChatroomViewModel";

    private ChatroomRepository mRepository;
    private SingleSourceLiveData<Resource<VRoomBean>> roomObservable;
    private SingleSourceLiveData<Resource<Boolean>> joinObservable;
    private SingleSourceLiveData<Resource<VRoomInfoBean>> roomDetailsObservable;
    private SingleSourceLiveData<Resource<VRoomInfoBean>> createObservable;
    private SingleSourceLiveData<Resource<Boolean>> leaveObservable;
    private SingleSourceLiveData<Resource<Boolean>> openBotObservable;
    private SingleSourceLiveData<Resource<Boolean>> closeBotObservable;
    private SingleSourceLiveData<Resource<Pair<Integer, Boolean>>> robotVolumeObservable;
    private SingleSourceLiveData<Resource<Boolean>> checkObservable;
    private SingleSourceLiveData<Resource<Boolean>> roomNoticeObservable;
    private final AtomicBoolean joinRtcChannel = new AtomicBoolean(false);
    private final AtomicBoolean joinImRoom = new AtomicBoolean(false);
    Handler handler = new Handler();

    public ChatroomViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ChatroomRepository();
        roomObservable = new SingleSourceLiveData<>();
        joinObservable = new SingleSourceLiveData<>();
        roomDetailsObservable = new SingleSourceLiveData<>();
        createObservable = new SingleSourceLiveData<>();
        leaveObservable = new SingleSourceLiveData<>();
        openBotObservable = new SingleSourceLiveData<>();
        closeBotObservable = new SingleSourceLiveData<>();
        robotVolumeObservable = new SingleSourceLiveData<>();
        checkObservable = new SingleSourceLiveData<>();
        roomNoticeObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<VRoomBean>> getRoomObservable() {
        return roomObservable;
    }

    public LiveData<Resource<VRoomInfoBean>> getCreateObservable() {
        return createObservable;
    }

    public LiveData<Resource<Boolean>> getJoinObservable() {
        return joinObservable;
    }

    public LiveData<Resource<VRoomInfoBean>> getRoomDetailObservable() {
        return roomDetailsObservable;
    }

    public LiveData<Resource<Boolean>> getRoomNoticeObservable() {
        return roomNoticeObservable;
    }

    public LiveData<Resource<Boolean>> getLeaveObservable() {
        return leaveObservable;
    }

    public LiveData<Resource<Boolean>> getOpenBotObservable() {
        return openBotObservable;
    }

    public LiveData<Resource<Boolean>> getCloseBotObservable() {
        return closeBotObservable;
    }

    public LiveData<Resource<Pair<Integer, Boolean>>> getRobotVolumeObservable() {
        return robotVolumeObservable;
    }

    public LiveData<Resource<Boolean>> getCheckPasswordObservable() {
        return checkObservable;
    }

    public void getDataList(Context context, int pageSize, int type, String cursor) {
        roomObservable.setSource(mRepository.getRoomList(context, pageSize, type, cursor));
    }

    public void getDetails(Context context, String roomId) {
        roomDetailsObservable.setSource(mRepository.getRoomInfo(context, roomId));
    }

    public void joinRoom(Context context, String roomId, String password) {
        if (joinRtcChannel.get() && joinImRoom.get()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    joinObservable.setSource(mRepository.joinRoom(context, roomId, password));
                }
            }, 200);
        }
    }

    public void leaveRoom(Context context, String roomId) {
        leaveObservable.setSource(mRepository.leaveRoom(context, roomId));
    }

    public void initSdkJoin(RoomKitBean roomKitBean, String password) {
        joinRtcChannel.set(false);
        joinImRoom.set(false);
        RtcRoomController.get().joinChannel(getApplication(), roomKitBean.getChannelId(),
                ProfileManager.getInstance().getProfile().getRtc_uid(),
                roomKitBean.getSoundEffect(),
                roomKitBean.isOwner(),
                new DefaultValueCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        LogToolsKt.logE("rtc  joinChannel onSuccess ", TAG);
                        joinRtcChannel.set(true);
                        joinRoom(getApplication(), roomKitBean.getRoomId(), password);
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        ThreadManager.getInstance().runOnMainThread(() -> joinObservable.setSource(new NetworkOnlyResource<Boolean>() {
                            @Override
                            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                                callBack.onError(error, errorMsg);
                            }
                        }.asLiveData()));
                        LogToolsKt.logE("rtc  joinChannel onError " + error + "  " + errorMsg, TAG);
                    }
                }
        );
        ChatClient.getInstance().chatroomManager().joinChatRoom(roomKitBean.getChatroomId(), new ValueCallBack<ChatRoom>() {
            @Override
            public void onSuccess(ChatRoom value) {
                LogToolsKt.logE("im  joinChatRoom onSuccess ", TAG);
                joinImRoom.set(true);
                joinRoom(getApplication(), roomKitBean.getRoomId(), password);
            }

            @Override
            public void onError(int error, String errorMsg) {
                ThreadManager.getInstance().runOnMainThread(() -> joinObservable.setSource(new NetworkOnlyResource<Boolean>() {
                    @Override
                    protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                        callBack.onError(error, errorMsg);
                    }
                }.asLiveData()));
                LogToolsKt.logE("im  joinChatRoom onError " + error + "  " + errorMsg, TAG);
            }
        });
    }

    public void createNormalRoom(Context context, String name, boolean is_private, String password,
                                 boolean allow_free_join_mic, String sound_effect) {
        createObservable.setSource(mRepository.createRoom(context, name, is_private, password, 0,
                allow_free_join_mic, sound_effect));
    }

    public void createNormalRoom(Context context, String name, boolean is_private,
                                 boolean allow_free_join_mic, String sound_effect) {
        createObservable.setSource(mRepository.createRoom(context, name, is_private, "", 0,
                allow_free_join_mic, sound_effect));
    }

    public void createSpatial(Context context, String name, boolean is_private, String password) {
        createObservable.setSource(mRepository.createRoom(context, name, is_private, password, 1,
                false, "Social Chat"));
    }

    public void createSpatial(Context context, String name, boolean is_private) {
        createObservable.setSource(mRepository.createRoom(context, name, is_private, "", 1,
                false, "Social Chat"));
    }

    public void activeBot(Context context, String roomId, boolean active) {
        if (active) {
            openBotObservable.setSource(mRepository.activeBot(context, roomId, true));
        } else {
            closeBotObservable.setSource(mRepository.activeBot(context, roomId, false));
        }
    }

    public void checkPassword(Context context, String roomId, String password) {
        checkObservable.setSource(mRepository.checkPassword(context, roomId, password));
    }


    public void updateBotVolume(Context context, String roomId, int robotVolume) {
        robotVolumeObservable.setSource(mRepository.changeRobotVolume(context, roomId, robotVolume));
    }

    public void updateRoomNotice(Context context, String roomId, String notice) {
        roomNoticeObservable.setSource(mRepository.updateRoomNotice(context, roomId, notice));
    }

    /**
     * ??????????????????
     */
    public void clearRegisterInfo() {
        roomObservable.call();
        joinObservable.call();
        createObservable.call();
    }
}
