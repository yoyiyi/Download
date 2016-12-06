package com.yoyiyi.download.entities;

/**
 * Created by yoyiyi on 2016/12/1.
 */
public class DownloadInfo
{
    private int threadId;//线程id
    private int startPos;//开始位置
    private int endPos;//结束位置
    private int size;//完成大小
    private String url;//url



    public DownloadInfo(int threadId, int startPos, int endPos,
                        int size, String url)
    {
        this.threadId = threadId;
        this.startPos = startPos;
        this.endPos = endPos;
        this.size = size;
        this.url = url;
    }
    public int getEndPos()
    {
        return endPos;
    }

    public void setEndPos(int endPos)
    {
        this.endPos = endPos;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getStartPos()
    {
        return startPos;
    }

    public void setStartPos(int startPos)
    {
        this.startPos = startPos;
    }

    public int getThreadId()
    {
        return threadId;
    }

    public void setThreadId(int threadId)
    {
        this.threadId = threadId;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }



    @Override
    public String toString()
    {
        return "DownloadInfo{" +
                "endPos=" + endPos +
                ", threadId=" + threadId +
                ", startPos=" + startPos +
                ", size=" + size +
                ", url='" + url + '\'' +
                '}';
    }
}
