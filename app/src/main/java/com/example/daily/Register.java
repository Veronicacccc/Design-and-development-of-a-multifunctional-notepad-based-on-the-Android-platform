package com.example.daily;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 用户注册
 */
public class Register extends AppCompatActivity {

    private Button btn_Register;
    private EditText edt_UserName, edt_Password, edt_Password2, edt_StuNum;
    private boolean isFlag = true;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //用户名及密码
        edt_UserName = this.findViewById(R.id.edt_UserName);
        edt_Password = this.findViewById(R.id.edt_Password);
        edt_Password2 = this.findViewById(R.id.edt_Password2);

        //学号
        edt_StuNum = this.findViewById(R.id.edt_StuNum);


        //确认注册按钮
        btn_Register = this.findViewById(R.id.btn_Register);

        //用户名输入框失焦处理
        edt_UserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag = true;
                } else {
                    isFlag = true;
                    String username = edt_UserName.getText().toString();
                    if (username.length() < 4) {
                        Toast.makeText(Register.this, "用户名长度必须大于4,请重新输入", Toast.LENGTH_SHORT).show();
                        edt_UserName.setText("");
                    }
                }
            }
        });
        //密码输入框失焦处理
        edt_Password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String password = edt_Password.getText().toString();
                    if (password.length() < 6 || password.length() > 12) {
                        Toast.makeText(Register.this, "密码长度必须为6-12位,请重新输入", Toast.LENGTH_LONG).show();
                        edt_Password.setText("");
                    }
                }
            }
        });
        //确认密码输入框失焦处理
        edt_Password2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String password = edt_Password.getText().toString();
                    String password2 = edt_Password2.getText().toString();

                    if (!(password.equals(password2))) {
                        Toast.makeText(Register.this, "两次密码不一致,请重新输入", Toast.LENGTH_SHORT).show();
                        edt_Password2.setText("");

                    } else if (password2.length() < 6 || password2.length() > 12) {
                        Toast.makeText(Register.this, "密码长度必须为6-12位", Toast.LENGTH_LONG).show();
                        edt_Password2.setText("");
                    }

                }
            }

        });
        //学号
        edt_StuNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isFlag=true;
                } else {
                    isFlag = true;
                    String stunum = edt_StuNum.getText().toString();
                    if (!(stunum.length() == 10)) {
                        Toast.makeText(Register.this, "请输入10位学号", Toast.LENGTH_SHORT).show();
                        edt_StuNum.setText("");
                    }
                }
            }
        });

        //打开数据库或创建数据库
        SQLiteDatabase database = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        String dropSQL = "drop table user;";
        String createSQL = "create table IF NOT EXISTS user(username text, password text, stunum text);";
        //database.execSQL(dropSQL);
        database.execSQL(createSQL);
        //确认注册处理
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                //获取用户输入信息
                String username = edt_UserName.getText().toString();
                String password = edt_Password.getText().toString();
                String password2 = edt_Password2.getText().toString();
                String stunum = edt_StuNum.getText().toString();
                //判断用户输入是否为空，若有一个输入框为空，则给出提示信息

                if (username.equals("") || password.equals("") || password2.equals("") || stunum.equals("")) {
                    Toast.makeText(Register.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
                    isFlag = false;


                }
                //判断用户是否存在，若已有此账号，则不允许重复注册，否则允许注册
                Cursor cursor = database.query("user", new String[]{"username"}, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    if (username.equals(cursor.getString(cursor.getColumnIndex("username")))) {
                        Toast.makeText(Register.this, "该账户已存在", Toast.LENGTH_SHORT).show();
                        isFlag = false;//
                    }
                }

                if(!password.equals(password2)) {

                    Toast.makeText(Register.this, "两次密码不一致", Toast.LENGTH_SHORT).show();

                    //若允许注册，则将用户输入的信息，插入到数据库表（user）中,插入成功跳转到登录界面
                }else if (!(stunum.length() == 10)){
                    Toast.makeText(Register.this, "请输入10位学号！", Toast.LENGTH_SHORT).show();
                }
                else if (isFlag ) {
                    ContentValues values = new ContentValues();
                    //将数据放入values中
                    values.put("username", username);
                    values.put("password", password);
                    values.put("stunum", stunum);
                    //用insert()方法将values中的数据插入到user表中
                    long xx = database.insert("user", null, values);
//                    if(xx == -1){
//                        Log.d("MainActivity", "没插入");
//                    }
                    Toast.makeText(Register.this, "注册成功,请登录！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Register.this, Login.class);
                    startActivity(intent);
                    database.close();
                }


            }
        });
    }

}
