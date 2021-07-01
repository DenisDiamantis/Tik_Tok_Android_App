package com.example.myAPP;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tiktok.R;


import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class VideoFileFragment2 extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public interface OnInteractionListener{
        public void videoSelection(VideoFile video);
        public List<VideoFile> getVideos();
    }
    private VideoFileFragment2.OnInteractionListener listener;

    public VideoFileFragment2() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static VideoFileFragment2 newInstance(int columnCount) {
        VideoFileFragment2 fragment = new VideoFileFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list2, container, false);
        List<VideoFile> videos=listener.getVideos();
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new VideoFileRecyclerViewAdapter2(videos,listener));
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof VideoFileFragment2.OnInteractionListener){
            listener= (VideoFileFragment2.OnInteractionListener) context;
        }else{
            throw new RuntimeException(context.toString()+ " must implement OnInteractionListener()");
        }
    }
}