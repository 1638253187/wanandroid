package com.example.windqq.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import com.example.windqq.app.SysUtils;
import com.example.windqq.bean.Constants;
import com.example.windqq.event.MqttArrivedMsgEvent;
import com.example.windqq.event.MqttToSendEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

public class MqttService extends Service {
    private static MqttBroker mqttBroker;
    private static String group_audio_message;
    public static String[] GROUP_AUDIO_MESSAGE;//群音频
    private String[] TOPIC_VIDEO;
    private String[] TOPIC_AUDIO;//语音主题
    private String[] TOPIC_MESSAGE;//文本主题
    private String[] GROUP_MESSAGE;//群组
    private String[] GPS;//群组
    public static String[] GROUP_TEXT;//群组消息
    public static String[] VIDEO_MESSAGE;//视频消息
    public static String[] GROUP_VIDEO_MESSAGE;//视频消息
    public static String[] AUDIO_CLIENT;
    public static String[] SETTING;//设置相关
    private static String video;
    private static String audio;
    private static String audio_client;
    private static String message;
    private static String message_group;
    private static String group_text;
    private static String video_message;
    private static String group_video_message;
    private static String Setting;
    private static String gps;

    private BeatHandler beatHandler;

    public static String getTopicAudio() {
        return audio;
    }

    public static String getTopcSelf() {
        return video;
    }

    public static String getTopcGps() {
        return gps;
    }

    public static String getTopBattery() {
        return Setting;
    }

    public static String getTopmessage() {
        return message;
    }

    public static String getGroup_message() {
        return message_group;
    }

    public static String getGroup_text() {
        return group_text;
    }

    public static String getVideo_message() {
        return video_message;
    }

    public static String getGroup_Video_message() {
        return group_video_message;
    }

    public static String getGroup_Audio_message() {
        return group_audio_message;
    }


    public static String getAudio_client() {
        return audio_client;
    }

    private MqttBroker.MsgListener msgListener;

    public MqttService() {
        Log.i("tag","MqttService()...");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        initMqtt();
        initBeatHeard();
        okfin();
    }

    private void okfin() {
        Log.e("tag", "MQTT准备就绪");
    }

    private void initBeatHeard() {
        beatHandler = new BeatHandler(MqttService.this);
        new BeatThread().start();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        Log.i("tag","unbindService" + conn.toString());
        super.unbindService(conn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        beatHandler.removeCallbacksAndMessages(null);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MqttToSendEvent messageEvent) {
        if (mqttBroker != null) {
            mqttBroker.sendMessage(messageEvent.getTopic(), messageEvent.getMsg());
            Log.e("tag", "消息:" + messageEvent.getMsg());
        } else {
            Log.i("tag","broker is not ready");
        }
    }

    private class BeatThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Message msg = new Message();
                    msg.what = 1;
                    beatHandler.sendMessageDelayed(msg, 1000);
                    Thread.sleep(10 * 1000);//每隔10s执行一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class BeatHandler extends Handler {
        private final WeakReference<Service> mServiceReference;

        BeatHandler(MqttService service) {
            this.mServiceReference = new WeakReference<Service>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MqttService service = (MqttService) mServiceReference.get();
            if (msg.what == 1) {
                String beat = MqttUtil.getHeartBeatOnline(SysUtils.getDeviceId());
                service.mqttBroker.sendMessage(Constants.MQTT_VIDEO_S, beat);
                Log.i("tag","send MQTT beat heard " + beat);
            }
        }
    }

    private void initMqtt() {
        video_message = "qqvideo/message" + SysUtils.getDeviceId();//初始化六个主题
        message = "qqmessage/" + SysUtils.getDeviceId();
//        video = "video/" + SysUtils.getInstance().getDeviceId();
//        audio = "audio/" + SysUtils.getInstance().getDeviceId();
        audio_client = "qqaudio/client";
        message_group = "qqmessage_group/";
//        gps = "video/gps";
        TOPIC_VIDEO = new String[]{video};
        TOPIC_AUDIO = new String[]{audio};
        TOPIC_MESSAGE = new String[]{message};
        GROUP_MESSAGE = new String[]{message_group};
        GPS = new String[]{gps};
        /*视频通话*/
        VIDEO_MESSAGE = new String[]{video_message};
        AUDIO_CLIENT = new String[]{audio_client};
        SETTING = new String[]{Setting};
        mqttBroker = MqttBroker.getInstance(this);
        mqttBroker.subscribe(TOPIC_VIDEO);//订阅六个主题
        mqttBroker.subscribe(TOPIC_AUDIO);/*音频*/
        mqttBroker.subscribe(TOPIC_MESSAGE);/*消息*/
        mqttBroker.subscribe(GROUP_MESSAGE);/*群组*/
        mqttBroker.subscribe(VIDEO_MESSAGE);/*视频*/
        mqttBroker.subscribe(AUDIO_CLIENT);/*语音通话*/
//        mqttBroker.subscribe(SETTING);/*系统设置*/
//        mqttBroker.subscribe(GPS);/*系统设置*/
        Log.e("tag", "已经订阅_群聊视频");
        msgListener = new MqttBroker.MsgListener() {
            @Override
            public void msgArrived(String topic, MqttMessage message) {

                EventBus.getDefault().post(new MqttArrivedMsgEvent(topic, message.toString()));
                Log.i("tag", "类型:" + topic + "->" + message);
            }
        };
        mqttBroker.setMsgListener(msgListener);
    }

    public static void setSubscribe(String groupId) {
        /*群聊参数*/
        group_text = "message/group_text/" + groupId;
        group_video_message = "group/video/message/" + groupId;
        /*群音频*/
        group_audio_message = "group/audio/message/" + groupId;
        /*群视频通话*/
        GROUP_VIDEO_MESSAGE = new String[]{group_video_message};
        /*群语音通话*/
        GROUP_AUDIO_MESSAGE = new String[]{group_audio_message};
        /*群消息发送与接收*/
        GROUP_TEXT = new String[]{group_text};
        mqttBroker.subscribe(GROUP_TEXT);/*群消息*/
        mqttBroker.subscribe(GROUP_VIDEO_MESSAGE);/*群视频*/
        mqttBroker.subscribe(GROUP_AUDIO_MESSAGE);/*群音频*/
        Log.e("tag_id", "已经订阅");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}