package com.linfeng.licamera.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.linfeng.licamera.R;
import com.linfeng.licamera.util.CommonUtil;
import com.linfeng.licamera.util.Constant;
import com.linfeng.licamera.util.SPUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int RESULT_OK = 1;
    private EditText username;
    private EditText password;
    private Button login;
    private TextView register;
    private String mUserName;
    //提示框
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //初始化信息
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.btn_login);
        register = (TextView) findViewById(R.id.register);

        //设置按钮监听器
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                //设置提示框
                dialog = new ProgressDialog(LoginActivity.this);
                dialog.setTitle("正在登陆");
                dialog.setMessage("请稍后");
                dialog.setCancelable(false);//设置可以通过back键取消
                dialog.show();
                if (TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
                    dialog.dismiss();
                    Toast.makeText(this,"请输入完整信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                //设置子线程，分别进行Get和Post传输数据
                new Thread(new LoginInfo()).start();

                break;
            case R.id.register:
                //跳转注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }

    public class LoginInfo implements Runnable {
        @Override
        public void run() {
            mUserName = username.getText().toString();
            String attr = "?username=" + mUserName + "&password=" + password.getText().toString();
            String infoString = WebServiceGet. executeHttpGet("LoginServlet", attr);//获取服务器返回的数据
            //更新UI，使用runOnUiThread()方法
            showResponse(infoString);

            attr = "?username=" + mUserName;
            infoString = WebServiceGet.executeHttpGet("UsageInfoServlet", attr);//获取服务器返回的数据
            String message;
            if(infoString != null) {
                String[] usage = infoString.split(",");
                SPUtils.putInt(Constant.PRODUCE_PHOTO_COUNT,Integer.parseInt(usage[0]),CommonUtil.context());
                SPUtils.putInt(Constant.PRODUCE_VIDEO_COUNT,Integer.parseInt(usage[1]),CommonUtil.context());
                message = "数据同步完毕";
            } else {
                message = "数据同步失败";
            }
            runOnUiThread(() -> Toast.makeText(CommonUtil.context(),message,Toast.LENGTH_SHORT).show());
        }
    }

    private void showResponse(final String response) {
        //更新UI
        runOnUiThread(() -> {
            if (response == null || response.equals("false")) {
                Toast.makeText(LoginActivity.this, "登陆失败！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                SPUtils.putString("userName", mUserName, CommonUtil.context());
                Intent intent = new Intent();
                LoginActivity.this.setResult(RESULT_OK,intent);
                finish();
            }
            dialog.dismiss();
        });
    }
}