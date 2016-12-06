package com.yoyiyi.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yoyiyi.download.entities.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoyiyi on 2016/12/1.
 */
public class DBDao
{
    private DBHelper mDBHelper;

    public DBDao(Context context)
    {
        mDBHelper = new DBHelper(context);
    }

    /**
     * 新建
     */
    public void insert(List<DownloadInfo> infos)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        for (DownloadInfo info : infos)
        {
            String sql = "insert into download_info(thread_id,start_pos, end_pos,file_size,url) values (?,?,?,?,?)";
            database.execSQL(sql, new Object[]{
                    info.getThreadId(), info.getStartPos(),
                    info.getEndPos(), info.getSize(), info.getUrl()});
        }
    }

    /**
     * 查询
     */
    public List<DownloadInfo> select(String url)
    {
        List<DownloadInfo> list = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String sql = "select thread_id, start_pos, end_pos,file_size,url from download_info where url=?";
        Cursor cursor = database.rawQuery(sql, new String[]{url});
        while (cursor.moveToNext())
        {
            DownloadInfo info = new DownloadInfo(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                    cursor.getString(4));
            list.add(info);
        }
        return list;
    }

    /**
     * 更新
     */
    public void update(int threadId, int size, String urlstr)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String sql = "update download_info set file_size=? where thread_id=? and url=?";
        database.execSQL(sql,new Object[]{size, threadId, urlstr});
    }

    /**
     * 关闭数据库
     */
    public void closeDb()
    {
        mDBHelper.close();
    }

    /**
     * 删除数据库中的数据
     */
    public void delete(String url)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.delete("download_info", "url=?", new String[]{url});
    }
}
