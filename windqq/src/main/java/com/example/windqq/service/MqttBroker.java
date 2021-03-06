package com.example.windqq.service;

import android.content.Context;
import android.util.Log;


import com.example.windqq.bean.Constants;
import com.example.windqq.util.SPUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONObject;

import java.util.UUID;

public class MqttBroker implements MqttCallback {
    // 常量
    private final static boolean CLEAN_START = false;//连接服务器前是否清空上一次连接的订阅主题和没有接收的消息
    private final static short KEEP_ALIVE = 30;      // 低耗网络，但是又需要及时获取数据，心跳30s
    private final static int[] QOS_VALUES = {2};     // 对应主题的消息级别
    // 变量
    private MqttClient mqttClient = null;

    /**
     * mqtt连接参数
     */
    private MqttConnectOptions options = null;
    //服务器端链接ID
    private static String CLIENT_ID;
    private static String MQTT_HOST = null;
    private static MqttBroker mqttbroker = null;
    private static String mqttHost;

    public static void loadMqttBrober() {
        if (mqttbroker == null) {
            MqttBroker.mqttbroker = null;
            MqttBroker.MQTT_HOST = mqttHost;
            MqttBroker.mqttbroker = new MqttBroker();
        }
        if (mqttbroker.mqttClient == null || !mqttbroker.mqttClient.isConnected()) {
            //连接
            // TODO: 2018/4/17 0017 是否需要记录连接状态
            MqttBroker.mqttbroker.connect();
        }
    }

    /**
     * 获取实例
     *
     * @return
     */

    public static MqttBroker getInstance(Context context) {
        String ip = (String) SPUtils.getParam(context,Constants.MQTT_IP, Constants.MQTT_IP_DEFAULT);
        String port = (String) SPUtils.getParam(context, Constants.MQTT_PORT, Constants.MQTT_PORT_DEFAULT);
        mqttHost = "tcp://" + ip + ":" + port;
        Log.e("tag", "mqttip:" + mqttHost);
        loadMqttBrober();
        return mqttbroker;
    }

    /**
     * mqtt中间类
     *
     * @throws MqttException
     */
    private MqttBroker() {
        try {
            CLIENT_ID = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            mqttClient = new MqttClient(MQTT_HOST, CLIENT_ID, null);
            //设置回调
            mqttClient.setCallback(this);
            //MQTT的连接设置
            options = new MqttConnectOptions();
            options.setCleanSession(CLEAN_START);
            options.setConnectionTimeout(20);
            options.setKeepAliveInterval(KEEP_ALIVE);
        } catch (Exception e) {
        }
    }

    //连接丢失
    @Override
    public void connectionLost(Throwable t) {
        Log.i("mqtt","mqtt 断开连接!尝试重连...");
        if (!mqttbroker.connect()) {
            try {
                Thread.sleep(5000);
                connectionLost(t);
            } catch (Exception e) {
                Log.e("mqtt",e.getMessage());
            }
        }
    }

    /**
     * 订阅主题
     *
     * @throws MqttSecurityException
     * @throws MqttException         订阅主题
     */
    public void subscribe(String[] TOPIC) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(TOPIC, QOS_VALUES);
            }
        } catch (Exception e) {
            Log.e("mqtt",e.getMessage());
        }
    }

    /**
     * @throws MqttException 退订主题
     */

    public void unsubcribe(String[] TOPIC) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(TOPIC);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("tag_mqtt", e.getMessage());
        }
    }


    /**
     * 判断mqtt是否连接
     *
     * @return
     */
    public boolean isConnect() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        }
        return false;
    }

    /**
     * mqtt连接方法
     *
     * @throws MqttException
     */
    public synchronized boolean connect() {
        try {
            if (mqttClient.isConnected()) {
                return false;
            } else {
                mqttClient.connect(options);
                Log.i("mqtt","MQTT 已连接...");
            }
        } catch (Exception e) {
            Log.i("mqtt",e.getMessage());
            try {
                Thread.sleep(2000);
                loadMqttBrober();
            } catch (Exception ex) {
                Log.i("mqtt",ex.getMessage());
            }
            return false;
        }
        return true;
    }

    /**
     * 发送消息
     * @param topicStr 主题
     * @param message  消息
     */
    public void sendMessage(String topicStr, String message) {
        try {
            MqttTopic topic = mqttClient.getTopic(topicStr);
            topic.publish(message.getBytes("utf-8"), QOS_VALUES[0], CLEAN_START);
        } catch (Exception e) {
            Log.i("mqtt",e.getMessage());
        }
    }

    private MsgListener msgListener;

    public void setMsgListener(MsgListener listener) {
        msgListener = listener;
    }

    public interface MsgListener {
        void msgArrived(String topic, MqttMessage message);
    }

    //接收到消息
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String msg = new String(message.getPayload(), "UTF-8");
            // TODO: 2018/4/17 0017 send to mqtt service
            if (msgListener != null) {
                msgListener.msgArrived(topic, message);
            }




//            Log.e("tag_mqtt","接收的消息:"+msg);
        } catch (Exception e) {
            Log.e("mqtt",e.getMessage());
        }
    }

    //消息发送完成后发生
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
