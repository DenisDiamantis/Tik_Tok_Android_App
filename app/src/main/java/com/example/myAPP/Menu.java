package com.example.myAPP;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tiktok.R;
import org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Menu extends AppCompatActivity {

    Button search_btn;
    Button upload_video;
    Button manage_hashtags;
    Button delete_video;
    Button downloads;
    TextView search_txt;
    static ChannelName channelName=new ChannelName();
    static ArrayList<Value> published = new ArrayList<>();
    static int maxBufferSize = 512 * 1024; //512kb
    static List<VideoFile> videoFiles=new ArrayList<>();
    static List<PublisherThread> brokers = new ArrayList<>();
    static ArrayList<String> namesVideos = new ArrayList<>();
    static String path = Environment.getExternalStorageDirectory() + "/MOVIES/";
    static Socket client = null;
    static ObjectOutputStream out = null;
    static ObjectInputStream in = null;
    static String port;
    static String serverPort;
    static String serverIp;
    private static final int requestCode = 100;
    private static final int videoRecord = 101;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        channelName.setChannelName(getIntent().getStringExtra("ChannelName"));
        port=getIntent().getStringExtra("Port");
        getCameraPermission();
        search_txt = findViewById(R.id.search_text);
        search_btn = findViewById(R.id.search_btn);
        upload_video = findViewById(R.id.upload_video);
        manage_hashtags = findViewById(R.id.add_hashtag);
        delete_video = findViewById(R.id.delete_video);
        downloads = findViewById(R.id.downloads);
        new init().execute();
        Publisher publisher=new Publisher();
        publisher.execute();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Initializer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloads.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(),Downloads.class);
            startActivity(intent);
        });
        search_btn.setOnClickListener(v -> {

            String search = search_txt.getText().toString();
            String[] params =new String[2];
            params[0]="Search";
            params[1]=search;
            new Consumer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
        });
        manage_hashtags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AddHashtag.class);
                startActivity(intent);
            }
        });
        upload_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent,videoRecord);
            }
        });
        delete_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Delete.class);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK){
            Intent intent=new Intent(getApplicationContext(),VideoConfiguration.class);
            intent.putExtra("FILENAME",getNewVideo());
            startActivity(intent);
        }
    }

    private class Initializer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
                
            try {
                client = new Socket("10.0.2.2", 4321);
                serverPort="4321";
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
                Message info = new Message(channelName.channelName, port, "PublisherInfo", null);
                out.writeObject(info);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private  class init extends AsyncTask<String, String, String> {


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... strings) {
            init();
            return null;
        }
    }
    private class Publisher extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            ServerSocket providerSocket=null;
            Socket connection;
            try {
                providerSocket = new ServerSocket(Integer.parseInt(port));
                while (true) {
                    connection = providerSocket.accept();
                    PublisherThread pub=new PublisherThread(Menu.this,connection);
                    brokers.add(pub);
                    pub.start();

                }
            } catch (IOException ioException) {
                try {
                    providerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }
    }



    private class Consumer extends AsyncTask<String, String, String> {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... params) {
                Message request;
                if (params[0].equals("Search")) {
                    request = new Message(channelName.channelName, params[1], "Consumer", null);
                    try {
                        out.writeObject(request);
                        out.flush();
                        request = (Message) in.readObject();
                        if (request.getFlag().equals("Redirect")) {
                            serverIp = request.getChannelName();
                            if(serverIp.equals("127.0.0.1")){
                                serverIp="10.0.2.2";
                            }
                            serverPort = request.getKey();
                            in.close();
                            out.close();
                            client.close();

                            client = new Socket(serverIp, Integer.parseInt(request.getKey()));
                            serverPort=request.getKey();
                            out = new ObjectOutputStream(client.getOutputStream());
                            in = new ObjectInputStream(client.getInputStream());
                            request = new Message(channelName.channelName, params[1], "Consumer", null);
                            out.writeObject(request);
                            out.flush();
                            request = (Message) in.readObject();
                        }
                        if (request.getFlag().equals("No results")) {
                            System.out.println("No video results for search item: " + request.getKey());
                        } else {
                            playData(request.getFlag(), null);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        try {
                            in.close();
                            out.close();
                            client.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }

               return null;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void init() {
        //  try {

        try {
            ArrayList<String> filenames = (ArrayList) Files.list(Paths.get(path)).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            Value value = new Value();
            VideoFile video;
            for (int i = 0; i < filenames.size(); i++) {
                ArrayList<String> mylist = new ArrayList<>();
                MediaMetadataRetriever metadata = new MediaMetadataRetriever();

                //detecting Mp4 files already published
                if (filenames.get(i).contains(".mp4")) {
                    metadata.setDataSource(path + filenames.get(i));
                    video = new VideoFile(filenames.get(i).substring(0, filenames.get(i).length() - 4), channelName.channelName,
                            metadata.extractMetadata(metadata.METADATA_KEY_DATE),
                            metadata.extractMetadata(metadata.METADATA_KEY_DURATION),
                            metadata.extractMetadata(metadata.METADATA_KEY_CAPTURE_FRAMERATE),
                            metadata.extractMetadata(metadata.METADATA_KEY_VIDEO_WIDTH),
                            metadata.extractMetadata(metadata.METADATA_KEY_VIDEO_HEIGHT),
                            new ArrayList<>(), null);
                    value = new Value(video);
                    published.add(value);
                    namesVideos.add(video.videoName);

                    if (filenames.contains(filenames.get(i).substring(0, filenames.get(i).length() - 4) + ".txt")) {
                        //detecting txt files already published
                        try {
                            FileReader reader = new FileReader(path + filenames.get(i).substring(0, filenames.get(i).length() - 4) + ".txt");
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
            }

            channelName.userVideoFilesMap.put(channelName.channelName, published);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void playData(String data, Value value) {
        int counter=Integer.parseInt(data);
        Message answer = null;
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Downloading " + Integer.parseInt(data) + " videos...", Toast.LENGTH_SHORT).show();
            }
        });
        do {
            int length = 0;
            List<byte[]> chunks = new ArrayList<>();
            do {
                try {
                    answer = (Message) in.readObject();
                    chunks.add(answer.getData().videoFile.getVideoFileChunk());
                    length += answer.getData().videoFile.getVideoFileChunk().length;

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } while (Integer.parseInt(answer.getFlag()) > 0);

            VideoFile temp = answer.getData().videoFile;
            length += chunks.get(chunks.size() - 1).length;

            File mp4 = new File(Environment.getExternalStorageDirectory() + "/DOWNLOAD/",answer.getData().getVideoFile().videoName + ".mp4");
            OutputStream os = null;
            try {
                os = new FileOutputStream(mp4);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                byte[] after = new byte[length];
                for (int j = 0; j < chunks.size(); j++) {
                    if (j == 0) {
                        System.arraycopy(chunks.get(j), 0, after, 0, chunks.get(j).length);
                    } else {
                        System.arraycopy(chunks.get(j), 0, after, j * chunks.get(j - 1).length, chunks.get(j).length);
                    }
                }

                os.write(after);
                os.close();
                boolean exists=false;
                for(int i=0;i<videoFiles.size();i++){
                    if(videoFiles.get(i).getVideoName().equals(temp.getVideoName())){
                       exists=true;
                    }
                }
                if(!exists){
                    videoFiles.add(temp);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Downloaded video " + temp.getVideoName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter--;
        }while (counter>0);
    }
    public static void push(String key, Value value) {
        Message videoChunks;
        if(!channelName.userVideoFilesMap.containsKey(key))
        {
            videoChunks=new Message(channelName.channelName,key,"No results",null);
            for(int j=0;j<brokers.size();j++) {
                if (brokers.get(j).equals(Thread.currentThread())) {
                    try {
                        brokers.get(j).out.writeObject(videoChunks);
                        brokers.get(j).out.flush();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        videoChunks=new Message(channelName.channelName,key,String.valueOf(channelName.userVideoFilesMap.get(key).size()),null);
        ArrayList<Value> temp;
        for(int j=0;j<brokers.size();j++) {
            if (brokers.get(j).equals(Thread.currentThread())) {
                try {
                    brokers.get(j).out.writeObject(videoChunks);
                    brokers.get(j).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < channelName.userVideoFilesMap.get(key).size(); i++) {
                    int counter;
                    temp = generateChunks(channelName.userVideoFilesMap.get(key).get(i).videoFile.videoName);
                    counter = temp.size() - 1;
                    for (int k = 0; k < temp.size(); k++) {
                        videoChunks = new Message(channelName.channelName, published.get(i).getVideoFile().videoName, String.valueOf(counter), temp.get(k));
                        try {
                            brokers.get(j).out.writeObject(videoChunks);
                            brokers.get(j).out.flush();
                            counter--;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }
    public static ArrayList<Value> generateChunks(String key) {
        VideoFile temp = new VideoFile();
        for (int i = 0; i < published.size(); i++) {
            //if key==Channel name
            if (key.equals(published.get(i).getVideoFile().videoName)) {
                temp = published.get(i).getVideoFile();
            }
        }
        byte[] byteArr = new byte[0];
        try {
            FileInputStream unchunkedmp4 = new FileInputStream(path + temp.videoName + ".mp4");
            byteArr = IOUtils.toByteArray(unchunkedmp4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int CHUNK_SIZE = maxBufferSize;
        byte[] temporary;
        int bytesRead = 0;
        ByteBuffer before = ByteBuffer.wrap(byteArr);
        int FILE_SIZE = byteArr.length;
        int NUMBER_OF_CHUNKS = FILE_SIZE / CHUNK_SIZE + 1;
        int bytesRemaining = FILE_SIZE;
        VideoFile copyInfo;
        ArrayList<Value> chunked = new ArrayList<>();
        for (int j = 0; j < NUMBER_OF_CHUNKS; j++) {
            if (j == NUMBER_OF_CHUNKS - 1) {
                CHUNK_SIZE = bytesRemaining;
            }
            temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
            before.get(temporary, 0, CHUNK_SIZE);
            bytesRead += CHUNK_SIZE;
            if (bytesRead > 0) // If bytes read is not empty
            {
                bytesRemaining -= CHUNK_SIZE;
            }
            copyInfo= new VideoFile(temp.videoName,temp.channelName,
                    temp.dateCreated,temp.length,temp.framerate,temp.frameWidth,
                    temp.frameHeight,temp.associatedHashtags,temporary);

            chunked.add(new Value(copyInfo));
        }
        return chunked;
    }
    public void getCameraPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getNewVideo() {
        try {
            ArrayList<String> filenames = (ArrayList) Files.list(Paths.get(path)).filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).collect(Collectors.toList());
            for(int i=0;i<filenames.size();i++){
                if(!namesVideos.contains(filenames.get(i).substring(0, filenames.get(i).length() - 4))){
                    return filenames.get(i);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
