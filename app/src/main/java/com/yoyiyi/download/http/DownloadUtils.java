package com.yoyiyi.download.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.orhanobut.logger.Logger;

/**
 * Created by yoyiyi on 2016/12/1.
 */
public class DownloadUtils
{
    private DownloadHttp mDownloadHttp;
    private OnDownloadListener onDownloadListener;//下载监听
    //文件长度
    private int fileSize;
    //一共下载长度
    private int downloadedSize = 0;
    private DownloadTask mTask;

    private Handler mHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            int length = msg.arg1;
            synchronized (this)
            {
                downloadedSize += length;
            }
            if (onDownloadListener != null)
            {
                onDownloadListener.downloadProgress(downloadedSize);
            }
            //如果下载长度大于等于文件长度
            if (downloadedSize >= fileSize)
            {
                //下载完成
                mDownloadHttp.compelete();
                if (onDownloadListener != null)
                {
                    onDownloadListener.downloadEnd();
                }
            }
        }

    };

    public DownloadUtils(int threadCount, String filename,
                         String urlString, Context context)
    {

        mDownloadHttp = new DownloadHttp(threadCount, urlString,
                filename, context, mHandler);
    }

    //调用ready方法获得文件大小信息，之后调用开始方法
    public void start()
    {
        mTask = new DownloadTask();
        mTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }

    class DownloadTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            if (!mTask.isCancelled())
            {
                mDownloadHttp.ready();
                Logger.d("开始测试");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            fileSize = mDownloadHttp.getFileSize();
            downloadedSize = mDownloadHttp.getFinishSize();
            if (onDownloadListener != null)
            {

                onDownloadListener.downloadStart(fileSize);
            }
            mDownloadHttp.start();
            Logger.d("开始执行");
        }
    }


    /**
     * 暂停
     */
    public void pause()
    {
        mDownloadHttp.pause();
    }

    /**
     * 删除
     */
    public void delete()
    {
        mDownloadHttp.delete();
        mTask.cancel(true);

    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener)
    {
        this.onDownloadListener = onDownloadListener;
    }

    //下载回调接口
    public interface OnDownloadListener
    {    //开始下载
        void downloadStart(int fileSize);

        //进度条
        void downloadProgress(int downloadedSize);

        //结束下载
        void downloadEnd();
    }
}
