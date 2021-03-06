package com.example.wanandroid.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.wanandroid.R;
import com.example.wanandroid.adapter.HomeAdapter;
import com.example.wanandroid.base.BaseMvpFargment;
import com.example.wanandroid.callback.BaseView;
import com.example.wanandroid.bean.BannerBean;
import com.example.wanandroid.bean.HomeBean;
import com.example.wanandroid.model.HomeModel;
import com.example.wanandroid.presenter.HomePresenter;
import com.example.wanandroid.ui.activity.Web;
import com.example.wanandroid.util.AnimatorUtil;
import com.example.wanandroid.util.AnimatorUtil_two;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import net.lemonsoft.lemonbubble.LemonBubble;
import net.lemonsoft.lemonbubble.LemonBubbleInfo;
import net.lemonsoft.lemonbubble.enums.LemonBubbleLayoutStyle;
import net.lemonsoft.lemonbubble.enums.LemonBubbleLocationStyle;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BaseMvpFargment<HomePresenter, HomeModel, BaseView<HomeBean, String>> implements BaseView<HomeBean, String>, OnRefreshLoadMoreListener {


    @BindView(R.id.recy)
    RecyclerView recy;
    @BindView(R.id.smart)
    SmartRefreshLayout smart;
    Unbinder unbinder;
    private ArrayList<BannerBean.DataBean> list;
    private ArrayList<HomeBean.DataBean.DatasBean> homeList;
    private HomeAdapter adapter;
    private int page = 0;
    private BottomNavigationView bnv;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private int index;
    private LemonBubbleInfo roundProgressBubbleInfo;


    @Override
    protected void initView() {
        super.initView ();
        list = new ArrayList<> ();
        homeList = new ArrayList<> ();
        recy.setLayoutManager (new LinearLayoutManager (getActivity ()));
        adapter = new HomeAdapter (getActivity (), list, homeList);
        adapter.setHasStableIds (true);
        recy.setAdapter (adapter);
        roundProgressBubbleInfo = LemonBubble.getRoundProgressBubbleInfo ();
        recy.setItemAnimator (new DefaultItemAnimator ());
        smart.setOnRefreshLoadMoreListener (this);
        adapter.setOnItemCliclListener (new HomeAdapter.OnItemCliclListener () {
            @Override
            public void onItemClick(int position) {
                HomeBean.DataBean.DatasBean datasBean = homeList.get (position);
                String link = datasBean.getLink ();
                Intent intent = new Intent (getActivity (), Web.class);
                intent.putExtra ("link", link);
                startActivity (intent);
                index = position;
            }
        });
    }

    @Override
    protected void initData() {
        super.initData ();
        presenter.getHome (page++);
    }

    @Override
    protected void initListener() {
        super.initListener ();
        recy.addOnScrollListener (new RecyclerView.OnScrollListener () {
            @Override
            public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged (recyclerView, newState);
                //??????recyclerView????????????????????????
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager ();
                int position = manager.findFirstVisibleItemPosition ();
                if (position > 0.1) {
                    fab.setOnClickListener (new View.OnClickListener () {
                        @Override
                        public void onClick(View v) {
                            recyclerView.scrollToPosition (0);
                        }
                    });
                }
                bnv = getActivity ().findViewById (R.id.bnv);
                fab = getActivity ().findViewById (R.id.fab);
                toolbar = getActivity ().findViewById (R.id.toolbar);
                // ??????????????????????????????
                RecyclerView.ItemAnimator animator = recy.getItemAnimator ();
                if (animator instanceof SimpleItemAnimator) {
                    ((SimpleItemAnimator) animator).setSupportsChangeAnimations (false);
                }

                recy.getItemAnimator ().setChangeDuration (0);
                //??????????????????item??????????????????  ?????????0???????????????item????????????????????? ????????????????????????????????? ????????????????????????
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition ();
                // ???????????????
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    // ??????????????????????????????
//                    if (firstVisibleItemPosition == 3) {
//                        AnimatorUtil.translateShow (bnv, null);
//                        bnv.setVisibility (View.VISIBLE);
//                    } else {
//                        //????????????????????????
//
//                    }
//                    //??????RecyclerView?????????????????????
//                } else if (newState == RecyclerView.SCROLL_INDICATOR_TOP) {//?????????
////                    bnv.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled (recyclerView, dx, dy);
                //??????
                if (dy == 6) {
                    getActivity ().runOnUiThread (new Runnable () {
                        @Override
                        public void run() {
                            AnimatorUtil_two.translateHide (toolbar, null);
                            toolbar.setVisibility (View.GONE);
//                            AnimatorUtil.translateHide (bnv, null);
//                            AnimatorUtil.translateHide (fab, null);
//                            bnv.setVisibility (View.GONE);
                        }
                    });
                } else if (dy == -6) {
//                    //??????
                    getActivity ().runOnUiThread (new Runnable () {
                        @Override
                        public void run() {
                            AnimatorUtil.translateShow (toolbar, null);
                            toolbar.setVisibility (View.VISIBLE);
//                            AnimatorUtil.translateShow (fab, null);
//                            AnimatorUtil.translateShow (bnv, null);
//                            bnv.setVisibility (View.VISIBLE);
                        }
                    });
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager ();
                int childCount = layoutManager.getChildCount ();
                //?????????item???????????????
                for (int i = 0; i < childCount; i++) {

                    layoutManager.getChildAt (i).setAlpha (1);
                    layoutManager.getChildAt (i).setScaleY (1);
                    layoutManager.getChildAt (i).setScaleX (1);
                }
                calculateAlphaAndScale (recyclerView, layoutManager);
            }
        });
    }

    private void calculateAlphaAndScale(RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        //??????recyclerView????????????????????????
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager ();

        int firstItemPosition = manager.findFirstVisibleItemPosition ();
        int lastItemPosition = manager.findLastVisibleItemPosition ();
        //??????????????????view
        View lastView = manager.getChildAt (lastItemPosition - firstItemPosition);
        if (lastView != null) {
            //????????????????????????view???,?????????????????????view??????????????????,???????????????????????????????????????
            int itemHeight = lastView.getHeight ();
            int visibleHeight = recyclerView.getHeight () - lastView.getTop ();
            if (visibleHeight < 0) {
                return;
            }
            float ratio = visibleHeight * 1.0f / itemHeight;
            if (ratio > 1.0) {
                return;
            }
            lastView.setAlpha (ratio);
            float scale = 0.8f;//???????????????????????????
            float scaleFactor = scale + (1 - scale) * ratio;

            lastView.setScaleX (scaleFactor);
            lastView.setScaleY (scaleFactor);
        }
    }

    @Override
    protected BaseView<HomeBean, String> initMvpView() {
        return this;
    }

    @Override
    protected HomeModel initMvpModel() {
        return new HomeModel ();
    }

    @Override
    protected HomePresenter initMvpPresenter() {
        return new HomePresenter ();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onSuccess(HomeBean homeBean) {
//        homeList.clear ();
        homeList.addAll (homeBean.getData ().getDatas ());
        if (homeList != null && homeList.size () > 0) {
            // ????????????????????????????????????????????????
            LemonBubbleInfo myInfo = LemonBubble.getRightBubbleInfo ();
            // ???????????????????????????????????????
            myInfo.setLayoutStyle (LemonBubbleLayoutStyle.ICON_LEFT_TITLE_RIGHT);
            // ???????????????????????????
            myInfo.setLocationStyle (LemonBubbleLocationStyle.BOTTOM);
            // ????????????????????????????????????????????????
            myInfo.setIconColor (Color.GREEN);
            // ????????????????????????????????????dp
            myInfo.setBubbleSize (200, 80);
            // ???????????????????????????????????????????????????0.01,
            myInfo.setProportionOfDeviation (0.01f);
            // ???????????????????????????
            myInfo.setTitle ("??????");
            // ??????????????????????????????????????????2s?????????
            //            LemonBubble.showBubbleInfo (getActivity (), myInfo, 1000);
            //            LemonBubble.hide();
        }
        adapter.notifyDataSetChanged ();
    }

    @Override
    public void onFail(String s) {
        LemonBubble.showError (getActivity (), "?????????????????????????????????", 1000);
    }


    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        initData ();
        refreshLayout.finishLoadMore (1000);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 0;
        initData ();
        homeList.clear ();
        refreshLayout.finishRefresh (1000);
    }
}
