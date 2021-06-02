package com.mee.main.videos

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mee.main.videos.VideosAdapter.VideoItemViewHolder
import com.mee.player.databinding.VideoItemBinding
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.main.bindImage
import com.mee.main.bindVideoNameTextView
import com.mee.main.bindVideoDurationTextView

class VideosAdapter(
    val videoItemClickListener: OnClickListener,
    val moreImageViewClickListener: OnClickListener
) : ListAdapter<videoContent, VideoItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(VideoItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val video = getItem(holder.adapterPosition)

        bindVideoNameTextView(holder.binding.videoTitleTextView, video.videoName)
        bindImage(holder.binding.videoThumbnailImageView, video.assetFileStringUri.toUri())
        bindVideoDurationTextView(holder.binding.videoDurationTextView, video.videoDuration)

        holder.binding.moreMenuItemImageView.setOnClickListener {moreImageViewClickListener.OnClick(holder.adapterPosition)}
        holder.binding.videoItemRelativeLayout.setOnClickListener {videoItemClickListener.OnClick(holder.adapterPosition)}
    }

    class VideoItemViewHolder(var binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<videoContent>() {
        override fun areItemsTheSame(oldItem: videoContent, newItem: videoContent): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: videoContent, newItem: videoContent): Boolean {
            return oldItem == newItem
        }
    }

    class OnClickListener(val clickListener: (position: Int) -> Unit) {
        fun OnClick(position: Int) = clickListener(position)
    }
}

