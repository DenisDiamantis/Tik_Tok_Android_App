package com.example.myAPP;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiktok.R;

import java.io.File;

import static com.example.myAPP.Menu.channelName;
import static com.example.myAPP.Menu.namesVideos;
import static com.example.myAPP.Menu.path;
import static com.example.myAPP.Menu.published;

public class Delete extends AppCompatActivity {
    TextView videoname;
    Button delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        videoname=findViewById(R.id.name);
        delete=findViewById(R.id.dltbtn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=videoname.getText().toString();
                deleteVideo(name);
            }
        });
    }


    public void deleteVideo(String name){
        if(!namesVideos.contains(name)){
            Toast.makeText(getApplicationContext(),"Video named " + name + " could not be found...",Toast.LENGTH_SHORT).show();
            return;
        }
        namesVideos.remove(name);
        Value temp=new Value();
        for(int i=0;i<published.size();i++){
            if(published.get(i).getVideoFile().videoName.equals(name)){
                for(int j=0;j<published.get(i).getVideoFile().associatedHashtags.size();j++)
                {
                    channelName.userVideoFilesMap.get(published.get(i).getVideoFile().associatedHashtags.get(j)).remove(published.get(i));
                    if(channelName.userVideoFilesMap.get(published.get(i).getVideoFile().associatedHashtags.get(j)).isEmpty()){
                        channelName.userVideoFilesMap.remove(published.get(i).getVideoFile().associatedHashtags.get(j));
                    }
                }
                temp=published.get(i);
                break;
            }
        }
        published.remove(temp);
        File tobedeleted=new File(path,name+".mp4");
        tobedeleted.delete();
        File tobedeleted2=new File(path,name+".txt");
        tobedeleted2.delete();
        Toast.makeText(getApplicationContext(),"Video successfully deleted",Toast.LENGTH_SHORT).show();
    }

}