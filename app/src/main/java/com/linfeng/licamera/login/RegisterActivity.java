package com.linfeng.licamera.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.linfeng.licamera.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText regUserName;
    private EditText regPassWord;
    private Button btn_reg;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //初始化
        regUserName = (EditText) findViewById(R.id.regUserName);
        regPassWord = (EditText) findViewById(R.id.regPassWord);
        btn_reg = (Button) findViewById(R.id.btn_reg);

        btn_reg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reg:
                dialog = new ProgressDialog(RegisterActivity.this);
                dialog.setTitle("正在注册");
                dialog.setMessage("请稍后");
                dialog.show();

                new Thread(new RegThread()).start();
                break;
        }
    }

    public class RegThread implements Runnable {
        @Override
        public void run() {

            //获取服务器返回数据
            //String RegRet = WebServiceGet.executeHttpGet(regUserName.getText().toString(),regPassWord.getText().toString(),"RegLet");
            String RegRet = WebServicePost.executeHttpPost(regUserName.getText().toString(), regPassWord.getText().toString(), "RegisterServlet");

            //更新UI，界面处理
            showReq(RegRet);
        }
    }

    private void showReq(final String RegRet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (RegRet.equals("true")) {
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("注册信息");
                    builder.setMessage("注册成功");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                } else {
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("注册信息");
                    builder.setMessage("注册失败");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
            }
        });
    }
}
