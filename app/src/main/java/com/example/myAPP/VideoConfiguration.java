package com.example.myAPP;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiktok.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.example.myAPP.Menu.channelName;
import static com.example.myAPP.Menu.client;
import static com.example.myAPP.Menu.in;
import static com.example.myAPP.Menu.namesVideos;
import static com.example.myAPP.Menu.out;
import static com.example.myAPP.Menu.path;
import static com.example.myAPP.Menu.published;
import static com.example.myAPP.Menu.serverPort;

public class VideoConfiguration extends AppCompatActivity {
    TextView videoName;
    TextView hashtags;
    Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_configuration);
        String filename = getIntent().getStringExtra("FILENAME");
        videoName = findViewById(R.id.video_name2);
        hashtags = findViewById(R.id.hashtag2);
        upload = findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = videoName.getText().toString();
                rename(filename, newName);
                String strhashtags = hashtags.getText().toString();
                addHashtags(strhashtags, newName);
                String[] params=new String[1];
                params[0]=newName;
                new uploadVideo().executeOnExecutor(THREAD_POOL_EXECUTOR,params);
                Toast.makeText(getApplicationContext(), "Successfully uploaded video", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void rename(String before, String name) {
        File from = new File(path, before);
        File to = new File(path, name + ".mp4");
        from.renameTo(to);
    }

    public void addHashtags(String hashtags, String video) {
        if(!hashtags.isEmpty()) {
            try {
                File gpxfile = new File(path, video + ".txt");
                FileWriter writer = new FileWriter(gpxfile);
                writer.append(hashtags);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class uploadVideo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> mylist = new ArrayList<>();
            MediaMetadataRetriever metadata = new MediaMetadataRetriever();
            Message request;
            metadata.setDataSource(path + params[0] + ".mp4");
            VideoFile video = new VideoFile(params[0], channelName.channelName,
                    metadata.extractMetadata(metadata.METADATA_KEY_DATE),
                    metadata.extractMetadata(metadata.METADATA_KEY_DURATION),
                    metadata.extractMetadata(metadata.METADATA_KEY_CAPTURE_FRAMERATE),
                    metadata.extractMetadata(metadata.METADATA_KEY_VIDEO_WIDTH),
                    metadata.extractMetadata(metadata.METADATA_KEY_VIDEO_HEIGHT),
                    new ArrayList<>(), null);
            Value value = new Value(video);
            published.add(value);
            namesVideos.add(video.videoName);
            File file = new File(path + params[0] + ".txt");
            if (file.exists()) {
                try {
                    FileReader reader = new FileReader(path + params[0] + ".txt");
                    BufferedReader breader = new BufferedReader(reader);
                    String a = breader.readLine();//diabazoyme to .txt to kanoyme split me ta # kai meta epeidh ta kobei ta prosthetoyme
                    String[] tags = a.split("#");
                    for (int j = 0; j < tags.length; j++) {
                        if (!tags[j].isEmpty()) {
                            mylist.add("#" + tags[j]);
                        }
                    }
                    breader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //adding hashtags to video
                value.getVideoFile().setAssociatedHashtags(mylist);
                //adding hashtags to channel name
                for (String hashtag : mylist) {
                    if (!channelName.hashtagsPublished.contains(hashtag)) {
                        channelName.hashtagsPublished.add(hashtag);
                        if (!serverPort.isEmpty()) {
                            if (!serverPort.equals("4321")) {
                                try {
                                    in.close();
                                    out.close();
                                    client = new Socket("127.0.0.1", 4321);
                                    out = new ObjectOutputStream(client.getOutputStream());
                                    in = new ObjectInputStream(client.getInputStream());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        request = new Message(channelName.channelName, hashtag, "new hashtag", null);
                        try {
                            out.writeObject(request);
                            out.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            ArrayList<Value> temp = new ArrayList<>();
            for (int j = 0; j < channelName.hashtagsPublished.size(); j++) {
                temp = new ArrayList<>();
                for (int k = 0; k < published.size(); k++) {
                    if (published.get(k).getVideoFile().associatedHashtags.contains(channelName.hashtagsPublished.get(j))) {
                        temp.add(published.get(k));
                    }
                }
                channelName.userVideoFilesMap.put(channelName.hashtagsPublished.get(j), temp);
            }


            channelName.userVideoFilesMap.put(channelName.channelName, published);
            return null;
        }

    }
    public Boolean addHashTag(String videoName, String hashtag) {
        for (int j = 0; j < published.size(); j++) {
            if (published.get(j).getVideoFile().videoName.equals(videoName)) {
                if (published.get(j).getVideoFile().associatedHashtags.contains(hashtag)) {
                    return false;
                }
                published.get(j).getVideoFile().associatedHashtags.add(hashtag);
                if (channelName.hashtagsPublished.contains(hashtag)) {
                    channelName.userVideoFilesMap.get(hashtag).add(published.get(j));
                } else {
                    channelName.hashtagsPublished.add(hashtag);
                    ArrayList<Value> temp = new ArrayList<>();
                    temp.add(published.get(j));
                    channelName.userVideoFilesMap.put(hashtag, temp);
                }
            }
        }
        try {
            FileWriter writer = new FileWriter(path + videoName + ".txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(hashtag);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            try {
                File myObj = new File(path + videoName + ".txt");
                FileWriter writer = new FileWriter(myObj);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write(hashtag);
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException ee) {
                e.printStackTrace();
            }
        }
        return true;
    }

}