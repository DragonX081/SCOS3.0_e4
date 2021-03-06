package es.source.code.activity2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.source.code.model.User;

public class LoginOrRegister extends AppCompatActivity {
    private Button btn_login,btn_rtn,btn_reg;
    private EditText etLogin, etPsw;
    private boolean validAccount = false;
    private SCOS_Global_Application myApp;
    private SharedPreferences sharedPreferences;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_register);
        myApp = (SCOS_Global_Application) getApplication();
        sharedPreferences = getSharedPreferences("scos_shpref",Context.MODE_PRIVATE);
        userName = getLoginUserName();
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new loginClick());
        btn_reg = findViewById(R.id.btn_register);
        btn_reg.setOnClickListener(new registerClick());
        btn_rtn = findViewById(R.id.btn_rtn);
        btn_rtn.setOnClickListener(new retCLick());
        etLogin = findViewById(R.id.et_login_lg);
        etPsw = findViewById(R.id.et_login_psw);

        setButtonForUser();
    }
    private class loginJump implements jumpToActivity {
        public void jump(String userName, String password) {
            User user = new User(userName, password, true);
            Intent intent = new Intent(LoginOrRegister.this, MainScreen.class);
            intent.putExtra("From", "LoginSuccess");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("User", (Serializable) user);
            myApp.setUserInfo(user);
            startActivity(intent);
        }
    }
    private void setButtonForUser(){
        if(userName==null){//has login
            btn_login.setVisibility(View.INVISIBLE);
            btn_reg.setVisibility(View.VISIBLE);
        }else{
            btn_login.setVisibility(View.VISIBLE);
            btn_reg.setVisibility(View.INVISIBLE);
            etLogin.setText(userName);
        }
    }
    private class registerJump implements jumpToActivity{
        public void jump(String userName,String password){
            Intent intent = new Intent(LoginOrRegister.this, MainScreen.class);
            intent.putExtra("From", "RegisterSuccess");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            User user = new User(userName,password,false);
            intent.putExtra("User", (Serializable) user);
            startActivity(intent);
        }
    }

    interface jumpToActivity{
        public void jump(String userName,String password);
    }
    private void threadJump(final String userName, final String password, final jumpToActivity jumper){
        final ProgressBar prgBar = findViewById(R.id.prgBar_login);
        prgBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int curProgress = 0;
                while (curProgress < 100) {
                    try {
                        Thread.sleep(100);
                        prgBar.incrementProgressBy(5);
                        curProgress += 5;
                    } catch (Exception except) {
                        //handle the Exception
                    }
                }
                //initialize progressBar
                prgBar.setProgress(0);
                prgBar.setVisibility(View.INVISIBLE);
                if(validAccount){
                    //to shared preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName",userName);
                    editor.putInt("loginState",1);
                    editor.commit();
                    //need to be improved
                    Log.v("valid","true");
                    jumper.jump(userName,password);
                }
            }
        }).start();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            if(keyCode==KeyEvent.KEYCODE_BACK){
                Intent intent = new Intent(LoginOrRegister.this,MainScreen.class);
                intent.putExtra("From","Return");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private class retCLick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent = new Intent(LoginOrRegister.this,MainScreen.class);
            intent.putExtra("From","Return");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            // suppose the userName won't be changed after onCreate
            if(userName!=null){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("loginState",0);
                editor.commit();
            }
        }
    }
    private class loginClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //for check
            String[]userInfo = buttonClick();
            threadJump(userInfo[0],userInfo[1],new loginJump());
        }
    }
    private class registerClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //for check
            String[]userInfo = buttonClick();
            threadJump(userInfo[0],userInfo[1],new registerJump());
        }
    }
    private String[] buttonClick(){
        final ProgressBar prgBar = findViewById(R.id.prgBar_login);
        prgBar.setVisibility(View.VISIBLE);
        validAccount= false;
        //for check
        String id, psw;
        id = etLogin.getText().toString();
        psw = etPsw.getText().toString();
        String[] userInfo = {id,psw};
        if (id.isEmpty() || psw.isEmpty()) {
            if(id.isEmpty())etLogin.setError("Null Input");
            if(psw.isEmpty())etPsw.setError("Null Input");
        }else{
            String pattern = "[^A-Za-z0-9]";
            Pattern r = Pattern.compile(pattern);
            Matcher mtcId = r.matcher(id);
            Matcher mtcPsw = r.matcher(psw);
            if (mtcId.find(0) || mtcPsw.find(0)) {
                //handle error for invalid input
                if (mtcId.find(0)) etLogin.setError("Invalid Input");
                if (mtcPsw.find(0)) etPsw.setError("Invalid Input");
            } else {
                //need to be improved
                validAccount = true;
            }
        }
        return userInfo;
    }
    private String getLoginUserName(){
        String userName = sharedPreferences.getString("userName",null);
        return userName;
    }
}
