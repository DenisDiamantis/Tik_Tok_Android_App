package com.example.myAPP;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiktok.R;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.example.myAPP.Menu.channelName;
import static com.example.myAPP.Menu.client;
import static com.example.myAPP.Menu.in;
import static com.example.myAPP.Menu.namesVideos;
import static com.example.myAPP.Menu.out;
import static com.example.myAPP.Menu.path;
import static com.example.myAPP.Menu.published;
import static com.example.myAPP.Menu.serverPort;

public class AddHashtag extends AppCompatActivity {
    TextView videoName;
    TextView hashtag;
    Button addHashtag;
    Button deleteHashtag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hashtag);
        videoName=findViewById(R.id.video_name);
        hashtag=findViewById(R.id.hashtag);
        addHashtag=findViewById(R.id.add_hashtag_btn);
        deleteHashtag=findViewById(R.id.delete);
        addHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=videoName.getText().toString();
                String tag=hashtag.getText().toString();
                String[] params=new String[3];
                params[0]="add Hashtag";
                params[1]=name;
                params[2]=tag;
                new Consumer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
            }
        });
        deleteHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=videoName.getText().toString();
                String tag=hashtag.getText().toString();
                String[] params=new String[3];
                params[0]="delete";
                params[1]=name;
                params[2]=tag;
                new Consumer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
            }
        });
    }

    private class Consumer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Message request;
            if (params[0].equals("add Hashtag")) {
                if (!namesVideos.contains(params[1])) {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Video does not exist in the app...", Toast.LENGTH_SHORT).show();
                }
            });
                    return null;
                }
                if (addHashTag(params[1], params[2])) {
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
                    request = new Message(channelName.channelName, params[2], "new hashtag", null);
                    try {
                        out.writeObject(request);
                        out.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Hashtag: " + params[2] + " already exists for video: " + params[1], Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            } else if (params[0].equals("delete")) {
                if (!published.isEmpty()) {
                    if(namesVideos.contains(params[1])) {
                        removeHashTagFromVideo(params[1], params[2]);
                    }else{
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "Video +" + params[1] +" does not exist in the app", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "You have no videos to delete hashtags from...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            return null;
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

        public void removeHashTagFromVideo(String videoName, String hashtag) {
            for (int j = 0; j < published.size(); j++) {
                if (published.get(j).getVideoFile().videoName.equals(videoName)) {
                    if (!published.get(j).getVideoFile().associatedHashtags.contains(hashtag)) {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "The " + videoName + " does not contain hashtag " + hashtag, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    published.get(j).getVideoFile().associatedHashtags.remove(hashtag);

                    if (channelName.userVideoFilesMap.get(hashtag).size() == 1) {
                        channelName.userVideoFilesMap.remove(hashtag);
                        channelName.hashtagsPublished.remove(hashtag);
                    }
                    try {
                        FileReader reader = new FileReader(path + videoName + ".txt");
                        BufferedReader breader = new BufferedReader(reader);
                        String a = breader.readLine();
                        FileWriter writer = new FileWriter(path + videoName + ".txt");
                        a = a.replace(hashtag, "");
                        writer.write(a);
                        writer.flush();
                        writer.close();
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "Hashtag " + hashtag + " successfully removed " + "from video " + videoName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}