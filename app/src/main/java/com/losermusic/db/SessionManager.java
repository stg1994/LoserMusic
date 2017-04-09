package com.losermusic.db;

/**
 * Created by STG on 2016/6/8.
 * 登陆连接
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    //日志标签
    private static String TAG = SessionManager.class.getSimpleName();
    // 共享资源
    SharedPreferences pref;
    Editor editor;
    Context _context;
    // 共享优先模式
    int PRIVATE_MODE = 0;
    // 共享首选项文件名
    private static final String PREF_NAME = "AndroidHiveLogin";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        // 提交更改
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}
