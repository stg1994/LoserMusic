package com.losermusic;

import android.app.Dialog;
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
import com.losermusic.db.SessionManager;
import com.losermusic.progressDialog.CustomProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stgqq on 2017/4/8.
 */

public class DeleteUserActivity extends AppCompatActivity {
    private Button btn_delete;
    private EditText del_username;
    private EditText del_password;
    private SessionManager session;
    private static final String TAG = DeleteUserActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedIntanceState){
        super.onCreate(savedIntanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//不在活动中显示标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏通知栏
        setContentView(R.layout.activity_deleteuser);

        btn_delete = (Button) findViewById(R.id.btn_delete);
        del_username = (EditText) findViewById(R.id.del_username);
        del_password = (EditText) findViewById(R.id.del_password);

        //会话管理器
        session = new SessionManager(getApplicationContext());

        //点击删除按钮事件
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = del_username.getText().toString().trim();
                String password  = del_password.getText().toString().trim();
                //检查窗体中的数据是否为空
                if (!username.isEmpty() && !password.isEmpty()){
                    checkDelete(username,password);
                }else {
                    //提示用户输入完整的数据
                    Toast.makeText(getApplicationContext(),"请输入完整数据",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //功能验证数据库的删除用户信息
    private void checkDelete(final String username,final String password){
        String tag_string_req = "req_delete";
        final Dialog pDailog = CustomProgressDialog.createLoadingDialog(this,"正在删除...");
        pDailog.setCancelable(false);//进度条不允许用“返回键”取消
        pDailog.show();//显示进度条
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_DELETE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Delete: " + response.toString());
                pDailog.dismiss();//关闭进度条
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // 检查JSON错误节点
                    if (!error) {
                        session.setLogin(true);
                       Toast.makeText(getApplicationContext(),"删除用户成功",Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Delete Error: " + error.getMessage());
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
