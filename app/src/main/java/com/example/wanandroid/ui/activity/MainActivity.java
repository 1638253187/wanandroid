package com.example.wanandroid.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wanandroid.R;
import com.example.wanandroid.adapter.PopAdapter;
import com.example.wanandroid.bean.ChangBean;
import com.example.wanandroid.bean.ShouBean;
import com.example.wanandroid.model.ImpChangModel;
import com.example.wanandroid.model.ImpShouModel;
import com.example.wanandroid.presenter.ImpChangPresenter;
import com.example.wanandroid.presenter.ImpShouPresenter;
import com.example.wanandroid.ui.fragment.DaoHangFragment;
import com.example.wanandroid.ui.fragment.GongZongFragment;
import com.example.wanandroid.ui.fragment.HomeFragment;
import com.example.wanandroid.ui.fragment.ScrollableViewPager;
import com.example.wanandroid.ui.fragment.SetiingFragment;
import com.example.wanandroid.ui.fragment.TixiFragment;
import com.example.wanandroid.ui.fragment.XiangMuFragment;
import com.example.wanandroid.util.AnimatorUtil;
import com.example.wanandroid.util.Constants;
import com.example.wanandroid.util.ToastUtil;
import com.example.wanandroid.util.night.SpUtil;
import com.example.wanandroid.util.viewpagerutil.FlowLayout;
import com.example.wanandroid.view.ChangView;
import com.example.wanandroid.view.ShouView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ChangView {
    public static final int TYPE_SETTTINGS = 6;
    private static final int ITEM_DISTANCE = 10;
    private long exitTime = 0;
    private Toolbar mToolbar;
    public String mType;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle toggle;
    private ArrayList<Fragment> fragments;
    private ScrollableViewPager mVp;
    private BottomNavigationView mBnv;
    private FloatingActionButton mFab;
    private TextView tv_tile;
    private ImageView mIvUsage;
    private ImageView mIvSearch;
    private PopupWindow popupWindow3;
    private RecyclerView recy;
    private ArrayList<ChangBean.DataBean> list;
    private PopAdapter adapter;
    private View inflate3;
    private Button btn_search;
    private ArrayList<ShouBean.DataBean> list_liu;
    private FlowLayout liushi1;
    private FlowLayout liushi2;
    private TextView tv_clear;
    private ImageView iv_clear;
    private EditText et_search;
    private TextView lable;
    private String shuju;
    private String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        initView ();
        list.clear ();
    }

    private void initData() {
        list.clear ();
        ImpChangPresenter impChangPresenter = new ImpChangPresenter (new ImpChangModel (), this);
        impChangPresenter.getChang ();
        ImpShouPresenter impShouPresenter = new ImpShouPresenter (new ImpShouModel (), new ShouView () {
            @Override
            public void onSuccess(List<ShouBean.DataBean> shouBeans) {
                list_liu.addAll (shouBeans);
            }

            @Override
            public void onFail(String error) {
                ToastUtil.showShort (error);
            }
        });
        impShouPresenter.getShou ();
    }

    private void getInintevent(final TextView textView) {
        textView.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                shuju = textView.getText ().toString ();
                et_search.setText (shuju);
                lable = (TextView) LayoutInflater.from (MainActivity.this).inflate (R.layout.item_label, null);
                lable.setText (shuju);
//                        textView.setBackgroundResource(R.drawable.shape_orj);
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins (15, 15, 15, 15);
                liushi1.addView (lable, params);
                tv_clear.setTextColor (Color.BLACK);
                iv_clear.setImageResource (R.drawable.ic_clear_all);
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void initView() {
        mToolbar = (Toolbar) findViewById (R.id.toolbar);
        mBnv = (BottomNavigationView) findViewById (R.id.bnv);
        mVp = (ScrollableViewPager) findViewById (R.id.vp);
        mNavigationView = (NavigationView) findViewById (R.id.navigation_view);
        mDrawer = (DrawerLayout) findViewById (R.id.drawer);
        mFab = (FloatingActionButton) findViewById (R.id.fab);
        mIvUsage = (ImageView) findViewById (R.id.iv_usage);
        mIvSearch = (ImageView) findViewById (R.id.iv_search);
        list = new ArrayList<ChangBean.DataBean> ();
        //??????????????????????????????
        mToolbar.setTitle (" ");
        tv_tile = mToolbar.findViewById (R.id.tv_title);
        mFab.setBackgroundTintList (ColorStateList.valueOf ((R.color.colorGreen)));
        mFab.setBackgroundTintList (ColorStateList.valueOf (Color.parseColor ("#009688")));
        setSupportActionBar (mToolbar);
        initDrawerLayout ();
        initNaviLayout ();
        initVp ();
        initLinster ();
        list.clear ();
        list_liu = new ArrayList<> ();
        initData ();
    }


    private void initLinster() {
        mIvSearch.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Window window = getWindow ();
                WindowManager.LayoutParams lp = window.getAttributes ();
                lp.alpha = 0.4f;
                window.setAttributes (lp);
                View inflate3 = LayoutInflater.from (MainActivity.this).inflate (R.layout.pop_search, null);
                ImageView iv_back = inflate3.findViewById (R.id.iv_back);
                liushi1 = inflate3.findViewById (R.id.liushi1);
                liushi2 = inflate3.findViewById (R.id.liushi);
                tv_clear = inflate3.findViewById (R.id.tv_clear);
                iv_clear = inflate3.findViewById (R.id.iv_clear);
                et_search = inflate3.findViewById (R.id.et_search);
                btn_search = inflate3.findViewById (R.id.btn_sphar);
                tv_clear.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        liushi1.removeAllViews ();
                        tv_clear.setTextColor (Color.GRAY);
                        iv_clear.setImageResource (R.drawable.ic_clear_all_gone);
                    }
                });

                iv_clear.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        liushi1.removeAllViews ();
                        tv_clear.setTextColor (Color.GRAY);
                        iv_clear.setImageResource (R.drawable.ic_clear_all_gone);
                    }
                });


                btn_search.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        s = et_search.getText ().toString ().trim ();
                        if (s.isEmpty ()) {
                            ToastUtil.showShort ("?????????????????????");
                        } else {
//                        TextView textView = new TextView(MainActivity.this);
                            lable = (TextView) LayoutInflater.from (MainActivity.this).inflate (R.layout.item_label, null);
                            lable.setText (s);
//                        textView.setBackgroundResource(R.drawable.shape_orj);
                            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT
                                    , ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins (15, 15, 15, 15);
                            liushi1.addView (lable, params);
                        }
                    }
                });

                for (int i = 0; i < list_liu.size (); i++) {
                    TextView lable = (TextView) LayoutInflater.from (MainActivity.this).inflate (R.layout.item_label, null);
                    ShouBean.DataBean shouBean = list_liu.get (i);
                    lable.setText (shouBean.getName ());
                    // textView.setBackgroundResource(R.drawable.shape);
                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams (ViewGroup.LayoutParams.WRAP_CONTENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins (15, 15, 5, 15);
                    final int finalI = i;
                    getInintevent (lable);
                    liushi2.addView (lable, params);
                }

                iv_back.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        popupWindow3.dismiss ();
                    }
                });
                popupWindow3 = new PopupWindow (inflate3, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                popupWindow3.setBackgroundDrawable (new BitmapDrawable ());
//                popupWindow3.setOutsideTouchable (true);
                popupWindow3.setFocusable (true);
                                                /*
                                                    2.??????????????????style ??????,????????????????????????
                                                     <style name="popAnimation" parent="@android:style/Animation">
                                                        <item name="android:windowEnterAnimation">@anim/pop_enter</item>
                                                        <item name="android:windowExitAnimation">@anim/pop_exit</item>
                                                    </style>
                                                 */
                popupWindow3.setAnimationStyle (R.style.popAnimation);

                popupWindow3.showAtLocation (mVp, Gravity.TOP, 0, 100);
                popupWindow3.setOnDismissListener (new PopupWindow.OnDismissListener () {
                    @Override
                    public void onDismiss() {
                        Window window = getWindow ();
                        WindowManager.LayoutParams lp = window.getAttributes ();
                        lp.alpha = 1.0f;
                        window.setAttributes (lp);
                    }
                });
            }
        });

        mIvUsage.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Window window = getWindow ();
                WindowManager.LayoutParams lp = window.getAttributes ();
                lp.alpha = 0.4f;
                window.setAttributes (lp);
                inflate3 = LayoutInflater.from (MainActivity.this).inflate (R.layout.pop_usage, null);
                recy = inflate3.findViewById (R.id.recy);
                adapter = new PopAdapter (MainActivity.this, list);
                recy.setLayoutManager (new LinearLayoutManager (MainActivity.this));
                recy.addOnScrollListener (new RecyclerView.OnScrollListener () {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged (recyclerView, newState);
                        LinearLayoutManager layoutManager = new LinearLayoutManager (MainActivity.this) {
                            @Override
                            public boolean canScrollVertically() {
                                return false;
                            }
                        };
                        recyclerView.setLayoutManager (layoutManager);
                    }
                });
                recy.setAdapter (adapter);
                ImageView iv_back = inflate3.findViewById (R.id.iv_back);
                iv_back.setOnClickListener (new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        popupWindow3.dismiss ();
                    }
                });
                popupWindow3 = new PopupWindow (inflate3, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                popupWindow3.setBackgroundDrawable (new BitmapDrawable ());
//                popupWindow3.setOutsideTouchable (true);
                //?????????????????????pop
                popupWindow3.setFocusable (true);
                                                /*
                                                    2.??????????????????style ??????,????????????????????????
                                                     <style name="popAnimation" parent="@android:style/Animation">
                                                        <item name="android:windowEnterAnimation">@anim/pop_enter</item>
                                                        <item name="android:windowExitAnimation">@anim/pop_exit</item>
                                                    </style>
                                                 */
                popupWindow3.setAnimationStyle (R.style.popAnimation);

                popupWindow3.showAtLocation (mVp, Gravity.TOP, 0, 130);
                popupWindow3.setOnDismissListener (new PopupWindow.OnDismissListener () {
                    @Override
                    public void onDismiss() {
                        Window window = getWindow ();
                        WindowManager.LayoutParams lp = window.getAttributes ();
                        lp.alpha = 1.0f;
                        window.setAttributes (lp);
                    }
                });
            }
        });

    }

    //view??????????????????????????????view?????????????????????????????????????????????????????? ?????????????????????
    private AnimatorSet doAnimOpenItem(View view, int index) {
        float distance = index * ITEM_DISTANCE;
        AnimatorSet animatorSet = new AnimatorSet ();                         //?????????
        animatorSet.play (                                                   //0
                ObjectAnimator.ofFloat (view, "alpha", 10, 10))
                .with (ObjectAnimator.ofFloat (view, "scaleX", 1, 1))//??????
                .with (ObjectAnimator.ofFloat (view, "scaleY", 1, 1))//??????
                .after (ObjectAnimator.ofFloat (view, "translationY", 520, -390, distance));//??????????????????
        //??????????????????????????????????????????????????????????????? ????????????????????????????????????????????????????????????
        animatorSet.setStartDelay (index * 1);
        animatorSet.setDuration (2200);//??????????????????
        return animatorSet;
    }

    private void initVp() {
        fragments = new ArrayList<> ();
        fragments.add (new HomeFragment ());
        fragments.add (new TixiFragment ());
        fragments.add (new GongZongFragment ());
        fragments.add (new XiangMuFragment ());
        fragments.add (new DaoHangFragment ());
        fragments.add (new SetiingFragment ());

//        mVp.setPageTransformer (true, new RotationPageTransformer ());
        mVp.setScanScroll (false);
        mVp.setNoScroll (false);
        mVp.setCurrentItem (1);
        mVp.setAdapter (new FragmentPagerAdapter (getSupportFragmentManager ()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get (i);
            }

            @Override
            public int getCount() {
                return fragments.size ();
            }
        });

        mVp.addOnPageChangeListener (new ViewPager.OnPageChangeListener () {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        mBnv.getMenu ().getItem (0).setChecked (true);
                        AnimatorUtil.translateShow (mBnv, null);
                        mBnv.setVisibility (View.VISIBLE);
                        AnimatorUtil.translateShow (mToolbar, null);
                        mToolbar.setVisibility (View.VISIBLE);
                        break;
                    case 1:
                        mBnv.getMenu ().getItem (1).setChecked (true);
                        break;
                    case 2:
                        mBnv.getMenu ().getItem (2).setChecked (true);
                        break;
                    case 3:
                        mBnv.getMenu ().getItem (3).setChecked (true);
                        break;
                    case 4:
                        mBnv.getMenu ().getItem (4).setChecked (true);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        // ???bnv????????????????????????
        mBnv.setOnNavigationItemSelectedListener (new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.home:
                        // ????????????????????????
                        mVp.setCurrentItem (0);
                        tv_tile.setText ("??????");
                        tv_tile.setTranslationX (0);
                        doAnimOpenItem (mFab, 1).start ();
                        break;
                    case R.id.guide:
                        mVp.setCurrentItem (1);
                        tv_tile.setText ("????????????");
                        tv_tile.setTranslationX (-50);
                        doAnimOpenItem (mFab, 1).start ();
                        break;
                    case R.id.msg:
                        mVp.setCurrentItem (2);
                        tv_tile.setText ("?????????");
                        tv_tile.setTranslationX (-15);
                        doAnimOpenItem (mFab, 1).start ();
                        break;
                    case R.id.person:
                        mVp.setCurrentItem (3);
                        tv_tile.setText ("??????");
                        tv_tile.setTranslationX (0);
                        doAnimOpenItem (mFab, 1).start ();
                        break;
                    case R.id.sett:
                        mVp.setCurrentItem (4);
                        tv_tile.setTranslationX (0);
                        tv_tile.setText ("??????");
                        doAnimOpenItem (mFab, 1).start ();
                        break;
                }
                // ??????????????????true????????????????????????
                return true;
            }
        });
    }


    private void initNaviLayout() {
        mNavigationView.setNavigationItemSelectedListener (new NavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                menuItem.setChecked (true);
                switch (menuItem.getItemId ()) {
                    case R.id.item_wan:
                        mDrawer.closeDrawer (Gravity.LEFT);
                        mVp.setCurrentItem (0);
                        doAnimOpenItem (mFab, 1).start ();
                        mBnv.setVisibility (View.VISIBLE);
                        break;
                    case R.id.item_shouchang:
                        SharedPreferences sp = getSharedPreferences ("mima", 0);
                        boolean flag = sp.getBoolean ("flag", false);
                        if (flag == true) {
                            String names = sp.getString ("names", "");
                            Toast.makeText (MainActivity.this, "??????:" + "\r\n" + "   " + names + "\r\n" + "      ??????", Toast.LENGTH_SHORT).show ();
                            startActivity (new Intent (MainActivity.this, Like.class));
                        } else {
                            Toast.makeText (MainActivity.this, "?????????", Toast.LENGTH_SHORT).show ();
                            startActivity (new Intent (MainActivity.this, Hello.class));
                        }
                        break;
                    case R.id.item_shezhi:
                        mDrawer.closeDrawer (Gravity.LEFT);
                        mVp.setCurrentItem (5);
                        doAnimOpenItem (mFab, 1).start ();
                        mBnv.setVisibility (View.GONE);
                        break;
                    case R.id.item_about:
                        startActivity (new Intent (MainActivity.this, About.class));
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onSuccess(List<ChangBean.DataBean> dataBeans) {
        list.clear ();
        list.addAll (dataBeans);
    }

    @Override
    public void onFail(String error) {
        ToastUtil.showShort (error);
    }


    public class RotationPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;

        @Override
        public void transformPage(View page, float position) {
            float scaleFactor = Math.max (MIN_SCALE, 1 - Math.abs (position));
            float rotate = 10 * Math.abs (position);
            //position????????????1??????????????????page??????????????????item???????????????
            //????????????????????????????????????????????????????????????
            if (position <= -1) {
                page.setScaleX (MIN_SCALE);
                page.setScaleY (MIN_SCALE);
                page.setRotationY (rotate);
            } else if (position < 0) {//position???0?????????-1???page??????????????????
                page.setScaleX (scaleFactor);
                page.setScaleY (scaleFactor);
                page.setRotationY (rotate);
            } else if (position >= 0 && position < 1) {//position???0?????????1???page??????????????????
                page.setScaleX (scaleFactor);
                page.setScaleY (scaleFactor);
                page.setRotationY (-rotate);
            } else if (position >= 1) {//position????????????1??????????????????page??????????????????item????????????
                page.setScaleX (scaleFactor);
                page.setScaleY (scaleFactor);
                page.setRotationY (-rotate);
            }
        }
    }

    public static final float SCALE_MAX = 1.2F;
    public static final float SCALE_NOMAL = 1f;

    private void setButtonScale(RadioButton controlRadioButton, float scaleType) {
        controlRadioButton.setScaleX (scaleType);
        controlRadioButton.setScaleY (scaleType);
    }

    public void resetScale(RadioButton bt1, RadioButton bt2, RadioButton bt3, RadioButton bt4) {
        bt1.setScaleX (SCALE_NOMAL);
        bt1.setScaleY (SCALE_NOMAL);
        bt2.setScaleX (SCALE_NOMAL);
        bt2.setScaleY (SCALE_NOMAL);
        bt3.setScaleX (SCALE_NOMAL);
        bt3.setScaleY (SCALE_NOMAL);
        bt4.setScaleX (SCALE_NOMAL);
        bt4.setScaleY (SCALE_NOMAL);

    }

    private void initDrawerLayout() {
        toggle = new ActionBarDrawerToggle (this, mDrawer, mToolbar, R.string.navi_drawer_open, R.string.navi_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //??????mDrawerLayout????????????????????????????????????????????????RelativeLayout
                //???????????????view
                View mContent = mDrawer.getChildAt (0);
                float scale = 1 - slideOffset;
                float endScale = 0.8f + scale * 0.2f;
                float startScale = 1 - 0.3f * scale;
                //????????????????????????????????????????????????
                drawerView.setScaleX (startScale);
                drawerView.setScaleY (startScale);
                //?????????????????????
                drawerView.setAlpha (0.6f + 0.4f * (1 - scale));
                //????????????????????????????????????????????????
                //???????????????????????????????????? ??????????????????????????????????????????
                mContent.setTranslationX (drawerView.getMeasuredWidth () * (1 - scale));
                //??????????????????????????????????????????button?????????????????????
                mContent.invalidate ();
                //????????????????????????????????????????????????
                mContent.setScaleX (endScale);
                mContent.setScaleY (endScale);
            }
        };
        toggle.syncState ();
        mDrawer.addDrawerListener (toggle);
    }


    public class CustomPageTransformer implements ViewPager.PageTransformer {
        //??????????????????Size?????????90%
        private static final float MIN_SCALE = 0.9F;

        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void transformPage(View view, float position) {
            //?????????????????????
            float scale = Math.max (MIN_SCALE, 1 - Math.abs (position));
            if (position < -1.0f) {
                view.setScaleY (MIN_SCALE);
            } else if (position <= 0.0f) {
                view.setScaleY (scale);
            } else if (position <= 1.0f) {
                view.setScaleY (scale);
            } else {
                view.setScaleY (MIN_SCALE);
            }
        }

    }

    public void onBackPressed() {
        doubleBackQuit ();
    }

    /**
     * ???????????????????????????????????????
     */
    private void doubleBackQuit() {

//        Snackbar.make (mBnv, "???????????????????????????GeeksAndroid", Snackbar.LENGTH_SHORT).setActionTextColor (Color.parseColor ("#009688")).setCallback (new Snackbar.Callback () {
//            @Override
//            public void onDismissed(Snackbar snackbar, int event) {
////                Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
//                switch (event) {
//                    case Snackbar.Callback.DISMISS_EVENT_SWIPE:
//                        Toast.makeText (MainActivity.this, "????????????", Toast.LENGTH_SHORT).show ();
//                        break;
//                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
//                        break;
//                }
//                super.onDismissed (snackbar, event);
//            }
//
//            @Override
//            public void onShown(Snackbar snackbar) {
////                Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
//                super.onShown (snackbar);
//            }
//        }).setAction ("?????????", new View.OnClickListener () {
//            @Override
//            public void onClick(View v) {
////                Intent mIntent=new Intent(MainActivity.this,Like.class);
////                startActivity(mIntent);
//                // Toast.makeText(SecondActivity.this,"OK",Toast.LENGTH_SHORT).show();
//            }
//        }).show ();
        Snackbar.make (mBnv, "???????????????????????????(????????)", Snackbar.LENGTH_LONG)
                .setActionTextColor (Color.parseColor ("#009688"))
                .setAction ("?????????", new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show ();
        if (System.currentTimeMillis () - exitTime > 3000) {
//            Toast.makeText (getApplicationContext (), "??????????????????", Toast.LENGTH_SHORT).show ();
            ToastUtil.showShort ("????????????????????????");
            exitTime = System.currentTimeMillis ();
        } else {
            finish ();
        }
    }

    @Override
    protected void onResume() {
        super.onResume ();

    }

    @Override
    protected void onPause() {
        super.onPause ();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        //????????????????????????,?????????????????????
        //??????????????????????????????????????????Activity??????,????????????????????????????????????
        if (TextUtils.isEmpty (mType)) {
            //??????,?????????????????????????????????
            SpUtil.setParam (Constants.CURRENT_FRAG_TYPE, this);
        }
    }

}
