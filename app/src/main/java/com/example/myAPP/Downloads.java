package com.example.myAPP;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import com.example.tiktok.R;

import java.util.List;

public class Downloads extends AppCompatActivity  implements VideoFileFragment2.OnInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
    }

    @Override
    public void videoSelection(VideoFile video) {
        Intent intent=new Intent(getApplicationContext(),Play.class);
        intent.putExtra("VideoName",video.videoName);
        startActivity(intent);
    }

    @Override
    public List<VideoFile> getVideos() {
        return Menu.videoFiles;
    }
}