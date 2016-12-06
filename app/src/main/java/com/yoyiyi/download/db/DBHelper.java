package com.yoyiyi.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yoyiyi on 2016/12/1.
 */
public class DBHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "download.db";
    private static final int DB_VERSION = 4;
    private static final String DB_CREATE = "create table download_info(_id integer primary key autoincrement, "
            + "thread_id integer, "//线程id
            + "start_pos integer,"//开始下载位置
            + "end_pos integer, "//结束位置
            + "file_size integer,"//完成下载文件大小
            + "url char)";//文件地址
    private static final String DB_DROP = "drop table download_info";

    public DBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DB_DROP);
        db.execSQL(DB_CREATE);
    }
}
