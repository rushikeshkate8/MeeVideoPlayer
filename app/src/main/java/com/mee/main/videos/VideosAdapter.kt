package com.mee.main.videos

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jiajunhui.xapp.medialoader.bean.VideoItem
import com.mee.main.videos.VideosAdapter.VideoItemViewHolder
import com.mee.player.databinding.VideoItemBinding

class VideosAdapter(
    val videoItemClickListener: OnClickListener,
    val moreImageViewClickListener: OnClickListener
) : ListAdapter<VideoItem, VideoItemViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(VideoItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        holder.binding.videoItem = getItem(position)
        holder.binding.videoItemRelativeLayout.setOnClickListener {
            videoItemClickListener.OnClick(position)
        }
        holder.binding.moreMenuItemImageView.setOnClickListener {
            moreImageViewClickListener.OnClick(position)
        }
    }

    class VideoItemViewHolder(var binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }

    class OnClickListener(val clickListener: (position: Int) -> Unit) {
        fun OnClick(position: Int) = clickListener(position)
    }
}

