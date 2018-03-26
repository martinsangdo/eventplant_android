package com.eventplant.ep.eventplant1;


import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity implements  Runnable{

    public MainActivity.phpDown ckDB;
    public CaptureActivity captureAct;
    public SQLiteDatabase db;
    String databaseName = "EP";
    String tableName = "booth";
    TextView status;

    TextView txtView;

    EditText ID, PW;
    Button loginButton;
    String loginName, loginPass;
    int cam_idx;
    String b_name, b_id, b_pw, auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtView = (TextView) findViewById(R.id.txtView);

        ID=(EditText)findViewById(R.id.ID); //id
        PW=(EditText)findViewById(R.id.PW); //pw
        loginButton=(Button)findViewById(R.id.loginBtn); //로그인버튼
        //SharedPreferences를 통한 저장된 로그인 정보가 있는지 확인
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        loginName = auto.getString("login ID",null);
        loginPass = auto.getString("Password",null);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //쓰레드 생성 후 서버와 통신

                String g_id = ID.getText().toString();
                String g_pw = PW.getText().toString();
                String newURL = "http://sunpyo89.cafe24.com/ep_new/app/insert.php?b_id="+g_id+"&b_pw="+g_pw;

                ckDB.execute(newURL);

            }
        });
    }
    public void run(){
        String url = "http://url";
        //입력값
        String g_id = ID.getText().toString();
        String g_pw = PW.getText().toString();

    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(LoginActivity.this, "이름과 전화번호를 확인하세요", Toast.LENGTH_SHORT).show();
        }
    };

}
