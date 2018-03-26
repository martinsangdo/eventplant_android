package com.eventplant.ep.eventplant1;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ListActivity extends Activity implements AdapterView.OnItemSelectedListener{
    String databaseName = "EP";
    String tableName = "visitor";
    String dbResult;
    int count;
    EditText ID, PW, editSearch;
    Button barcode_btn, stat_btn, search_btn, down_btn;
    String loginName, loginPass, selected;

    SQLiteDatabase db;
    TextView booth_name, count_info;
    TextView tv0, tv1,tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10;



    ListActivity.phpDown2 task2;
    String newURL2, auth;
    TableLayout stk;
    TableRow tbrow0;
    SwipeRefreshLayout mSwipeRefreshLayout; // 클래스에 변수 선언
    TableRow tbrow;
    private BackPressedForFinish backPressedForFinish;

    String[] items={"성명","관리번호","소속"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list2);
        booth_name = (TextView) findViewById(R.id.booth_name);
        count_info = (TextView) findViewById(R.id.count_info);
        barcode_btn = (Button) findViewById(R.id.barcode_btn);
        stat_btn = (Button) findViewById(R.id.stat_btn);
        search_btn = (Button) findViewById(R.id.search_btn);
        down_btn = (Button) findViewById(R.id.down_btn);
        editSearch = (EditText) findViewById(R.id.editSearch);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        tv0 = (TextView) findViewById(R.id.tv0);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);
        tv5 = (TextView) findViewById(R.id.tv5);
        tv6 = (TextView) findViewById(R.id.tv6);
        tv7 = (TextView) findViewById(R.id.tv7);
        tv8 = (TextView) findViewById(R.id.tv8);
        tv9 = (TextView) findViewById(R.id.tv9);
        tv10 = (TextView) findViewById(R.id.tv10);
        task2 = new phpDown2();

        Intent intentx = this.getIntent();
        final String cam_idx = intentx.getStringExtra("cam_idx");
        final String b_name = intentx.getStringExtra("b_name");
        final String b_id = intentx.getStringExtra("b_id");
        auth = intentx.getStringExtra("auth");

        createDatabase(databaseName);
        createTable(tableName);

        stk = (TableLayout) findViewById(R.id.table_main);
        tbrow0 = (TableRow) findViewById(R.id.tbrow_top);

        booth_name.setText(b_name);

        newURL2 = "http://eventplant.cafe24.com/app/appdata.php?cam_idx="+cam_idx+"&b_id="+b_id;
        task2.execute(newURL2);


        backPressedForFinish = new BackPressedForFinish(this);



        barcode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //쓰레드 생성 후 서버와 통신
                Intent intentsr = new Intent(ListActivity.this, CaptureActivity.class);
                intentsr.putExtra("cam_idx", cam_idx);     // 데이터 보내기(전송) - it_id 에 str1 의 값을 실어 보냄
                intentsr.putExtra("b_id", b_id);
                startActivity(intentsr);

            }
        });
        stat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //쓰레드 생성 후 서버와 통신

                Intent intentsr = new Intent(ListActivity.this, WebActivity.class);
                intentsr.putExtra("cam_idx", cam_idx);     // 데이터 보내기(전송) - it_id 에 str1 의 값을 실어 보냄
                intentsr.putExtra("b_id", b_id);
                startActivity(intentsr);

            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //쓰레드 생성 후 서버와 통신
            String strSearch = editSearch.getText().toString();
            searchData(selected, strSearch);
            }
        });
//        down_btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intentstr = new Intent(ListActivity.this, DownActivity.class);
//                intentstr.putExtra("cam_idx", cam_idx);     // 데이터 보내기(전송) - it_id 에 str1 의 값을 실어 보냄
//                intentstr.putExtra("b_id", b_id);
//                startActivity(intentstr);
//            }
//        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stk.removeAllViews();
                stk.addView(tbrow0);
                //새로고침시 실행 할 작업 실행...
                task2 = new phpDown2();
                task2.execute(newURL2);

                mSwipeRefreshLayout.setRefreshing(false);

            }
        });

    }
    public void searchData(String strWhere, String str){
        String num;
        String name;
        String company;
        String b_num;
        String jtype;
        String dept;
        String position;
        String hp;
        String tel;
        String email;
        stk.removeAllViews();
        stk.addView(tbrow0);
        String sqlStatement = String.format("SELECT num, name, jtype, company, event, dept, position, hp, tel, email FROM visitor WHERE %s LIKE '%s' and event != '미응모' order by event desc",strWhere,"%"+str+"%");
        String sql2 = "select num, name, jtype, company, event, dept, position, hp, tel, email from "+tableName+" where "+strWhere+" like ?";
        String[] sqlQuery = {"%"+str+"%"};
        Cursor outCursor2 = db.rawQuery(sqlStatement,null);
        int recordCount = outCursor2.getCount();
        for(int t=0; t<recordCount; t++){
            outCursor2.moveToNext();
            num = outCursor2.getString(0);
            name = outCursor2.getString(1);
            jtype = outCursor2.getString(2);
            company = outCursor2.getString(3);
            b_num = outCursor2.getString(4);
            dept = outCursor2.getString(5);
            position = outCursor2.getString(6);
            hp = outCursor2.getString(7);
            tel = outCursor2.getString(8);
            email = outCursor2.getString(9);
            tbrow = new TableRow(ListActivity.this);
            TextView t1v = new TextView(ListActivity.this);
            t1v.setText(String.valueOf(recordCount-t));
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);
            TextView t2v = new TextView(ListActivity.this);
            t2v.setText(String.valueOf(num));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            tbrow.addView(t2v);
            TextView t3v = new TextView(ListActivity.this);
            t3v.setText(name);
            t3v.setTextColor(Color.BLACK);
            t3v.setPadding(30,0,30,0);
            t3v.setGravity(Gravity.CENTER);
            tbrow.addView(t3v);
            TextView t6v = new TextView(ListActivity.this);
            t6v.setText(jtype+" ");
            t6v.setTextColor(Color.BLACK);
            t6v.setGravity(Gravity.CENTER);
            tbrow.addView(t6v);
            TextView t4v = new TextView(ListActivity.this);
            t4v.setText(company+" ");
            t4v.setTextColor(Color.BLACK);
            t4v.setPadding(30,0,30,0);
            t4v.setGravity(Gravity.CENTER);
            tbrow.addView(t4v);
            TextView t5v = new TextView(ListActivity.this);
            t5v.setText(b_num+" ");
            t5v.setTextColor(Color.BLACK);
            t5v.setGravity(Gravity.CENTER);
            tbrow.addView(t5v);
            TextView t7v = new TextView(ListActivity.this);
            t7v.setText(dept+" ");
            t7v.setTextColor(Color.BLACK);
            t7v.setPadding(10,0,10,0);
            t7v.setGravity(Gravity.CENTER);
            tbrow.addView(t7v);
            TextView t8v = new TextView(ListActivity.this);
            t8v.setText(position+" ");
            t8v.setTextColor(Color.BLACK);
            t8v.setGravity(Gravity.CENTER);
            tbrow.addView(t8v);
            TextView t9v = new TextView(ListActivity.this);
            t9v.setText(hp+" ");
            t9v.setTextColor(Color.BLACK);
            t9v.setPadding(10,0,10,0);
            t9v.setGravity(Gravity.CENTER);
            tbrow.addView(t9v);
            TextView t10v = new TextView(ListActivity.this);
            t10v.setText(tel+" ");
            t10v.setTextColor(Color.BLACK);
            t10v.setGravity(Gravity.CENTER);
            tbrow.addView(t10v);
            TextView t11v = new TextView(ListActivity.this);
            t11v.setText(email+" ");
            t11v.setTextColor(Color.BLACK);
            t11v.setPadding(10,0,0,0);
            t11v.setGravity(Gravity.CENTER);
            tbrow.addView(t11v);
            if(t%2==0) {
                t1v.setBackgroundColor(Color.rgb(223,223,223));
                t2v.setBackgroundColor(Color.rgb(223,223,223));
                t3v.setBackgroundColor(Color.rgb(223,223,223));
                t6v.setBackgroundColor(Color.rgb(223,223,223));
                t4v.setBackgroundColor(Color.rgb(223,223,223));
                t5v.setBackgroundColor(Color.rgb(223,223,223));
                t7v.setBackgroundColor(Color.rgb(223,223,223));
                t8v.setBackgroundColor(Color.rgb(223,223,223));
                t9v.setBackgroundColor(Color.rgb(223,223,223));
                t10v.setBackgroundColor(Color.rgb(223,223,223));
                t11v.setBackgroundColor(Color.rgb(223,223,223));
            }
            stk.addView(tbrow);
        }
    }
    public void viewData(){

        String sql = "select num, name, jtype, company, event, dept, position, hp, tel, email from "+tableName+" where event != '미응모' order by event desc";
        Cursor outCursor = db.rawQuery(sql,null);
        int recordCount = outCursor.getCount();
        if(recordCount<1){

        }
        for(int k=0; k<recordCount; k++){
            outCursor.moveToNext();
            //Toast.makeText(getApplicationContext(),sql+recordCount,Toast.LENGTH_SHORT).show();
            int count_p = outCursor.getPosition();
            String num = outCursor.getString(0);
            String name = outCursor.getString(1);
            String jtype = outCursor.getString(2);
            String company = outCursor.getString(3);
            String b_num = outCursor.getString(4);
            String dept = outCursor.getString(5);
            String position = outCursor.getString(6);
            String hp = outCursor.getString(7);
            String tel = outCursor.getString(8);
            String email = outCursor.getString(9);
            tbrow = new TableRow(ListActivity.this);
            TextView t1v = new TextView(ListActivity.this);
            t1v.setText(String.valueOf(recordCount-k));
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);
            TextView t2v = new TextView(ListActivity.this);
            t2v.setText(String.valueOf(num));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            tbrow.addView(t2v);
            TextView t3v = new TextView(ListActivity.this);
            t3v.setText(name);
            t3v.setTextColor(Color.BLACK);
            t3v.setPadding(30,0,30,0);
            t3v.setGravity(Gravity.CENTER);
            tbrow.addView(t3v);
            TextView t6v = new TextView(ListActivity.this);
            t6v.setText(jtype+" ");
            t6v.setTextColor(Color.BLACK);
            t6v.setGravity(Gravity.CENTER);
            tbrow.addView(t6v);
            TextView t4v = new TextView(ListActivity.this);
            t4v.setText(company+" ");
            t4v.setTextColor(Color.BLACK);
            t4v.setPadding(30,0,30,0);
            t4v.setGravity(Gravity.CENTER);
            tbrow.addView(t4v);
            TextView t5v = new TextView(ListActivity.this);
            t5v.setText(b_num+" ");
            t5v.setTextColor(Color.BLACK);
            t5v.setGravity(Gravity.CENTER);
            tbrow.addView(t5v);
            TextView t7v = new TextView(ListActivity.this);
            t7v.setText(dept+" ");
            t7v.setTextColor(Color.BLACK);
            t7v.setPadding(10,0,10,0);
            t7v.setGravity(Gravity.CENTER);
            tbrow.addView(t7v);
            TextView t8v = new TextView(ListActivity.this);
            t8v.setText(position+" ");
            t8v.setTextColor(Color.BLACK);
            t8v.setGravity(Gravity.CENTER);
            tbrow.addView(t8v);
            TextView t9v = new TextView(ListActivity.this);
            t9v.setText(hp+" ");
            t9v.setTextColor(Color.BLACK);
            t9v.setPadding(10,0,10,0);
            t9v.setGravity(Gravity.CENTER);
            tbrow.addView(t9v);
            TextView t10v = new TextView(ListActivity.this);
            t10v.setText(tel+" ");
            t10v.setTextColor(Color.BLACK);
            t10v.setGravity(Gravity.CENTER);
            tbrow.addView(t10v);
            TextView t11v = new TextView(ListActivity.this);
            t11v.setText(email+" ");
            t11v.setTextColor(Color.BLACK);
            t11v.setPadding(10,0,0,0);
            t11v.setGravity(Gravity.CENTER);
            tbrow.addView(t11v);

            if(k%2==0) {
                t1v.setBackgroundColor(Color.rgb(223,223,223));
                t2v.setBackgroundColor(Color.rgb(223,223,223));
                t3v.setBackgroundColor(Color.rgb(223,223,223));
                t6v.setBackgroundColor(Color.rgb(223,223,223));
                t4v.setBackgroundColor(Color.rgb(223,223,223));
                t5v.setBackgroundColor(Color.rgb(223,223,223));
                t7v.setBackgroundColor(Color.rgb(223,223,223));
                t8v.setBackgroundColor(Color.rgb(223,223,223));
                t9v.setBackgroundColor(Color.rgb(223,223,223));
                t10v.setBackgroundColor(Color.rgb(223,223,223));
                t11v.setBackgroundColor(Color.rgb(223,223,223));
            }
            stk.addView(tbrow);
        }
        outCursor.close();
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
        switch(items[position]){
            case "성명" :
                selected = "name";
                break;
            case "관리번호" :
                selected = "num";
                break;
            case "소속" :
                selected = "company";
                break;
        }
    }
    public void onNothingSelected(AdapterView<?> parent){
        selected = "name";
    }
    @Override
    public void onBackPressed() {

        // BackPressedForFinish 클래시의 onBackPressed() 함수를 호출한다.
        backPressedForFinish.onBackPressed();
    }


    public class phpDown2 extends AsyncTask<String, Integer, String> {

        protected void onPreExecute(){
        }
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
                //txtView.setText(str);
                String num;
                String name;
                String company;
                String b_num;
                String jtype;
                String dept;
                String position;
                String hp;
                String tel;
                String email;

                try{
                    db.execSQL("drop table "+ tableName);
                    createTable(tableName);
                    JSONObject root = new JSONObject(str);
                    count = root.getInt("counts");
                    auth = root.getString("auth");
                    count_info.setText("이벤트응모수("+count+")");
                    JSONArray ja = root.getJSONArray("results");
                    for(int i=0; i<ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        num = jo.getString("num");
                        name = jo.getString("name");
                        jtype = jo.getString("jtype");
                        company = jo.getString("company");
                        b_num = jo.getString("b_num");
                        if(auth.contains("department")){
                            dept = jo.getString("department");
                        }else{ dept = "미노출";};
                        if(auth.contains("position")){
                            position = jo.getString("position");
                        }else{ position = "미노출";};
                        if(auth.contains("HP")){
                            hp = jo.getString("phone");
                        }else{ hp = "미노출";};
                        if(auth.contains("Tel")){
                            tel = jo.getString("tel");
                        }else{ tel = "미노출";};
                        if(auth.contains("Email")){
                            email = jo.getString("email");
                        }else{ email = "미노출";};

                        db.execSQL("insert into " + tableName + "(num, name, jtype, company, event, dept, position, hp, tel, email) values " + "(" + num + ", '" + name + "', '" + jtype + "', '" + company + "', '" + b_num +"', '"+ dept + "', '"+position + "', '"+hp + "', '"+tel + "', '"+email +"');");


                    }
                    viewData();
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "실패! 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            }else{

            }

        }

    }


    public void createDatabase(String name) {

        db = openOrCreateDatabase(
                name,
                MODE_WORLD_WRITEABLE,
                null);

    }

    public void createTable(String tableName) {

        db.execSQL("create table if not exists " + tableName + "("
                + " _id integer PRIMARY KEY autoincrement, "
                + " num integer, "
                + " name text, "
                + " jtype text, "
                + " company text, "
                + " event text, "
                + " dept text, "
                + " position text, "
                + " hp text, "
                + " tel text, "
                + " email text);");

    }

}