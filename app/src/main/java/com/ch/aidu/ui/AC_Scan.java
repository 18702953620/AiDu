package com.ch.aidu.ui;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.ch.aidu.helper.CollBookHelper;
import com.ch.aidu.utils.FileUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 作者： ch
 * 时间： 2018/8/9 0009-下午 1:46
 * 描述： 智能扫描
 * 来源：
 */


public class AC_Scan extends AC_Base {
    @BindView(R.id.back)
    RelativeLayout back;
    @BindView(R.id.tv_simple_title)
    TextView tvSimpleTitle;
    @BindView(R.id.ll_tools)
    LinearLayout llTools;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.rv_file)
    RecyclerView rvFile;
    private FileAdapter adapter;

    private List<FileMedia> fileMediaList;

    @Override
    protected int getLayoutId() {
        return R.layout.ac_scan;
    }

    @Override
    protected void initView() {
        adapter = new FileAdapter(null);
        rvFile.setLayoutManager(new LinearLayoutManager(context));
        rvFile.addItemDecoration(new DividerItemDecoration(context, RecyclerView.VERTICAL));
        rvFile.setAdapter(adapter);

        fileMediaList = new ArrayList<>();
        showLoadingDialog();
        new ScanTask().execute();

    }

    @Override
    protected void addListener() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter ad, View view, int position) {
                List<FileMedia> list = adapter.getData();

                //转换成CollBook,并存储
                List<File> files = new ArrayList<>();
                files.add(new File(list.get(position).getPath()));
                List<CollBookBean> collBooks = FileUtils.convertCollBook(files);
                CollBookHelper.getsInstance().saveBooks(collBooks);

                Intent intent = new Intent(context, AC_Read.class);
                intent.putExtra(AC_Read.EXTRA_COLL_BOOK, collBooks.get(0));
                startActivity(intent);
            }
        });

    }

    @OnClick({R.id.back, R.id.ll_tools})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ll_tools:
                break;
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


    class ScanTask extends AsyncTask<Void, Void, List<FileMedia>> {


        @Override
        protected List<FileMedia> doInBackground(Void... voids) {

            fileMediaList = new ArrayList<>();

            String state = Environment.getExternalStorageState();
            // 检查是否有存储卡
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                // 创建位置
                String baseDir = Environment.getExternalStorageDirectory().getPath();

                FileUtils.listFiles(baseDir, fileMediaList);
            }
            return fileMediaList;
        }

        @Override
        protected void onPostExecute(List<FileMedia> mediaList) {
            adapter.setNewData(mediaList);
            showtoast("扫描完毕，共扫描到" + mediaList.size() + "个");
            closeLoadingDialog();
        }

    }
}
