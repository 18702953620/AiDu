package com.ch.aidu.ui;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ch.aidu.R;
import com.ch.aidu.base.AC_Base;
import com.ch.aidu.bean.CollBookBean;
import com.ch.aidu.bean.FileMedia;
import com.ch.aidu.utils.DataUtils;
import com.ch.aidu.utils.FileUtils;
import com.ch.aidu.helper.CollBookHelper;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 2:01
 * 描述：
 * 来源：
 */


public class AC_Select extends AC_Base {
    @BindView(R.id.back)
    RelativeLayout back;
    @BindView(R.id.tv_simple_title)
    TextView tvSimpleTitle;
    @BindView(R.id.ll_tools)
    LinearLayout llTools;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.tv_path)
    TextView tvPath;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.rv_file)
    RecyclerView rvFile;
    private String baseDir;
    private FileAdapter adapter;

    public static final String BASE_PATH = "BASE_PATH";

    @Override
    protected int getLayoutId() {
        return R.layout.ac_select;
    }

    @Override
    protected void initView() {

        adapter = new FileAdapter(null);
        rvFile.setLayoutManager(new LinearLayoutManager(context));
        rvFile.addItemDecoration(new DividerItemDecoration(context, RecyclerView.VERTICAL));
        rvFile.setAdapter(adapter);

        baseDir = getIntent().getStringExtra(BASE_PATH);

        if (baseDir == null) {
            String state = Environment.getExternalStorageState();
            // 检查是否有存储卡
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                // 创建位置
                baseDir = Environment.getExternalStorageDirectory().getPath();
            }
        }

        updataDir(false);

    }


    private void updataDir(boolean isShowSelct) {
        List<FileMedia> listMedia = FileUtils.listFiles(baseDir, FileMedia.TYPE_TXT);
        adapter.setShowSelect(isShowSelct);
        adapter.setNewData(listMedia);
        tvPath.setText(baseDir);
    }

    @Override
    protected void addListener() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter ad, View view, int position) {
                List<FileMedia> list = adapter.getData();


                if (list.get(position).isDir()) {
                    if (list.get(position).getCount() == 0) {
                        return;
                    }
                    baseDir = list.get(position).getPath();

                    updataDir(false);
                } else {
                    //转换成CollBook,并存储
                    List<File> files = new ArrayList<>();
                    files.add(new File(list.get(position).getPath()));
                    List<CollBookBean> collBooks = FileUtils.convertCollBook(files);
                    CollBookHelper.getsInstance().saveBooks(collBooks);

                    Intent intent = new Intent(context, AC_Read.class);
                    intent.putExtra(AC_Read.EXTRA_COLL_BOOK, collBooks.get(0));
                    startActivity(intent);
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter ad, View view, int position) {
                if (adapter.isShowSelect()) {
                    adapter.setShowSelect(false);
                } else {
                    List<FileMedia> list = ad.getData();
                    list.get(position).setSelected(true);
                    adapter.setShowSelect(true);
                }
                ad.notifyDataSetChanged();
                return false;
            }
        });

    }


    @OnClick({R.id.back, R.id.ll_tools, R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ll_tools:
                break;
            //上一级
            case R.id.tv_back:
                if (baseDir.equals(Environment.getExternalStorageDirectory().getPath())) {
                    showtoast("已经是初始目录了");
                    return;
                }

                String tempPath = new File(baseDir).getParent();
                if (tempPath == null) {
                    return;
                }

                baseDir = tempPath;

                updataDir(false);
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (baseDir.equals(Environment.getExternalStorageDirectory().getPath())) {
            super.onBackPressed();
        } else {
            String tempPath = new File(baseDir).getParent();
            if (tempPath == null) {
                return;
            }
            baseDir = tempPath;
            updataDir(false);
        }


    }

    class FileAdapter extends BaseQuickAdapter<FileMedia, BaseViewHolder> {

        private boolean isShowSelect;

        public FileAdapter(@Nullable List<FileMedia> data) {
            super(R.layout.item_dir, data);
        }

        public void setShowSelect(boolean showSelect) {
            isShowSelect = showSelect;
        }

        public boolean isShowSelect() {
            return isShowSelect;
        }


        @Override
        protected void convert(BaseViewHolder holder, final FileMedia bean) {
            holder.setText(R.id.tv_dir_name, bean.getName());
            if (bean.isDir()) {
                holder.setBackgroundRes(R.id.iv_dir, R.mipmap.folder);
                holder.setText(R.id.tv_dir_length, bean.getCount() + "项");
            } else {
                holder.setBackgroundRes(R.id.iv_dir, R.mipmap.file);
                holder.setText(R.id.tv_dir_length, FileUtils.getFormatSize(bean.getSize()));
            }
            ImageView cb_dir = holder.getView(R.id.cb_dir);

            if (isShowSelect) {
                cb_dir.setVisibility(View.VISIBLE);
                if (bean.isSelected()) {
                    cb_dir.setBackgroundResource(R.mipmap.ic_select);
                } else {
                    cb_dir.setBackgroundResource(R.mipmap.ic_unselect);
                }
            } else {
                cb_dir.setVisibility(View.GONE);
            }

            cb_dir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.isSelected()) {
                        bean.setSelected(false);
                    } else {
                        bean.setSelected(true);
                    }
                    notifyDataSetChanged();

                }
            });
        }

    }


}
