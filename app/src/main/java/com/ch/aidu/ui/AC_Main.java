package com.ch.aidu.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ch.aidu.R;
import com.ch.aidu.base.AC_Base;
import com.ch.aidu.bean.CollBookBean;
import com.ch.aidu.helper.CollBookHelper;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 主页面 ch
 */
public class AC_Main extends AC_Base implements View.OnClickListener {

    @BindView(R.id.ll_tools)
    LinearLayout llTools;
    @BindView(R.id.rv_main)
    RecyclerView rvMain;
    @BindView(R.id.sl_main)
    SmartRefreshLayout slMain;
    private List<CollBookBean> allBooks = new ArrayList<>();
    private BookShelfAdapter bookAdapter;
    private PopupWindow popTools;
    private PopupWindow popBook;
    private TextView tvScan;
    private TextView tvSelect;
    private TextView tvSetting;
    private TextView tvLookFile;
    private TextView tvDelete;
    private int longClickPosition = -1;
    private long mLastClickReturnTime;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        allBooks.addAll(CollBookHelper.getsInstance().findAllBooks());
        bookAdapter = new BookShelfAdapter(allBooks);
        rvMain.setLayoutManager(new LinearLayoutManager(context));
        bookAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        rvMain.setAdapter(bookAdapter);

        initToolsPop();
        initShelfPop();

    }

    private void initShelfPop() {
        View view = getViewByRes(R.layout.pop_book_shlef);

        tvLookFile = view.findViewById(R.id.tv_look_file);
        tvDelete = view.findViewById(R.id.tv_delete);

        tvLookFile.setOnClickListener(this);
        tvDelete.setOnClickListener(this);

        popBook = new PopupWindow(context);
        popBook.setContentView(view);
        popBook.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popBook.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popBook.setFocusable(false);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        popBook.setBackgroundDrawable(dw);
        popBook.setOutsideTouchable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popBook != null && popBook.isShowing()) {
                    popBook.dismiss();
                }
            }

        });
    }

    private void initToolsPop() {
        View view = getViewByRes(R.layout.pop_tools);
        tvScan = view.findViewById(R.id.tv_scan);
        tvSelect = view.findViewById(R.id.tv_select);
        tvSetting = view.findViewById(R.id.tv_setting);

        tvScan.setOnClickListener(this);
        tvSelect.setOnClickListener(this);
        tvSetting.setOnClickListener(this);

        popTools = new PopupWindow(context);
        popTools.setContentView(view);
        popTools.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popTools.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popTools.setFocusable(false);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        popTools.setBackgroundDrawable(dw);
        popTools.setOutsideTouchable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popTools != null && popTools.isShowing()) {
                    popTools.dismiss();
                }
            }

        });

    }

    @Override
    protected void addListener() {
        bookAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                List<CollBookBean> collBookBeanList = adapter.getData();
                Intent intent = new Intent(context, AC_Read.class);
                intent.putExtra(AC_Read.EXTRA_COLL_BOOK, collBookBeanList.get(position));
                startActivity(intent);
            }
        });
        slMain.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadBook();
                refreshLayout.finishRefresh(100);
            }
        });
        bookAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                longClickPosition = position;
                popBook.showAsDropDown(view, 0, -view.getHeight() / 2);
                return true;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBook();
    }

    private void loadBook() {
        allBooks = CollBookHelper.getsInstance().findAllBooks();
        bookAdapter.setNewData(allBooks);
    }

    @OnClick(R.id.ll_tools)
    public void onViewClicked() {
        popTools.showAsDropDown(llTools);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //扫描ContentCatcherManager
            case R.id.tv_scan:
                startA(AC_Scan.class);
                break;
            //手动选择
            case R.id.tv_select:
                startA(AC_Select.class);
                break;
            //设置
            case R.id.tv_setting:
                break;
            //查看文件
            case R.id.tv_look_file:
                if (longClickPosition >= 0) {
                    String path = allBooks.get(longClickPosition).get_id();

                    File file = new File(path);
                    if (!file.exists()) {
                        return;
                    }
                    Intent intent = new Intent(context, AC_Select.class);
                    intent.putExtra(AC_Select.BASE_PATH, file.getParent());
                    startActivity(intent);
                }
                break;
            //删除
            case R.id.tv_delete:
                if (longClickPosition >= 0) {
                    String bookId = allBooks.get(longClickPosition).get_id();
                    CollBookHelper.getsInstance().removeBookInRx(bookId);
                    //刷新
                    bookAdapter.remove(longClickPosition);
                }
                break;
        }
        if (popTools != null && popTools.isShowing()) {
            popTools.dismiss();
        }

        if (popBook != null && popBook.isShowing()) {
            popBook.dismiss();
        }


    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastClickReturnTime > 1000L) {
            mLastClickReturnTime = System.currentTimeMillis();
            showtoast("再按一次退出应用");
        } else {
            finish();
        }
    }


    public class BookShelfAdapter extends BaseQuickAdapter<CollBookBean, BaseViewHolder> {


        public BookShelfAdapter(@Nullable List<CollBookBean> data) {
            super(R.layout.item_book_shelf, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CollBookBean item) {
            helper.setImageResource(R.id.coll_book_iv_cover, R.drawable.ic_base_local_book);
            //文件名称
            helper.setText(R.id.coll_book_tv_name, item.getTitle());
            //最后阅读的章节
            helper.setText(R.id.coll_book_tv_chapter, item.getLastChapter());
            //最后更新日期
            helper.setText(R.id.coll_book_tv_lately_update, item.getLastRead());

            if (item.isUpdate()) {
                helper.setVisible(R.id.coll_book_iv_red_rot, true);
            } else {
                helper.setVisible(R.id.coll_book_iv_red_rot, false);

            }

        }
    }

}
