package com.losermusic;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.losermusic.db.AppConfig;
import com.losermusic.db.AppController;
import com.losermusic.db.SQLiteHandler;
import com.losermusic.db.SessionManager;
import com.losermusic.progressDialog.CustomProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stgqq on 2017/3/31.
 */

public class LoginActivity extends AppCompatActivity {
    //APP启动页面
    private ViewFlipper allFlipper;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            //TODO Auto-generated method stub
            switch (msg.what){
                case 1:
                    //切换到主页面
                    allFlipper.setDisplayedChild(1);
                    break;
            }
        }
    };


    private Button btn_login;
    private Button btn_register;
    private TextView tv;
    private SessionManager session;
    private SQLiteHandler db;
    private EditText et_users;
    private EditText et_pwd;
    private static final String TAG = LoginActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//不在活动中显示标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏通知栏
        setContentView(R.layout.activity_login);

        //APP启动页面
        allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);//给UI主线程发送消息
            }
        },2000);//启动等待3秒




        btn_login = (Button) findViewById(R.id.btn_login);
        tv = (TextView) findViewById(R.id.tv_fp);
        btn_register = (Button) findViewById(R.id.btn_register);
        et_users = (EditText) findViewById(R.id.et_users);
        et_pwd = (EditText) findViewById(R.id.et_pwd);

        //SQLite数据库处理程序
        db = new SQLiteHandler(getApplicationContext());
        //会话管理器
        session = new SessionManager(getApplicationContext());
        //检查用户是否已登录
        if (session.isLoggedIn()){
            //用户已登录，跳转到主页面
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }




        //登录按钮点击事件
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = et_users.getText().toString().trim();
                String password  = et_pwd.getText().toString().trim();
                //检查窗体中的空数据
                if (!username.isEmpty() && !password.isEmpty()){
                    //登录用户
                    checkLogin(username,password);
                }else {
                    //提示用输入完整数据
                    Toast.makeText(getApplicationContext(),"请输入完整的信息",Toast.LENGTH_LONG).show();
                }

            }
        });

        //忘记密码点击事件
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(LoginActivity.this,ChangePwdActivity.class);
               // startActivity(intent);
            }
        });
        //注册按钮点击事件
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });


    }



    //功能验证数据库的登录信息
    private void checkLogin(final String username,final String password){
        // 用于取消请求的标记
        String tag_string_req = "req_login";
        final Dialog pDailog = CustomProgressDialog.createLoadingDialog(this,"正在加载...");
        pDailog.setCancelable(false);//进度条不允许用“返回键”取消
        pDailog.show();//显示进度条
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                pDailog.dismiss();//关闭进度条
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // 检查JSON错误节点
                    if (!error) {
                        // 用户成功登录
                        // 创建登录会话
                        session.setLogin(true);
                        // 存储数据到SQLite
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String username = user.getString("username");
                        String created_at = user.getString("created_at");
                        // 在用户表中插入行
                        db.addUser(username, uid, created_at);
                        //跳转到主页面
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 登录错误，获取错误消息
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON错误
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                pDailog.dismiss();//关闭进度条
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // 发布参数到登录网址
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        // 请求队列添加请求
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
