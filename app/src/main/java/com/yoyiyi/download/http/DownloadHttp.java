package com.yoyiyi.download.http;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.orhanobut.logger.Logger;
import com.yoyiyi.download.db.DBDao;
import com.yoyiyi.download.entities.DownloadInfo;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by yoyiyi on 2016/12/1.
 */
public class DownloadHttp
{
    //线程数量
    private int threadCount;
    //URL地址
    private String url;
    private Context mContext;
    private Handler mHandler;
    //保存下载信息的类
    private List<DownloadInfo> downloadInfos;
    //目录
    private String localPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/ADownload";
    //文件名
    private String fileName;
    //文件大小
    private int fileSize;
    //文件信息保存的数据库操作类
    private DBDao mDBDao;
    //已经下载文件大小
    private int mFinishedSize = 0;
    //线程池
    public static ExecutorService sExecutorService = Executors.newFixedThreadPool(5);

    /**
     * 下载状态
     */
    private enum Download_State
    {
        DOWNLOADING, PAUSE, START
    }

    //默认开始开始
    private Download_State state = Download_State.START;


    public DownloadHttp(int threadCount,
                        String url,
                        String fileName,
                        Context context,
                        Handler handler)
    {
        super();
        this.threadCount = threadCount;
        this.url = url;
        this.mContext = context;
        this.mHandler = handler;
        this.fileName = fileName;
        mDBDao = new DBDao(mContext);
    }

    /**
     * 准备下载
     */
    public void ready()
    {
        mFinishedSize = 0;
        downloadInfos = mDBDao.select(url);
        //判断文件是否存在
        if (downloadInfos.size() == 0)
        {
            initThread();
        } else
        {
            File file = new File(localPath + File.separator + fileName);
            if (!file.exists())
            {
                //如果文件不存在 第一次下载 先删除过去信息
                mDBDao.delete(url);
                initThread();
            } else
            {   //否则断点下载
                fileSize = downloadInfos.get(downloadInfos.size() - 1)
                        .getEndPos();
                for (DownloadInfo info : downloadInfos)
                {
                    mFinishedSize += info.getSize();
                }
            }
        }
    }

    /**
     * 开始下载
     */
    public void start()
    {
        if (downloadInfos != null)
        {
            if (state == Download_State.DOWNLOADING)
            {
                return;
            }
            state = Download_State.DOWNLOADING;
            for (DownloadInfo info : downloadInfos)
            {
                DownloadThread dt = new DownloadThread(info.getThreadId(),
                        info.getStartPos(),
                        info.getEndPos(),
                        info.getSize(),
                        info.getUrl());
                sExecutorService.execute(dt);
            }
        }
    }

    /**
     * 暂停下载
     */
    public void pause()
    {
        state = Download_State.PAUSE;
        //
        mDBDao.closeDb();
    }

    /**
     * 删除文件
     */
    public void delete()
    {
        compelete();
        File file = new File(localPath + File.separator + fileName);
        file.delete();
    }

    /**
     * 完成下载
     */
    public void compelete()
    {
        state = Download_State.START;
        mDBDao.delete(url);
        mDBDao.closeDb();
    }

    /**
     * 获取文件大小
     *
     * @return
     */
    public int getFileSize()
    {
        return fileSize;
    }

    /**
     * 获取结束文件大小
     *
     * @return
     */
    public int getFinishSize()
    {
        return mFinishedSize;
    }


    /**
     * 初始化下载
     */
    private void initThread()
    {
        try
        {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            //文件长度
            fileSize = connection.getContentLength();
            File fileParent = new File(localPath);
            //判断文件夹是否存在
            if (!fileParent.exists())
            {
                fileParent.mkdir();
                Logger.d("文件夹：" + fileParent.getAbsolutePath());
            }

            File file = new File(fileParent, fileName);
            //判断文件是否存在
            if (!file.exists())
            {
                file.createNewFile();
                //  Logger.d("文件路径：" + file.getAbsolutePath());
            }
            // 在本地生成一个一样大小文件
            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
            //设置本地文件大小
            accessFile.setLength(fileSize);
            accessFile.close();
            connection.disconnect();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        //分成几段下载 每一段的大小
        int range = fileSize / threadCount;
        downloadInfos = new ArrayList<>();
        //储存每一段的下载信息
        for (int i = 0; i < threadCount - 1; i++)
        {
            DownloadInfo info = new DownloadInfo(i, i * range, (i + 1) * range
                    - 1, 0, url);
            downloadInfos.add(info);
        }
        //储存最后一段信息
        DownloadInfo info = new DownloadInfo(threadCount - 1, (threadCount - 1)
                * range, fileSize - 1, 0, url);
        // Logger.d("线程id：" + (threadCount - 1));
        downloadInfos.add(info);
        //储存进数据库
        mDBDao.insert(downloadInfos);
    }

    //自定义下载线程
    private class DownloadThread extends Thread
    {
        //线程id
        private int threadId;
        //开始位置
        private int startPos;
        //结束位置
        private int endPos;
        //完成大小
        private int finishedSize;
        //文件url
        private String url;
        //一共大小
        private int totalThreadSize;

        public DownloadThread(int threadId, int startPos, int endPos,
                              int finishedSize, String url)
        {
            this.threadId = threadId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.totalThreadSize = endPos - startPos + 1;
            this.url = url;
            this.finishedSize = finishedSize;
        }

        @Override
        public void run()
        {
            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream is = null;
            try
            {
                randomAccessFile = new RandomAccessFile(localPath + File.separator
                        + fileName, "rwd");
                randomAccessFile.seek(startPos + finishedSize);
                URL url = new URL(this.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");

                connection.setRequestProperty("Range", "bytes="
                        + (startPos + finishedSize) + "-" + endPos);
                if (connection.getResponseCode() == 206)
                {
                    is = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 8];
                    int length = -1;
                    while ((length = is.read(buffer)) != -1)
                    {
                        randomAccessFile.write(buffer, 0, length);
                        finishedSize += length;
                        //发送Message
                        Message message = Message.obtain();
                        message.what = threadId;
                        message.obj = this.url;
                        message.arg1 = length;

                        mHandler.sendMessage(message);
                        //更新数据库
                        mDBDao.update(threadId, finishedSize, this.url);
                        if (finishedSize >= totalThreadSize)
                        {
                            break;
                        }
                        if (state != Download_State.DOWNLOADING)
                        {
                            break;
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (is != null)
                    {
                        is.close();
                    }
                    randomAccessFile.close();
                    connection.disconnect();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
