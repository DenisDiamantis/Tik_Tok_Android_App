package com.example.myAPP;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tiktok.R;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {
    String port;
    public static ChannelName channelName=new ChannelName();
    List<PublisherThread> brokers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLogin=findViewById(R.id.buttonLogin);
        EditText usernameTxt=findViewById(R.id.UsernameIn);
        EditText porttxt=findViewById(R.id.port);
        btnLogin.setOnClickListener(v -> {
            String username=usernameTxt.getText().toString();
            port=porttxt.getText().toString();
            if(username.isEmpty()||port.isEmpty()){
                Toast.makeText(getApplicationContext(),"Fields cannot be empty",Toast.LENGTH_SHORT).show();
            }else{
            Intent intent =new Intent(getApplicationContext(),Menu.class);
            intent.putExtra("ChannelName",username);
            intent.putExtra("Port",port);

            startActivity(intent);
            }
        });
    }


}