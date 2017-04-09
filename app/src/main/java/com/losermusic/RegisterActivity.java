package com.losermusic;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText inputusername;
    private EditText inputpassword;
    private Button btnRegister;
    private SessionManager session;
    private SQLiteHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏通知栏
        setContentView(R.layout.activity_register);

        inputusername = (EditText) findViewById(R.id.re_username);
        inputpassword = (EditText) findViewById(R.id.re_password);
        btnRegister   = (Button)   findViewById(R.id.btnRegister);

        //进度对话框
        Dialog pDialog = CustomProgressDialog.createLoadingDialog(this,"正在加载中...");
        pDialog.setCancelable(true);//允许“返回键取消”
        //会话管理器
        session = new SessionManager(getApplicationContext());
        //SQLite数据库处理程序
        db = new SQLiteHandler(getApplicationContext());
        //检查用户是否登录
        if (session.isLoggedIn()){
            //用户已登录，跳转到主界面
            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = inputusername.getText().toString().trim();
                String password = inputpassword.getText().toString().trim();
                if (!username.isEmpty() && !password.isEmpty()){
                    registerUser(username,password);
                }else {
                    Toast.makeText(getApplicationContext(),"请输入你的完整信息",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    //功能存储在MYSQL数据库将参数（标签，姓名，密码）登记网址
    private void registerUser(final String username,final String password){
        //用于取消请求标签
        String tag_string_req = "req_register";
        final Dialog pDialog = CustomProgressDialog.createLoadingDialog(this,"正在加载中...");
        pDialog.setCancelable(true);//允许“返回键取消”
        pDialog.show();
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"Register Response: " + response.toString());
                pDialog.dismiss();//进度条关闭
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error){
                        //用户成功储存在MYSQL
                        //储存用户到SQLite
                        String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String username = user.getString("username");
                        String created_at = user.getString("created_at");
                        //在用户表中插入
                        db.addUser(username,uid,created_at);
                        session.setLogin(true);
                        //启动登录页面
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        //注册错误时，获取错误信息
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),errorMsg,Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.e(TAG,"Registeratipn Error: " + error.getMessage());
                byte[] htmlBodyBytes = error.networkResponse.data;
                Log.e("Register-Error",new String(htmlBodyBytes));
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                pDialog.dismiss();//进度条关闭
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                //发布参数来注册网址
                Map<String,String> params = new HashMap<String, String>();
                params.put("username",username);
                params.put("password",password);
                return params;
                }
            };
        //请求队列添加请求
        AppController.getInstance().addToRequestQueue(strReq,tag_string_req);
        }
}
