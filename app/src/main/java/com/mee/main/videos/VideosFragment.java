package com.mee.main.videos;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jiajunhui.xapp.medialoader.bean.VideoItem;
import com.mee.player.R;
import com.mee.player.databinding.VideosFragmentBinding;

import java.util.List;

public class VideosFragment extends Fragment {

    private VideosViewModel mViewModel;

    public static VideosFragment newInstance() {
        return new VideosFragment();
    }

    VideosFragmentBinding binding;
    VideosAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater , @Nullable ViewGroup container ,
                             @Nullable Bundle savedInstanceState) {
        binding = VideosFragmentBinding.inflate( inflater, container, false );
        adapter = new VideosAdapter( new DiffCallback() );
        binding.videoItemsRecyclerView.setAdapter(adapter);

        VideosViewModel.getVideoItemsLiveData().observe( getViewLifecycleOwner() , new Observer<List<VideoItem>>() {
            @Override
            public void onChanged(List<VideoItem> videoItems) {
                adapter.submitList( videoItems );
            }
        } );

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated( savedInstanceState );
        mViewModel = new ViewModelProvider( this ).get( VideosViewModel.class );
        // TODO: Use the ViewModel
    }

}