package com.example.windqq.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.windqq.R;
import com.example.windqq.adapter.MsgAdapter;
import com.example.windqq.app.SysUtils;
import com.example.windqq.bean.Constants;
import com.example.windqq.bean.DaoCallBean;
import com.example.windqq.bean.DaoMsg;
import com.example.windqq.bean.DaoUserBean;
import com.example.windqq.event.MqttArrivedMsgEvent;
import com.example.windqq.event.MqttToSendEvent;
import com.example.windqq.service.MqttUtil;
import com.example.windqq.util.ActivityHook;
import com.example.windqq.util.PictureFileUtil;
import com.example.windqq.util.VoiceDbUtil;
import com.example.windqq.view.ChatUiHelper;
import com.example.windqq.view.CircleImagesView;
import com.example.windqq.view.RecordsButton;
import com.example.windqq.view.StateButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*????????? ????????? ???????????????(????????????????????????)*/
public class AudiosActivity extends BaseActivity {

    //message_text
    public String MESSAGE_TYPE = "";
    public static final int REQUEST_CODE_IMAGE = 1111;
    public static final int REQUEST_CODE_VEDIO = 2222;
    public static final int REQUEST_CODE_FILE = 3333;

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//??????,??????????????????
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;

    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//????????????
    @BindView(R.id.btnAudio)
    RecordsButton mBtnAudio;//????????????
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//????????????
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//????????????
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//????????????
    @BindView(R.id.common_toolbar_back)
    RelativeLayout mBack_toolbar;//????????????
    @BindView(R.id.common_toolbar_title)
    TextView commonToolbarTitle;
    @BindView(R.id.iv_user_about)
    CircleImagesView ivUserAbout;
    @BindView(R.id.rl_video)
    RelativeLayout rlVideo;
    @BindView(R.id.tv_video)
    TextView tvVideo;
    private int page = 1;
    private static final int DURATION = 15;
    private MsgAdapter adapters;
    private ChatUiHelper mUiHelper;
    private String user;
    private String code;
    private String user_name;
    private DaoMsg daoMsg;
    private String fromUser_name;
    private List<DaoMsg> wxTwentyMsg;
    private String[] users;
    private DaoMsg group_daoMsg;
    private String fromUser_ID;
    private Intent intents_group;
    private Intent intents;
    private int types;
    private Intent intent;
    private int onlines;
    private String gpName;
    private List<File> imageList = new ArrayList<>();
    private int size;

    /**
     * ????????????
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityHook.hookOrientation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        ButterKnife.bind(this);
        ivUserAbout.setVisibility(View.VISIBLE);
        EventBus.getDefault().register(this);
        intents = new Intent();
        intents.setAction("action.refreshuser");
        intents_group = new Intent();
        intents_group.setAction("action.refreshuser_group");
        /*????????????*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refreshmessage");
        registerReceiver(mRefreshBroadcastReceiver, intentFilter);
        Bundle bundle = this.getIntent().getExtras();
        user_name = bundle.getString("users");
        code = bundle.getString("codes");
        types = bundle.getInt("types", 100);
        onlines = bundle.getInt("onlins", 1);
        code.replace(",", "");
        users = new String[0];
        users = code.split(",");
        //Log.e("tag", "??????code:" + code);
        if (types == Constants.GROUP_CHAT) {
            MESSAGE_TYPE = "group_text";
            tvVideo.setText("?????????");
            //MqttService.setSubscribe();
        } else {
            MESSAGE_TYPE = "message_text";
            tvVideo.setText("??????");
        }
        //List<DaoUserBean> user = VoiceDbUtil.getInstance().getUser(code);
        //fromUser_name = user.get(1).getUser();
        initContent();
        initData();
        toBottom();
        initNATiVI();
    }

    public void setMessageType(String TYPE) {
        MESSAGE_TYPE = TYPE;
    }

    private void initData() {
        if (user_name == null) {
            Intent intent = getIntent();
            user = intent.getStringExtra("users");
            code = intent.getStringExtra("codes");
            if (user == null) {
                commonToolbarTitle.setText(code);
            }
        } else {
            commonToolbarTitle.setText(user_name);
        }
        List<DaoUserBean> userbean = VoiceDbUtil.getInstance().getUser(SysUtils.getDeviceId());
        if (userbean.size() > 1) {
            fromUser_name = userbean.get(1).getUser();
        } else {
            fromUser_name = SysUtils.getDeviceId();
        }
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ++page;
                loadData(page);
                //??????
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    // broadcast receiver
    private BroadcastReceiver mRefreshBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.refreshmessage")) {
//                initData();
//                addData();
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MqttArrivedMsgEvent messageEvent) {
        analyzeMqttMsg(messageEvent.getMsg());
    }

    private void analyzeMqttMsg(String message) {
        try {
            JSONObject msgJson = JSONObject.parseObject(message);
            String type = msgJson.getString("type");
            /*??????*/
            if (type.equals("message_text")) {
                String content = msgJson.getString("content");
                String userid = msgJson.getString("userId");
//                Log.e("Tag", "Audios????????????Audios:" + userid);
//                Log.e("Tag", "Audios????????????:" + content);

                if (code.equals(userid)) {
                    daoMsg = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 1, content);
                    addData();
                }
            }

            /*??????*/
            if (type.equals("group_text")) {
                String content = msgJson.getString("content");
                String group_userid = msgJson.getString("userId");
                String group_name = msgJson.getString("username");
                List<DaoUserBean> userbean = VoiceDbUtil.getInstance().getUserID(group_name);
                fromUser_ID = userbean.get(1).getUserId();
                if (code.equals(group_userid)) {
                    if (fromUser_ID.equals(SysUtils.getDeviceId())) {
//                        Log.e("tag", "?????????????????? ??????");
                    } else {
                        group_daoMsg = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, group_name, 1, content);
                        addData();
//                        Log.e("tag", "????????????code");
                    }
                } else {
                    Log.e("tag", "?????????");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*?????????????????????????????????*/
                if (MESSAGE_TYPE.equals("group_text")) {
                    if (group_daoMsg != null) {
                        adapters.addData(group_daoMsg);
                        adapters.notifyDataSetChanged();
                        toBottom();
                    }
                }
                /*????????????????????????*/
                if (MESSAGE_TYPE.equals("message_text")) {
                    if (daoMsg != null) {
                        adapters.addData(daoMsg);
                        adapters.notifyDataSetChanged();
                        toBottom();
                    }
                }
            }
        });
    }

    /*????????????*/
    private void initContent() {
        ButterKnife.bind(this);
        adapters = new MsgAdapter(this, null, DURATION);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(adapters);
        adapters.setOnItemCliclListener(new MsgAdapter.OnItemCliclListener() {
            @Override
            public void onItemClick(int position) {
//                Intent intent = new Intent(AudiosActivity.this, UserAboutActivity.class);
//                intent.putExtra("users", user_name);
//                intent.putExtra("codes", code);
//                intent.putExtra("groupName", commonToolbarTitle.getText().toString());
//                if (types == Constant.GROUP_CHAT) {
//                    intent.putExtra("types", Constant.GROUP_CHAT);
//                } else {
//                    intent.putExtra("types", Constant.SINGLE_CHAT);
//                }
//                startActivity(intent);
            }
        });
        initChatUi();
        toBottom();
        mBack_toolbar.setOnClickListener(backClick);
        ivUserAbout.setOnClickListener(useraboutActivityClick);
        page = 1;
        loadData(page);
        toBottom();
    }

    /*??????????????????*/
    private void initSend() {
        if (MESSAGE_TYPE.equals("message_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, mEtContent.getText().toString());
            String sendMessage = MqttUtil.getSendMessage(MESSAGE_TYPE, mEtContent.getText().toString(), SysUtils.getDeviceId(), fromUser_name);
            boolean ok = sendMessage.equals("ok");
            Log.e("tag", "???????????????:" + sendMessage + "," + "??????????????????:" + ok);
            //????????????
            Log.e("tag", "????????????code:" + "message/" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + code, sendMessage));

            //Log.e("tag", "sendMessage " + sendMessage);
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);
            //????????????beanDao
            DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???: " + mEtContent.getText().toString(), Constants.SINGLE_CHAT, System.currentTimeMillis());
            VoiceDbUtil.getInstance().insertUser(daoCallBean);
            //Log.e("tag", "audios????????????" + daoCallBean.getContent());
            Log.e("tag", "audio?????????:" + daoCallBean.getContent());
            toBottom();
            sendBroadcast(intents);
        }

        if (MESSAGE_TYPE.equals("group_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, mEtContent.getText().toString());
            String sendMessage = MqttUtil.getSendGroupMessage(MESSAGE_TYPE, user_name, mEtContent.getText().toString(), code, fromUser_name);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + MESSAGE_TYPE + "/" + code, sendMessage));
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);
            //??????????????????beanDao
            DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???: " + mEtContent.getText().toString(), Constants.GROUP_CHAT, System.currentTimeMillis());
            VoiceDbUtil.getInstance().insertUser(daoCallBean);
            Log.e("tag", "Group?????????:" + daoCallBean.getContent());
            toBottom();
            sendBroadcast(intents_group);
        }
    }

    //???????????????
    private void toBottom() {
        if (adapters.getItemCount() > 0) {
            mRvChat.scrollToPosition(adapters.getItemCount() - 1);
        }
    }

    private void loadData(int page) {
        wxTwentyMsg = VoiceDbUtil.getInstance().getWXTwentyMsg(page, SysUtils.getDeviceId(), code);
        if (wxTwentyMsg != null) {
            //Log.e("tagdao", "?????????:" + wxTwentyMsg.size());
            if (wxTwentyMsg.size() > 0) {
                adapters.loadMore(wxTwentyMsg);
            } else {
                if (page > 2) {
                    Log.e("tag", "page???" + page);
                    Toast.makeText(this, "??????????????????,????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private View.OnClickListener useraboutActivityClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (types == Constants.GROUP_CHAT) {
//                Intent intent = new Intent(AudiosActivity.this, UserAboutActivity.class);
//                intent.putExtra("users", user_name);
//                intent.putExtra("codes", code);
//                intent.putExtra("userSize", users.length);
//                intent.putExtra("groupName", commonToolbarTitle.getText().toString());
//                intent.putExtra("types", Constant.GROUP_CHAT);
//                intent.putExtra("onlins", 1);
//                startActivity(intent);
            } else {
//                Intent intent = new Intent(AudiosActivity.this, UserAboutActivity.class);
//                intent.putExtra("users", user_name);
//                intent.putExtra("codes", code);
//                intent.putExtra("types", Constants.SINGLE_CHAT);
//                startActivity(intent);
            }
        }
    };

    private View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUiHelper.hideBottomLayout(false);
            mUiHelper.bindEmojiData();
            mUiHelper.hideSoftInput();
            mEtContent.clearFocus();
            finish();
        }
    };

    /*??????UI*/
    private void initChatUi() {
        //mBtnAudio
        mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)

                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio)
                .bindEmojiData();

        //??????????????????,??????????????????
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (adapters.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(adapters.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });

        ((RecordsButton) mBtnAudio).setOnFinishedRecordListener(new RecordsButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                Log.d("tag", "??????????????????");
                File file = new File(audioPath);
                if (file.exists()) {
                    if (MESSAGE_TYPE.equals("message_text")) {
                        DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "??????????????????" + time + "S" + "\r\n" + " ??????:" + audioPath);
                        String sendMessage = MqttUtil.getSendMessage(MESSAGE_TYPE, "??????????????????" + time + "S" + "\r\n" + " ??????:" + audioPath, SysUtils.getDeviceId(), fromUser_name);
                        //????????????
                        //Log.e("tag", "????????????code:" + "message/" + code);
                        EventBus.getDefault().post(new MqttToSendEvent("message/" + code, sendMessage));
                        //Log.e("tag", "sendMessage " + sendMessage);
                        //???????????????
                        VoiceDbUtil.getInstance().insert(dao);
                        adapters.addData(dao);
                        //????????????beanDao
                        DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???: " + "??????????????????" + time + "S" + "\r\n" + " ??????:" + audioPath, Constants.SINGLE_CHAT, System.currentTimeMillis());
                        VoiceDbUtil.getInstance().insertUser(daoCallBean);
                        //Log.e("tag", "audios????????????" + daoCallBean.getContent());
                        toBottom();
                        sendBroadcast(intents);
                    }

                    if (MESSAGE_TYPE.equals("group_text")) {
                        DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "??????????????????" + time + "S" + "\r\n" + " ??????:" + audioPath);
                        String sendMessage = MqttUtil.getSendGroupMessage(MESSAGE_TYPE, user_name, "??????????????????" + time + "S" + "\r\n" + " ??????:" + audioPath, code, fromUser_name);
                        EventBus.getDefault().post(new MqttToSendEvent("message/" + MESSAGE_TYPE + "/" + code, sendMessage));
                        //Log.e("tag_id", "????????????code:" + "message/" + MESSAGE_TYPE);
                        //???????????????
                        VoiceDbUtil.getInstance().insert(dao);
                        adapters.addData(dao);
                        //??????????????????beanDao
                        //DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getInstance().getDeviceId(), "???: " + "??????????????????" + time + "S", Constant.GROUP_CHAT, System.currentTimeMillis());
                        //VoiceDbUtil.getInstance().insertUser(daoCallBean);
                        //Log.e("tag", "group????????????" + daoCallBean.getContent());
                        toBottom();
                        sendBroadcast(intents_group);
                    }
                }
            }
        });

        //??????????????????????????????
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });
    }

    @OnClick({ R.id.rlPhoto, R.id.rlVideo, R.id.rlFile, R.id.rl_video, R.id.rl_voice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
//            case R.id.btn_send:
//                initSend();
//                mEtContent.setText("");
//                break;
            /*????????????*/
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(AudiosActivity.this, REQUEST_CODE_IMAGE);
                break;
            /*??????????????????*/
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(AudiosActivity.this, REQUEST_CODE_VEDIO);
                break;

            /*????????????*/
            /*action*/
            /* 0 ---??????????????????
               1 ---????????????????????????
               2 ---??????????????????
               6 ---??????????????????
               3 ---??????????????????
            */

            case R.id.rl_voice:
                if (types == Constants.GROUP_CHAT) {
                    /*?????????*/
                    String last_name = commonToolbarTitle.getText().toString();
//                    String sendAudio = MqttUtil.getAudio_Group(Constants.MQTT_AUDIO_GROUP, last_name, SysUtils.getDeviceId(), code, 0);
//                    EventBus.getDefault().post(new MqttToSendEvent("group/audio/message/" + code, sendAudio));
//                    Intent intent = new Intent(AudiosActivity.this, Audio_ChatActivity.class);
//                    intent.putExtra("FromCode", SysUtils.getDeviceId());
//                    intent.putExtra("type", "send");
//                    intent.putExtra("AllCode", code);
//                    startActivity(intent);
                } else if (types == Constants.SINGLE_CHAT) {
                    if (code.equals(SysUtils.getDeviceId())) {
                        Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
//                        if (onlines == 1) {
//                            String sendAudio = MqttUtil.getAudio(Constant.MQTT_AUDIO_SINGLE, SysUtils.getInstance().getDeviceId(), code, 0);
//                            startService(new Intent(AudiosActivity.this, SocketService.class));
//                            /*????????????*/
//                            EventBus.getDefault().post(new MqttToSendEvent("audio/client", sendAudio));
//                            Intent intent = new Intent(AudiosActivity.this, Audio_MainActivity.class);
//                            intent.putExtra("FromCode", code);
//                            intent.putExtra("type", "send");
//                            startActivity(intent);
//                        } else {
//                            Toast.makeText(AudiosActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
//                        }
                    }
                }
                break;
//            /*??????*/
//            case R.id.rl_video:
//                if (ActivityUtil.isServiceWork(this, "com.demon.suspensionbox.FloatingService")) {//??????????????????
//                    Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (!Settings.canDrawOverlays(this)) {
//                        Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
//                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
//                    } else {
//                        if (types == Constant.GROUP_CHAT) {
//                            /*?????????*/
//                            Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
//                            String sendMessage = MqttUtil.getSendGroupVideo("group_video", commonToolbarTitle.getText().toString(), code, SysUtils.getInstance().getDeviceId(), 0, users.length);
//                            EventBus.getDefault().post(new MqttToSendEvent("group/video/message/" + code, sendMessage));
////                          Log.e("tag_video", "?????????:" + "message/" + sendMessage);
//                            this.intent = new Intent(AudiosActivity.this, Group_ChatActivity.class);
//                            this.intent.putExtra("action", 0);
//                            this.intent.putExtra("video_user", SysUtils.getInstance().getDeviceId());
//                            this.intent.putExtra("groupUserId", code);
//                            this.intent.putExtra("groupName", commonToolbarTitle.getText().toString());
//                            this.intent.putExtra("userSize", users.length);
//                            this.intent.putExtra("toId", code);
//                            startActivity(this.intent);
//                        } else {
//                            /*????????????*/
//                            /*????????????????????????????????????*/
//                            intent = new Intent(AudiosActivity.this, MainActivity_Tk.class);
//                            intent.putExtra("action", 0);
//                            intent.putExtra("toId", code);
//                            if (onlines == 1) {
//                                initVideo();
//                                startActivity(intent);
//                                AudiosActivity.this.finish();
//                            } else {
//                                Toast.makeText(AudiosActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
////                                Log.e("tag", "???????????????");
//                            }
//                        }
//                    }
//                }
//                break;
            /*????????????*/
            case R.id.rlFile:
//                PictureFileUtil.openFile(AudiosActivity.this, REQUEST_CODE_FILE);
                break;
        }
    }

    /*??????????????????*/
    public void saveToSystemGallery(Bitmap bmp) {
        // ??????????????????
        File fileDir = new File(Environment.getExternalStorageDirectory(), Constants.SAVE_IMG_PATH);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(fileDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ????????????????????????????????????
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), fileName, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // ????????????????????????
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
        //????????????????????????????????????
        //Log.e("tag", "?????????????????? ??????:" + file.getAbsolutePath());
    }

    /*????????????*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
//                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
////                    Log.d("tag", "????????????????????????:" + filePath);
//                    sendFileMessage(filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    //????????????????????????
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
//                        Log.e("tag", "????????????????????????:" + media.getPath());
                        imageList.add(new File(media.getPath()));
                        size = imageList.size();
                        sendImageMessage(size, media);
                    }
//                    Log.e("tag", "???????????????:" + size + "?????????");
                    break;
                case REQUEST_CODE_VEDIO:
                    //????????????????????????
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
//                        Log.d("tag", "????????????????????????:" + media.getPath());
                        sendVedioMessage(media);
                    }
                    break;
            }
        }
    }

    /*??????????????????*/
    private void sendVedioMessage(LocalMedia media) {
        if (MESSAGE_TYPE.equals("message_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + media.getPath());
            String sendMessage = MqttUtil.getSendMessage(MESSAGE_TYPE, "????????????" + "   " + media.getPath(), SysUtils.getDeviceId(), fromUser_name);
            //????????????
//            Log.e("tag", "????????????code:" + "message/" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + code, sendMessage));
//            Log.e("tag", "sendMessage:" + sendMessage);
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);

            //????????????beanDao
            DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???: " + "????????????" + "   " + media.getPath(), Constants.SINGLE_CHAT, System.currentTimeMillis());
            VoiceDbUtil.getInstance().insertUser(daoCallBean);
//            Log.e("tag", "audios????????????" + daoCallBean.getContent());
            toBottom();
            sendBroadcast(intents);
        }

        if (MESSAGE_TYPE.equals("group_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + media.getPath());
            String sendGroupMessage = MqttUtil.getSendGroupMessage(MESSAGE_TYPE, user_name, "????????????" + "   " + media.getPath(), code, fromUser_name);
//            Log.e("tag_id", "????????????ID???:" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + MESSAGE_TYPE + "/" + code, sendGroupMessage));
//            Log.e("tag_id", "????????????code:" + "message/" + MESSAGE_TYPE);
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);
            toBottom();
            sendBroadcast(intents_group);
        }

        Toast.makeText(this, "??????????????????,Path???:" + "\r\n" + media.getPath(), Toast.LENGTH_SHORT).show();
    }

    /*??????????????????*/
    private void sendFileMessage(String filePath) {
        if (MESSAGE_TYPE.equals("message_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + filePath);
            String sendMessage = MqttUtil.getSendMessage(MESSAGE_TYPE, "????????????" + "   " + filePath, SysUtils.getDeviceId(), fromUser_name);
            //????????????
//            Log.e("tag", "????????????code:" + "message/" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + code, sendMessage));
//            Log.e("tag", "sendMessage " + sendMessage);
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);
            //????????????beanDao
            DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???: " + "????????????" + "   " + filePath, Constants.SINGLE_CHAT, System.currentTimeMillis());
            VoiceDbUtil.getInstance().insertUser(daoCallBean);
//            Log.e("tag", "audios????????????" + daoCallBean.getContent());
            toBottom();
            sendBroadcast(intents);
        }

        if (MESSAGE_TYPE.equals("group_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + filePath);
            String sendMessage = MqttUtil.getSendGroupMessage(MESSAGE_TYPE, user_name, "????????????" + "   " + filePath, code, fromUser_name);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + MESSAGE_TYPE + "/" + code, sendMessage));
//            Log.e("tag_id", "????????????code:" + "message/" + MESSAGE_TYPE);
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);
            toBottom();
            sendBroadcast(intents_group);
        }
    }

    //??????????????????
    private void sendImageMessage(final int Size, final LocalMedia media) {
        if (MESSAGE_TYPE.equals("message_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + media.getPath());
            String sendMessage = MqttUtil.getSendMessage(MESSAGE_TYPE, "????????????" + "   " + media.getPath(), SysUtils.getDeviceId(), fromUser_name);
            //????????????
//            Log.e("tag", "????????????code:" + "message/" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + code, sendMessage));
//            Log.e("tag", "sendMessage " + sendMessage);
            //???????????????
            VoiceDbUtil.getInstance().insert(dao);
            adapters.addData(dao);

            //????????????beanDao
            DaoCallBean daoCallBean = new DaoCallBean(null, commonToolbarTitle.getText().toString(), code, SysUtils.getDeviceId(), "???:" + "????????????" + "   " + media.getPath(), Constants.SINGLE_CHAT, System.currentTimeMillis());
            VoiceDbUtil.getInstance().insertUser(daoCallBean);
//            Log.e("tag", "audios????????????" + daoCallBean.getContent());
            toBottom();
            sendBroadcast(intents);
        }

        if (MESSAGE_TYPE.equals("group_text")) {
            DaoMsg dao = new DaoMsg(null, System.currentTimeMillis(), SysUtils.getDeviceId(), code, user_name, 0, "????????????" + "   " + media.getPath());
            String sendMessage = MqttUtil.getSendGroupMessage(MESSAGE_TYPE, user_name, "????????????" + "   " + media.getPath(), code, fromUser_name);
//            Log.e("tag_id", "????????????ID???:" + code);
            EventBus.getDefault().post(new MqttToSendEvent("message/" + MESSAGE_TYPE + "/" + code, sendMessage));
//            Log.e("tag_id", "????????????code:" + "message/" + MESSAGE_TYPE);
            //???????????????
            boolean insert = VoiceDbUtil.getInstance().insert(dao);
            if (insert) {
                Log.e("tag", "?????????????????????");
            }
            adapters.addData(dao);
            toBottom();
            sendBroadcast(intents_group);
        }
    }

    private void initVideo() {
        List<DaoUserBean> fromuser = VoiceDbUtil.getInstance().getUser(SysUtils.getDeviceId());
        if (fromuser.size() > 1) {
            gpName = fromuser.get(1).getUser();
        } else {
            gpName = "??????";
        }

        String video = MqttUtil.getVideo("video_message", SysUtils.getDeviceId(), gpName, 0);
        EventBus.getDefault().post(new MqttToSendEvent("video/message" + code, video));
//        Log.e("tag", "video/message" + code);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unregisterReceiver(mRefreshBroadcastReceiver);
        finish();
    }

    private void initNATiVI() {
        //???????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
        }
    }
}
