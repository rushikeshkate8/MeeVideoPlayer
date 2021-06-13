package com.mee.ui.main.videos

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.marginEnd
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mee.ui.main.videos.VideosAdapter.VideoItemViewHolder
import com.mee.player.databinding.VideoItemBinding
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.ui.main.bindImage
import com.mee.ui.main.bindVideoNameTextView
import com.mee.ui.main.bindVideoDurationTextView
import com.mee.ui.main.bindVideoSize
import androidx.recyclerview.selection.SelectionTracker
import com.mee.ui.main.mainUtils.OnClickListeners

class VideosAdapter(
    val videoItemClickListener: OnClickListeners.OnClickListener,
    val moreImageViewClickListener: OnClickListeners.OnClickListener
) : ListAdapter<videoContent, VideoItemViewHolder>(DiffCallback()) {
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {
        return VideoItemViewHolder(VideoItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val video = getItem(holder.absoluteAdapterPosition)
        tracker?.let {
            holder.bind(video, videoItemClickListener, moreImageViewClickListener,
                it.isSelected(position.toLong()))
        }
    }

    class VideoItemViewHolder(var binding: VideoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            video: videoContent,
            videoItemClickListener: OnClickListeners.OnClickListener,
            moreImageViewClickListener: OnClickListeners.OnClickListener,
            isActivated: Boolean = false
        ) {

            bindVideoNameTextView(binding.videoTitleTextView, video.videoName)
            bindImage(binding.videoThumbnailImageView, video.assetFileStringUri.toUri())
            bindVideoDurationTextView(binding.videoDurationTextView, video.videoDuration)
            bindVideoSize(binding.videoSizeTextView, video.videoSize)
            itemView.isActivated = isActivated

            if(isActivated) {
                binding.moreMenuItemImageView.visibility = View.GONE
                binding.checkItemImageView.visibility = View.VISIBLE
            }
            else {
                binding.moreMenuItemImageView.visibility = View.VISIBLE
                binding.checkItemImageView.visibility = View.GONE
            }

//            binding.moreMenuItemImageView.setOnClickListener {
//                moreImageViewClickListener.OnClick(
//                    video
//                )
//            }
//            binding.videoItemRelativeLayout.setOnClickListener {
//                videoItemClickListener.OnClick(
//                    video
//                )
//            }
        }


        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): Long = itemId
            }
    }

    class DiffCallback : DiffUtil.ItemCallback<videoContent>() {
        override fun areItemsTheSame(oldItem: videoContent, newItem: videoContent): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: videoContent, newItem: videoContent): Boolean {
            return oldItem == newItem
        }
    }
}








