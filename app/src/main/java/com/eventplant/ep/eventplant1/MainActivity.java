package com.eventplant.ep.eventplant1;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {
    String dbResult;

    EditText ID, PW;
    Button loginButton;
    String loginName, loginPass;
    ImageView logo_img;
    TextView txtView;
    MainActivity.phpDown task;
    String newURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logo_img = (ImageView) findViewById(R.id.logo_img);
        txtView = (TextView) findViewById(R.id.txtView);
        TextView txt1 = (TextView) findViewById(R.id.txt1);
        TextView txt2 = (TextView) findViewById(R.id.txt2);
        ID=(EditText)findViewById(R.id.IDe); //id
        PW=(EditText)findViewById(R.id.PWe); //pw
        loginButton=(Button)findViewById(R.id.loginBtn); //로그인버튼
        //SharedPreferences를 통한 저장된 로그인 정보가 있는지 확인
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        loginName = auto.getString("login ID",null);
        loginPass = auto.getString("Password",null);
        logo_img.setImageResource(R.drawable.ep_logo);

        if(loginName !=null && loginPass != null) { //저장된 정보가 있으면 액티비티 전환
            Toast.makeText(MainActivity.this, loginName +"님 자동로그인 입니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
            finish();
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //쓰레드 생성 후 서버와 통신
                task = new phpDown();

                String g_id = ID.getText().toString();
                String g_pw = PW.getText().toString();
                newURL = "http://eventplant.cafe24.com/app/insert.php?b_id="+g_id+"&b_pw="+g_pw;

                task.execute(newURL);

            }
        });


    }

    public class phpDown extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... urls) {
            StringBuilder jsonHtml = new StringBuilder();
            try {
                // 연결 url 설정
                URL url = new URL(urls[0]);
                // 커넥션 객체 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 연결되었으면.
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);
                    // 연결되었음 코드가 리턴되면.
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        for (; ; ) {
                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                            String line = br.readLine();
                            if (line == null) break;
                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            dbResult = jsonHtml.toString();
            return jsonHtml.toString();

        }
        protected void onPostExecute(String str) {
            if(str != null){
                final String cam_idx;
                final String b_name;
                final String b_id;
                final String b_pw;
                final String auth;
                int count;

                try{

                    JSONObject root = new JSONObject(str);
                    JSONObject ja = root.getJSONObject("results");

                    cam_idx = ja.getString("cam_idx");
                    b_name = ja.getString("b_name");
                    b_id = ja.getString("b_id");
                    b_pw = ja.getString("b_pw");
                    auth = ja.getString("auth");
                    Toast.makeText(getApplicationContext(), b_name+"님 환영합니다", Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Intent intents = new Intent(MainActivity.this, ListActivity.class);
                            intents.putExtra("cam_idx", cam_idx);     // 데이터 보내기(전송) - it_id 에 str1 의 값을 실어 보냄
                            intents.putExtra("b_name", b_name);
                            intents.putExtra("b_id", b_id);
                            intents.putExtra("auth", auth);
                            startActivity(intents);
                            finish();
                        }
                    },0);

                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "로그인 실패! 확인 후 다시 로그인해주세요", 2000).show();
                }
            }else{
            }

        }

    }


}