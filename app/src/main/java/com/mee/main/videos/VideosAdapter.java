package com.mee.main.videos;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jiajunhui.xapp.medialoader.bean.VideoItem;
import com.mee.player.databinding.VideoItemBinding;

import org.jetbrains.annotations.NotNull;

public class VideosAdapter extends ListAdapter<VideoItem, VideosAdapter.VideoItemViewHolder> {

    protected VideosAdapter(@NonNull @NotNull DiffUtil.ItemCallback<VideoItem> diffCallback) {
        super( diffCallback );
    }

    @NonNull
    @NotNull
    @Override
    public VideoItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent , int viewType) {
        return new VideoItemViewHolder(VideoItemBinding.inflate( LayoutInflater.from( parent.getContext() ) ));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull VideoItemViewHolder holder , int position) {
        holder.binding.setVideoItem( getItem( position ) );
    }

    class VideoItemViewHolder extends RecyclerView.ViewHolder {
        VideoItemBinding binding;
        public VideoItemViewHolder(VideoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

class DiffCallback extends DiffUtil.ItemCallback<VideoItem> {

    @Override
    public boolean areItemsTheSame(@NonNull @NotNull VideoItem oldItem , @NonNull @NotNull VideoItem newItem) {
        return oldItem.getPath().equals( newItem.getPath() );
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull @NotNull VideoItem oldItem , @NonNull @NotNull VideoItem newItem) {
        return oldItem == newItem;
    }
}
