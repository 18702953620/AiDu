package com.ch.aidu.weight;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ch.aidu.R;
import com.ch.aidu.bean.ReadBgBean;
import com.ch.aidu.utils.BrightnessUtils;
import com.ch.aidu.utils.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ch on 17-5-18.
 */

public class ReadSettingDialog extends Dialog {
    private static final String TAG = "ReadSettingDialog";
    private static final int DEFAULT_TEXT_SIZE = 16;

    int[] colorBg = {R.color.ccec29c, R.color.cccebcc,
            R.color.caaaaaa, R.color.cd1cec5, R.color.c001c27};

    @BindView(R.id.read_setting_iv_brightness_minus)
    ImageView mIvBrightnessMinus;
    @BindView(R.id.read_setting_sb_brightness)
    SeekBar mSbBrightness;
    @BindView(R.id.read_setting_iv_brightness_plus)
    ImageView mIvBrightnessPlus;
    @BindView(R.id.read_setting_cb_brightness_auto)
    CheckBox mCbBrightnessAuto;
    @BindView(R.id.read_setting_tv_font_minus)
    TextView mTvFontMinus;
    @BindView(R.id.read_setting_tv_font)
    TextView mTvFont;
    @BindView(R.id.read_setting_tv_font_plus)
    TextView mTvFontPlus;
    @BindView(R.id.read_setting_cb_font_default)
    CheckBox mCbFontDefault;
    @BindView(R.id.read_setting_rg_page_mode)
    RadioGroup mRgPageMode;

    @BindView(R.id.read_setting_rb_simulation)
    RadioButton mRbSimulation;
    @BindView(R.id.read_setting_rb_cover)
    RadioButton mRbCover;
    @BindView(R.id.read_setting_rb_slide)
    RadioButton mRbSlide;
    @BindView(R.id.read_setting_rb_scroll)
    RadioButton mRbScroll;
    @BindView(R.id.read_setting_rb_none)
    RadioButton mRbNone;
    @BindView(R.id.read_setting_rv_bg)
    RecyclerView mRvBg;
    /************************************/
    private ReadBgAdapter mReadBgAdapter;
    private ReadSettingManager mSettingManager;
    private PageLoader mPageLoader;
    private Activity mActivity;

    private int mBrightness;
    private boolean isBrightnessAuto;
    private int mTextSize;
    private boolean isTextDefault;
    private int mPageMode;
    private int mReadBgTheme;
    private List<ReadBgBean> mReadBgBeans = new ArrayList<>();


    public ReadSettingDialog(@NonNull Activity activity, PageLoader mPageLoader) {
        super(activity, R.style.ReadSettingDialog);
        mActivity = activity;
        this.mPageLoader = mPageLoader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_read_setting);
        ButterKnife.bind(this);
        setUpWindow();
        initData();
        initWidget();
        initClick();
    }

    //设置Dialog显示的位置
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
    }

    private void initData() {
        mSettingManager = ReadSettingManager.getInstance();

        isBrightnessAuto = mSettingManager.isBrightnessAuto();
        mBrightness = mSettingManager.getBrightness();
        mTextSize = mSettingManager.getTextSize();
        isTextDefault = mSettingManager.isDefaultTextSize();
        mPageMode = mSettingManager.getPageMode();
        mReadBgTheme = mSettingManager.getReadBgTheme();
    }

    private void initWidget() {
        mSbBrightness.setProgress(mBrightness);
        mTvFont.setText(mTextSize + "");
        mCbBrightnessAuto.setChecked(isBrightnessAuto);
        mCbFontDefault.setChecked(isTextDefault);
        initPageMode();
        //RecyclerView
        setUpAdapter();
    }

    private void setUpAdapter() {
        setReadBg(0);
        mReadBgAdapter = new ReadBgAdapter(mReadBgBeans);
        //横向列表
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mRvBg.setLayoutManager(linearLayoutManager);
        mRvBg.setLayoutManager(new GridLayoutManager(getContext(), 5));
        mRvBg.setAdapter(mReadBgAdapter);

    }

    /**
     * 设置选择背景数据
     *
     * @param selectPos 选中下标
     */
    private void setReadBg(int selectPos) {
        mReadBgBeans.clear();
        for (int i = 0; i < colorBg.length; i++) {
            ReadBgBean readBgBean = new ReadBgBean();
            readBgBean.setBgColor(colorBg[i]);
            if (i == selectPos) {
                readBgBean.setSelect(true);
            } else {
                readBgBean.setSelect(false);
            }
            mReadBgBeans.add(readBgBean);
        }
    }

    private void initPageMode() {
        switch (mPageMode) {
            case PageView.PAGE_MODE_SIMULATION:
                mRbSimulation.setChecked(true);
                break;
            case PageView.PAGE_MODE_COVER:
                mRbCover.setChecked(true);
                break;
            case PageView.PAGE_MODE_SLIDE:
                mRbSlide.setChecked(true);
                break;
            case PageView.PAGE_MODE_NONE:
                mRbNone.setChecked(true);
                break;
            case PageView.PAGE_MODE_SCROLL:
                mRbScroll.setChecked(true);
                break;
        }
    }

    private Drawable getDrawable(int drawRes) {
        return ContextCompat.getDrawable(getContext(), drawRes);
    }

    private void initClick() {
        //亮度调节
        mIvBrightnessMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbBrightnessAuto.isChecked()) {
                    mCbBrightnessAuto.setChecked(false);
                }
                int progress = mSbBrightness.getProgress() - 1;
                if (progress < 0) return;
                mSbBrightness.setProgress(progress);
                BrightnessUtils.setBrightness(mActivity, progress);
            }
        });

        mIvBrightnessPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbBrightnessAuto.isChecked()) {
                    mCbBrightnessAuto.setChecked(false);
                }
                int progress = mSbBrightness.getProgress() + 1;
                if (progress > mSbBrightness.getMax()) return;
                mSbBrightness.setProgress(progress);
                BrightnessUtils.setBrightness(mActivity, progress);
                //设置进度
                ReadSettingManager.getInstance().setBrightness(progress);
            }
        });

        mSbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (mCbBrightnessAuto.isChecked()) {
                    mCbBrightnessAuto.setChecked(false);
                }
                //设置当前 Activity 的亮度
                BrightnessUtils.setBrightness(mActivity, progress);
                //存储亮度的进度条
                ReadSettingManager.getInstance().setBrightness(progress);
            }
        });
        mCbBrightnessAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //获取屏幕的亮度
                    BrightnessUtils.setBrightness(mActivity, BrightnessUtils.getScreenBrightness(mActivity));
                } else {
                    //获取进度条的亮度
                    BrightnessUtils.setBrightness(mActivity, mSbBrightness.getProgress());
                }
                ReadSettingManager.getInstance().setAutoBrightness(isChecked);
            }
        });


        //字体大小调节
        mTvFontMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbFontDefault.isChecked()) {
                    mCbFontDefault.setChecked(false);
                }
                int fontSize = Integer.valueOf(mTvFont.getText().toString()) - 1;
                if (fontSize < 0) return;
                mTvFont.setText(fontSize + "");
                mPageLoader.setTextSize(fontSize);
            }
        });

        mTvFontPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbFontDefault.isChecked()) {
                    mCbFontDefault.setChecked(false);
                }
                int fontSize = Integer.valueOf(mTvFont.getText().toString()) + 1;
                mTvFont.setText(fontSize + "");
                mPageLoader.setTextSize(fontSize);
            }
        });


        mCbFontDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    int fontSize = ScreenUtils.dpToPx(DEFAULT_TEXT_SIZE);
                    mTvFont.setText(fontSize + "");
                    mPageLoader.setTextSize(fontSize);
                }
            }
        });

        //Page Mode 切换

        mRgPageMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int pageMode = 0;
                switch (checkedId) {
                    case R.id.read_setting_rb_simulation:
                        pageMode = PageView.PAGE_MODE_SIMULATION;
                        break;
                    case R.id.read_setting_rb_cover:
                        pageMode = PageView.PAGE_MODE_COVER;
                        break;
                    case R.id.read_setting_rb_slide:
                        pageMode = PageView.PAGE_MODE_SLIDE;
                        break;
                    case R.id.read_setting_rb_scroll:
                        pageMode = PageView.PAGE_MODE_SCROLL;
                        break;
                    case R.id.read_setting_rb_none:
                        pageMode = PageView.PAGE_MODE_NONE;
                        break;
                }
                mPageLoader.setPageMode(pageMode);
            }
        });

        //背景的点击事件

        mReadBgAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mPageLoader.setBgColor(position);
                setReadBg(position);
                adapter.notifyDataSetChanged();
            }
        });

    }

    public boolean isBrightFollowSystem() {
        if (mCbBrightnessAuto == null) {
            return false;
        }
        return mCbBrightnessAuto.isChecked();
    }


    public class ReadBgAdapter extends BaseQuickAdapter<ReadBgBean, BaseViewHolder> {

        public ReadBgAdapter(@Nullable List<ReadBgBean> data) {
            super(R.layout.item_read_bg, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, ReadBgBean item) {
            helper.setBackgroundRes(R.id.read_bg_view, item.getBgColor());
            helper.setVisible(R.id.read_bg_iv_checked, item.isSelect());
        }
    }
}
