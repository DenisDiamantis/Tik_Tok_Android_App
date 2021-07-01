package com.example.myAPP;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.tiktok.R;

import java.util.List;


public class VideoFileRecyclerViewAdapter2 extends RecyclerView.Adapter<VideoFileRecyclerViewAdapter2.ViewHolder> {

    private final List<VideoFile> videos;
    private VideoFileFragment2.OnInteractionListener listener;
    public VideoFileRecyclerViewAdapter2(List<VideoFile> items,VideoFileFragment2.OnInteractionListener listener) {
        videos = items;
        this.listener=listener;
    }

    @Override
    public VideoFileRecyclerViewAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item2,parent,false);
        return new VideoFileRecyclerViewAdapter2.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final VideoFileRecyclerViewAdapter2.ViewHolder holder, int position) {
        holder.mItem = videos.get(position);
        holder.videoname.setText(videos.get(position).getVideoName());
        holder.channelname.setText(videos.get(position).getChannelName());
        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.videoSelection(videos.get(position));

            }
        });

    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView videoname;
        public final TextView channelname;
        public final Button play;
        public VideoFile mItem;

        public ViewHolder(View view) {
            super(view);
            videoname = (TextView) view.findViewById(R.id.videoname2);
            channelname = (TextView) view.findViewById(R.id.publisher2);
            play=  view.findViewById(R.id.play2);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + videoname.getText() + "'";
        }
    }
}