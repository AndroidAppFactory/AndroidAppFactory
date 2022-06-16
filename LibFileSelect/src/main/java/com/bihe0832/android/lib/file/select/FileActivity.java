package com.bihe0832.android.lib.file.select;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bihe0832.android.lib.file.mimetype.FileMimeTypes;
import com.bihe0832.android.lib.immersion.AppCompatActivityImmersiveExtKt;
import com.bihe0832.android.lib.permission.PermissionManager;
import com.bihe0832.android.lib.permission.PermissionManager.OnPermissionResult;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.ui.toast.ToastUtil;
import com.bihe0832.android.lib.ui.touchregion.ViewExtForTouchRegionKt;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import me.yokeyword.fragmentation.SupportActivity;

public class FileActivity extends SupportActivity {

    final int HANDLER_MSG_SELECTED = 0x01;
    ImageView btn_back, btn_close;
    TextView tv_path;
    ListView lv;
    FileAdapter mAdapter;
    File mFile = null;
    File[] mFileArr = null;
    String mSelectedPath = null;
    FileSearchTask mFSTask = null;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_bihe0832_activity_file);

        AppCompatActivityImmersiveExtKt
                .enableActivityImmersive(
                        this,
                        ContextCompat.getColor(this, R.color.colorPrimaryDark),
                        ContextCompat.getColor(this, R.color.navigationBarColor)
                );

        init();
        initHandler();
        initView();
        initViewAction();

        String path = getIntent().getStringExtra(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL);
        if (!TextUtils.isEmpty(path)) {
            mFSTask.execute(new File(path).getParentFile());
        } else {
            mFSTask.execute(Environment.getExternalStorageDirectory());
        }
    }


    private void init() {
        PermissionManager.INSTANCE.addPermissionDesc(new HashMap<String, String>() {
            {
                put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储卡");
            }
        });

        PermissionManager.INSTANCE.addPermissionScene(new HashMap<String, String>() {
            {
                put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "选择文件");
            }
        });
        String[] permission = new String[1];
        permission[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        PermissionManager.INSTANCE.checkPermission(this, "FileSelect", false, new OnPermissionResult() {
            @Override
            public void onSuccess() {
//                ToastUtil.showShort(FileActivity.this, "用户授权成功");
            }

            @Override
            public void onUserCancel(String scene, String permission) {
                ToastUtil.showShort(FileActivity.this, "未获得访问存储卡权限，请重试");
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onUserDeny(String scene, String permission) {
                ToastUtil.showShort(FileActivity.this, "未获得访问存储卡权限，请重试");
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onFailed(@NotNull String msg) {
                ToastUtil.showShort(FileActivity.this, "未获得访问存储卡权限，请重试");
                setResult(RESULT_CANCELED);
                finish();
            }
        }, permission);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFSTask != null) {
            mFSTask.cancel(true);
            mFSTask = null;
        }
        mFileArr = null;
        mFile = null;
    }


    private void initView() {
        btn_back = findViewById(R.id.activity_file_btn_back);
        btn_close = findViewById(R.id.activity_file_btn_close);
        tv_path = findViewById(R.id.activity_file_tv_title);
        lv = findViewById(R.id.activity_file_lv);
        mAdapter = new FileAdapter();
        lv.setAdapter(mAdapter);
        mFSTask = new FileSearchTask();

    }

    private void initViewAction() {

        btn_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackAction();
            }
        });
        ViewExtForTouchRegionKt.expandTouchRegionWithdp(btn_back, 30);

        btn_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopSearch();

                if (mSelectedPath == null) {
                    setResult(RESULT_CANCELED);
                } else {
                    Intent data = new Intent();
                    data.putExtra(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, mSelectedPath);
                    setResult(RESULT_OK, data);
                }
                finish();
            }
        });
        ViewExtForTouchRegionKt.expandTouchRegionWithdp(btn_close, 30);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mFileArr != null) {
                    File mTempFile = mFileArr[position];
                    if (mTempFile.isDirectory()) {
                        stopSearch();
                        if (mTempFile.list() == null || mTempFile.listFiles() == null
                                || mTempFile.listFiles().length < 1) {
                            ToastUtil.showShort(FileActivity.this, "当前为空目录");
                        } else {
                            mFSTask = new FileSearchTask();
                            mFSTask.execute(mTempFile);
                        }
                    } else {
                        if (mSelectedPath != mTempFile.getAbsolutePath()) {
                            mSelectedPath = mTempFile.getAbsolutePath();
                        } else {
                            mSelectedPath = mSelectedPath == null ? mTempFile.getAbsolutePath() : null;
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


    }

    private void stopSearch() {
        if (mFSTask != null) {
            mFSTask.cancel(true);
        }
    }

    private void initHandler() {
        mHandler = new Handler(ThreadManager.getInstance().getLooper(ThreadManager.LOOPER_TYPE_ANDROID_MAIN)) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_MSG_SELECTED:
                        if (mSelectedPath != null) {
                            btn_close.setBackgroundResource(R.mipmap.ic_done);
                            btn_back.setVisibility(View.GONE);
                            tv_path.setText("已选择：" + mSelectedPath);
                        } else {
                            btn_close.setBackgroundResource(R.mipmap.close);
                            btn_back.setVisibility(View.VISIBLE);
                            tv_path.setText(mFile.getAbsolutePath());
                        }
                        break;
                }
            }
        };
    }

    private void loadFile(File file) {
        final File mTempFile = file;
        if (mTempFile != null && mTempFile.exists() && mTempFile.isDirectory()) {
            mFile = mTempFile;
            mFileArr = mTempFile.listFiles();
            if (mFileArr != null && mFileArr.length > 0) {
                Arrays.sort(mFileArr, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        int result;
                        boolean f1 = lhs.isDirectory();
                        boolean f2 = rhs.isDirectory();
                        if (f1 && !f2) {
                            result = -1;
                        } else if (!f1 && f2) {
                            result = 1;
                        } else {
                            result = lhs.getName().toUpperCase().compareTo(rhs.getName().toUpperCase());
                        }
                        return result;
                    }
                });
            }
        }
    }

    private class FileSearchTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFileArr = null;
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                loadFile((File) params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                mFileArr = null;
            }
            return mFileArr;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            tv_path.setText(mFile.getAbsolutePath());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mAdapter.notifyDataSetChanged();
        }
    }

    class FileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFileArr == null ? 0 : mFileArr.length;
        }

        @Override
        public Object getItem(int position) {
            return mFileArr[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FileViewHolder mHolder;
            if (convertView == null) {
                mHolder = new FileViewHolder();
                convertView = LayoutInflater.from(FileActivity.this)
                        .inflate(R.layout.com_bihe0832_card_item_file, null);
                mHolder.iv_icon = (ImageView) convertView.findViewById(R.id.item_file_iv_icon);
                mHolder.tv_fileName = (TextView) convertView.findViewById(R.id.item_file_tv_filename);
                mHolder.cb_selected = (CheckBox) convertView.findViewById(R.id.item_file_cb_selected);
                mHolder.cb_selected.setOnCheckedChangeListener(MyCheckListener);
                convertView.setTag(mHolder);
            } else {
                mHolder = (FileViewHolder) convertView.getTag();
            }

            File mTempFile = mFileArr[position];
            if (mTempFile.isDirectory()) {
                mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_folder);
            } else {
                if (FileMimeTypes.INSTANCE.isTextFile(mTempFile.getName())) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_text);
                } else if (FileMimeTypes.INSTANCE.isApkFile(mTempFile.getName())) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_apk);
                } else if (FileMimeTypes.INSTANCE.isImageFile(mTempFile.getName())) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_image);
                } else if (FileMimeTypes.INSTANCE.isArchive(mTempFile.getName())) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_archive);
                } else if (FileMimeTypes.INSTANCE.isVideoFile(mTempFile)) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_video);
                } else if (FileMimeTypes.INSTANCE.isAudioFile(mTempFile)) {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_audio);
                } else {
                    mHolder.iv_icon.setImageResource(R.mipmap.ic_file_type_file);
                }
            }
            mHolder.iv_icon.setColorFilter(getResources().getColor(R.color.primary_text));

            mHolder.cb_selected.setVisibility(View.VISIBLE);
            mHolder.cb_selected.setTag(position);
            if (mSelectedPath != null && mTempFile.getAbsolutePath().equals(mSelectedPath)) {
                mHolder.cb_selected.setChecked(true);
            } else {
                mHolder.cb_selected.setChecked(false);
            }

            mHolder.tv_fileName.setText(mTempFile.getName());

            return convertView;
        }

        CompoundButton.OnCheckedChangeListener MyCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getTag() != null) {
                    int position = (int) buttonView.getTag();
                    File mTempFile = mFileArr[position];
                    if (isChecked) {
                        mSelectedPath = mTempFile.getAbsolutePath();
                    } else {
                        if (mSelectedPath != null && mTempFile.getAbsolutePath().equals(mSelectedPath)) {
                            mSelectedPath = null;
                        }
                    }
                    mHandler.sendEmptyMessage(HANDLER_MSG_SELECTED);
                    FileAdapter.this.notifyDataSetChanged();
                }
            }
        };
    }

    private void onBackAction() {
        if (mSelectedPath != null) {
            mSelectedPath = null;
            mAdapter.notifyDataSetChanged();
            return;
        }
        if (mFileArr != null) {
            File mTempFile = mFile.getParentFile();
            if (mTempFile != null) {
                mFSTask = new FileSearchTask();
                mFSTask.execute(mTempFile);
            } else {
                ToastUtil.showShort(FileActivity.this, "已经是最上层目录了");
            }
        }
    }

    @Override
    public void onBackPressedSupport() {
        onBackAction();
    }
}
