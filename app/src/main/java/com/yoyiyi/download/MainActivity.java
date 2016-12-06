package com.yoyiyi.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.yoyiyi.download.http.DownloadUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{

    @BindView(R.id.et_url)
    EditText mEtUrl;
    @BindView(R.id.et_file)
    EditText mEtFile;
    @BindView(R.id.ll)
    LinearLayout mLl;
    @BindView(R.id.btn_down)
    Button mBtnDown;
    @BindView(R.id.btn_pause)
    Button mBtnPause;
    @BindView(R.id.pb_down)
    ProgressBar mPbDown;
    @BindView(R.id.tv_update)
    TextView mTvUpdate;
    @BindView(R.id.activity_main)
    LinearLayout mActivityMain;
    @BindView(R.id.btn_delete)
    Button mBtnDelete;
    private int size;
    //http://192.168.2.222/entboost/ebc_install.zip
    private DownloadUtils mUtils;
    //  private static final String URL = "http://192.168.2.222/entboost/ebc_install.zip";
    //private static final String URL = "http://218.205.75.62/apk.r1.market.hiapk.com/data/upload/apkres/2016/11_23/9/cn.graphic.artist_094512.apk?wsiphost=local";
    private static final String URL = "http://gdown.baidu.com/data/wisegame/ea3ece7bbe67330b/anzhuoshichang_16792523.apk";

    //private static final String URL = "http://dldir1.qq.com/weixin/android/weixin6331android940.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initWidget();
    }

    /**
     * 初始化控件
     */
    private void initWidget()
    {
        mBtnDown.setOnClickListener(v -> mUtils.start());

        mBtnPause.setOnClickListener(v -> {
            mUtils.pause();
            mBtnDown.setEnabled(true);
        });

        mBtnDelete.setOnClickListener(v -> {
            mUtils.delete();
            mBtnDown.setEnabled(true);
            mPbDown.setMax(0);
            mPbDown.setProgress(0);
            mTvUpdate.setText("0/0");
        });

        mEtUrl.setText(URL);

    }

    /**
     * 初始化数据
     */

    private void initData()
    {
        mUtils = new DownloadUtils(5, "setup.apk", URL, this);
        mUtils.setOnDownloadListener(new DownloadUtils.OnDownloadListener()
        {
            @Override
            public void downloadStart(int fileSize)
            {
                size = fileSize;
                Logger.d(fileSize);
                mPbDown.setMax(size);
                mBtnDown.setEnabled(false);
                mBtnDelete.setEnabled(false);
            }

            @Override
            public void downloadProgress(int downloadedSize)
            {
                mPbDown.setProgress(downloadedSize);
                mTvUpdate.setText(Formatter.formatFileSize(MainActivity.this, downloadedSize)
                        + "/" + Formatter.formatFileSize(MainActivity.this, size));
            }

            @Override
            public void downloadEnd()
            {
                mBtnDelete.setEnabled(true);
                Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_LONG).show();
            }
        });

    }

}
