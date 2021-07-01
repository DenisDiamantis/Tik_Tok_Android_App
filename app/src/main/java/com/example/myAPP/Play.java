package com.example.myAPP;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.VideoView;

import com.example.tiktok.R;

public class Play extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        VideoView video =findViewById(R.id.videoView);
        String videoname=getIntent().getStringExtra("VideoName");
        Uri uri= Uri.parse(Environment.getExternalStorageDirectory() + "/DOWNLOAD/"+videoname+".mp4");
        video.setVideoURI(uri);
        video.requestFocus();
        video.start();

    }
}