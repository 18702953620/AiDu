package com.ch.aidu.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ch.aidu.R;
import com.ch.aidu.base.AC_Base;
import com.ch.aidu.bean.CollBookBean;
import com.ch.aidu.bean.TxtChapter;
import com.ch.aidu.utils.BrightnessUtils;
import com.ch.aidu.utils.ScreenUtils;
import com.ch.aidu.utils.StatusBarUtils;
import com.ch.aidu.weight.LocalPageLoader;
import com.ch.aidu.weight.PageLoader;
import com.ch.aidu.weight.PageView;
import com.ch.aidu.weight.ReadSettingDialog;
import com.ch.aidu.weight.ReadSettingManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 3:41
 * 描述：
 * 来源：
 */


public class AC_Read extends AC_Base {
    @BindView(R.id.tv_toolbar_title)
    TextView tvToolbarTitle;
    @BindView(R.id.al_title)
    AppBarLayout alTitle;
    @BindView(R.id.pv_read)
    PageView pvRead;
    @BindView(R.id.tv_read_page_tip)
    TextView tvReadPageTip;
    @BindView(R.id.tv_read_pre_chapter)
    TextView tvReadPreChapter;
    @BindView(R.id.sb_read_chapter_progress)
    SeekBar sb_chapter;
    @BindView(R.id.tv_read_next_chapter)
    TextView tvReadNextChapter;
    @BindView(R.id.tv_read_category)
    TextView tvReadCategory;
    @BindView(R.id.tv_read_night_mode)
    TextView tvReadNightMode;
    @BindView(R.id.tv_read_setting)
    TextView tvReadSetting;
    @BindView(R.id.ll_read_bottom_menu)
    LinearLayout llReadBottomMenu;
    @BindView(R.id.rv_read_category)
    RecyclerView rvReadCategory;
    @BindView(R.id.read_dl_slide)
    DrawerLayout dlSlide;
    public static final String EXTRA_COLL_BOOK = "extra_coll_book";
    private CollBookBean bookBean;
    private boolean isNightMode;
    private boolean isFullScreen;
    private LocalPageLoader pageLoader;
    private ReadCategoryAdapter categoryAdapter;
    private List<TxtChapter> txtChapters = new ArrayList<>();
    private Animation topInAnim;
    private Animation topOutAnim;
    private Animation bottomInAnim;
    private Animation bottomOutAnim;
    private ReadSettingDialog settingDialog;
    private PowerManager.WakeLock wakeLock;


    // 接收电池信息和时间更新的广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                pageLoader.updateBattery(level);
            }
            //监听分钟的变化
            else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                pageLoader.updateTime();
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.ac_read;
    }

    @Override
    protected void setStatusBar() {
    }

    @Override
    protected void initView() {

        bookBean = (CollBookBean) getIntent().getSerializableExtra(EXTRA_COLL_BOOK);
        isNightMode = ReadSettingManager.getInstance().isNightMode();
        isFullScreen = ReadSettingManager.getInstance().isFullScreen();
        tvToolbarTitle.setText(bookBean.getTitle());


        pageLoader = pvRead.getPageLoader(bookBean.isLocal());
        dlSlide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        pageLoader.openBook(bookBean);

        //更多设置dialog
        settingDialog = new ReadSettingDialog(this, pageLoader);

        setCategory();

        //初始化TopMenu
        initTopMenu();

        //初始化BottomMenu
        initBottomMenu();

        toggleNightMode();


        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);

        //设置当前Activity的Brightness
        if (ReadSettingManager.getInstance().isBrightnessAuto()) {
            BrightnessUtils.setBrightness(this, BrightnessUtils.getScreenBrightness(this));
        } else {
            BrightnessUtils.setBrightness(this, ReadSettingManager.getInstance().getBrightness());
        }

        //初始化屏幕常亮类
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "keep bright");
        //隐藏StatusBar
        pvRead.post(new Runnable() {
            @Override
            public void run() {
                hideSystemBar();
            }
        });


    }

    @Override
    protected void addListener() {
        pvRead.setTouchListener(new PageView.TouchListener() {
            @Override
            public void center() {
                toggleMenu(true);
            }

            @Override
            public boolean onTouch() {
                return !hideReadMenu();
            }

            @Override
            public boolean prePage() {
                return true;
            }

            @Override
            public boolean nextPage() {
                return true;
            }

            @Override
            public void cancel() {
            }
        });


        pageLoader.setOnPageChangeListener(new PageLoader.OnPageChangeListener() {
            @Override
            public void onChapterChange(int pos) {
                setCategorySelect(pos);

            }

            @Override
            public void onLoadChapter(List<TxtChapter> chapters, int pos) {
                setCategorySelect(pageLoader.getChapterPos());

                //隐藏提示
                tvReadPageTip.setVisibility(GONE);
                sb_chapter.setProgress(0);
            }

            @Override
            public void onCategoryFinish(List<TxtChapter> chapters) {
                txtChapters.clear();
                txtChapters.addAll(chapters);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageCountChange(int count) {
                sb_chapter.setEnabled(true);
                sb_chapter.setMax(count - 1);
                sb_chapter.setProgress(0);
            }

            @Override
            public void onPageChange(final int pos) {
                sb_chapter.post(new Runnable() {
                    @Override
                    public void run() {
                        sb_chapter.setProgress(pos);
                    }
                });
            }
        });


        sb_chapter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (llReadBottomMenu.getVisibility() == VISIBLE) {
                    //显示标题
                    tvReadPageTip.setText((progress + 1) + "/" + (sb_chapter.getMax() + 1));
                    tvReadPageTip.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //进行切换
                int pagePos = sb_chapter.getProgress();
                if (pagePos != pageLoader.getPagePos()) {
                    pageLoader.skipToPage(pagePos);
                }
                //隐藏提示
                tvReadPageTip.setVisibility(GONE);
            }
        });

    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        hideSystemBar();
        if (alTitle.getVisibility() == VISIBLE) {
            toggleMenu(true);
            return true;
        } else if (settingDialog.isShowing()) {
            settingDialog.dismiss();
            return true;
        }
        return false;
    }


    private void initTopMenu() {
        if (Build.VERSION.SDK_INT >= 19) {
            alTitle.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
        }
    }

    private void initBottomMenu() {
        //判断是否全屏
        if (ReadSettingManager.getInstance().isFullScreen()) {
            //还需要设置mBottomMenu的底部高度
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) llReadBottomMenu.getLayoutParams();
            params.bottomMargin = ScreenUtils.getNavigationBarHeight();
            llReadBottomMenu.setLayoutParams(params);
        } else {
            //设置mBottomMenu的底部距离
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) llReadBottomMenu.getLayoutParams();
            params.bottomMargin = 0;
            llReadBottomMenu.setLayoutParams(params);
        }
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu(boolean hideStatusBar) {
        initMenuAnim();

        if (alTitle.getVisibility() == View.VISIBLE) {
            //关闭
            alTitle.startAnimation(topOutAnim);
            llReadBottomMenu.startAnimation(bottomOutAnim);
            alTitle.setVisibility(GONE);
            llReadBottomMenu.setVisibility(GONE);
            tvReadPageTip.setVisibility(GONE);

            if (hideStatusBar) {
                hideSystemBar();
            }
        } else {
            alTitle.setVisibility(View.VISIBLE);
            llReadBottomMenu.setVisibility(View.VISIBLE);
            alTitle.startAnimation(topInAnim);
            llReadBottomMenu.startAnimation(bottomInAnim);

            showSystemBar();
        }
    }


    private void showSystemBar() {
        //显示
        StatusBarUtils.showUnStableStatusBar(this);
        if (isFullScreen) {
            StatusBarUtils.showUnStableNavBar(this);
        }
    }

    private void hideSystemBar() {
        //隐藏
        StatusBarUtils.hideStableStatusBar(this);
        if (isFullScreen) {
            StatusBarUtils.hideStableNavBar(this);
        }
    }


    //初始化菜单动画
    private void initMenuAnim() {
        if (topInAnim != null) return;

        topInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        topOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        bottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        bottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
        //退出的速度要快
        topOutAnim.setDuration(200);
        bottomOutAnim.setDuration(200);
    }

    private void setCategory() {
        rvReadCategory.setLayoutManager(new LinearLayoutManager(context));
        categoryAdapter = new ReadCategoryAdapter(txtChapters);
        rvReadCategory.setAdapter(categoryAdapter);

        if (txtChapters.size() > 0) {
            setCategorySelect(0);
        }

        categoryAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setCategorySelect(position);
                dlSlide.closeDrawer(Gravity.START);
                pageLoader.skipToChapter(position);
            }
        });

    }


    private void toggleNightMode() {
        if (isNightMode) {
            tvReadNightMode.setText("白天");
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.read_menu_morning);
            tvReadNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        } else {
            tvReadNightMode.setText("夜晚");
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.read_menu_night);
            tvReadNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }

    /**
     * 设置选中目录
     *
     * @param selectPos
     */
    private void setCategorySelect(int selectPos) {
        for (int i = 0; i < txtChapters.size(); i++) {
            TxtChapter chapter = txtChapters.get(i);
            if (i == selectPos) {
                chapter.setSelect(true);
            } else {
                chapter.setSelect(false);
            }
        }

        categoryAdapter.notifyDataSetChanged();
        rvReadCategory.smoothScrollToPosition(selectPos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pageLoader.saveRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @OnClick({R.id.tv_read_page_tip, R.id.tv_read_pre_chapter, R.id.tv_read_next_chapter, R.id.tv_read_category, R.id.tv_read_night_mode, R.id.tv_read_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_read_page_tip:
                break;
            //上一章
            case R.id.tv_read_pre_chapter:
                setCategorySelect(pageLoader.skipPreChapter());
                break;
            //下一章
            case R.id.tv_read_next_chapter:
                setCategorySelect(pageLoader.skipNextChapter());
                break;
            //目录
            case R.id.tv_read_category:
                setCategorySelect(pageLoader.getChapterPos());
                //切换菜单
                toggleMenu(true);
                //打开侧滑动栏
                dlSlide.openDrawer(Gravity.START);
                break;
            //夜间/日间
            case R.id.tv_read_night_mode:
                if (isNightMode) {
                    isNightMode = false;
                } else {
                    isNightMode = true;
                }
                pageLoader.setNightMode(isNightMode);
                toggleNightMode();
                break;
            //设置
            case R.id.tv_read_setting:
                toggleMenu(false);
                settingDialog.show();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (alTitle.getVisibility() == View.VISIBLE) {
            //非全屏下才收缩，全屏下直接退出
            if (!ReadSettingManager.getInstance().isFullScreen()) {
                toggleMenu(true);
                return;
            }
        } else if (settingDialog.isShowing()) {
            settingDialog.dismiss();
            return;
        } else if (dlSlide.isDrawerOpen(Gravity.START)) {
            dlSlide.closeDrawer(Gravity.START);
            return;
        }


        super.onBackPressed();
    }

    public class ReadCategoryAdapter extends BaseQuickAdapter<TxtChapter, BaseViewHolder> {


        public ReadCategoryAdapter(@Nullable List<TxtChapter> data) {
            super(R.layout.item_category, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, TxtChapter item) {
            //首先判断是否该章已下载
            Drawable drawable = null;

            //如果没有链接地址表示是本地文件
            if (item.getLink() == null) {
                drawable = ContextCompat.getDrawable(context, R.drawable.selector_category_load);
            } else {
                if (item.getBookId() != null) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.selector_category_load);
                } else {
                    drawable = ContextCompat.getDrawable(context, R.drawable.selector_category_unload);
                }
            }

            TextView category_tv_chapter = helper.getView(R.id.category_tv_chapter);
            category_tv_chapter.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            category_tv_chapter.setSelected(item.isSelect());
            category_tv_chapter.setText(item.getTitle());
            if (item.isSelect()) {
                category_tv_chapter.setTextColor(ContextCompat.getColor(context, R.color.cec4a48));
            } else {
                category_tv_chapter.setTextColor(ContextCompat.getColor(context, R.color.c000000));
            }

        }
    }
}
