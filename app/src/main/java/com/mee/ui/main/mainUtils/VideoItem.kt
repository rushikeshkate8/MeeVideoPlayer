package com.mee.ui.main.mainUtils


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.player.R
import com.mee.player.databinding.VideoItemBinding
import com.mee.ui.main.bindImage
import com.mee.ui.main.bindVideoDurationTextView
import com.mee.ui.main.bindVideoNameTextView
import com.mee.ui.main.bindVideoSize
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.dsl.genericFastAdapter
import com.mikepenz.fastadapter.items.AbstractItem


/**
 * Where AbstractFlexibleItem implements IFlexible!
 */
//class VideoItem /*(1)*/(val video: videoContent, val moreImageViewClickListener: VideosAdapter.OnClickListener) :
//    AbstractFlexibleItem<VideoItem.VideoItemViewHolder>() {
//    /**
//     * When an item is equals to another?
//     * Write your own concept of equals, mandatory to implement or use
//     * default java implementation (return this == o;) if you don't have unique IDs!
//     * This will be explained in the "Item interfaces" Wiki page.
//     */
//    override fun equals(other: Any?): Boolean {
//        if (other is VideoItem) {
//            return video.videoId == video.videoId
//        }
//        return false
//    }
//
//    /**
//     * You should implement also this method if equals() is implemented.
//     * This method, if implemented, has several implications that Adapter handles better:
//     * - The Hash, increases performance in big list during Update & Filter operations.
//     * - You might want to activate stable ids via Constructor for RV, if your id
//     * is unique (read more in the wiki page: "Setting Up Advanced") you will benefit
//     * of the animations also if notifyDataSetChanged() is invoked.
//     */
//    override fun hashCode(): Int {
//        return video.videoId.hashCode()
//    }
//
//    /**
//     * For the item type we need an int value: the layoutResID is sufficient.
//     */
//    override fun getLayoutRes(): Int {
//        return R.layout.video_item
//    }
//
//    /**
//     * Delegates the creation of the ViewHolder to the user (AutoMap).
//     * The inflated view is already provided as well as the Adapter.
//     */
//    override fun createViewHolder(
//        view: View?,
//        adapter: FlexibleAdapter<IFlexible<*>?>?
//    ): VideoItemViewHolder {
//        return VideoItemViewHolder(VideoItemBinding.inflate(LayoutInflater.from(view?.context)), adapter)
//    }
//
//    /**
//     * The Adapter and the Payload are provided to perform and get more specific
//     * information.
//     */
//    override fun bindViewHolder(
//        adapter: FlexibleAdapter<IFlexible<*>?>?, holder: VideoItemViewHolder,
//        position: Int,
//        payloads: List<Any>
//    ) {
//        holder.bind(video)
//    }
//
//
//    /**
//     * The ViewHolder used by this item.
//     * Extending from FlexibleViewHolder is recommended especially when you will use
//     * more advanced features.
//     */
//
//    inner class VideoItemViewHolder(val binding: VideoItemBinding, adapter: FlexibleAdapter<*>?) :
//        FlexibleViewHolder(binding.root, adapter) {
//
//        fun bind(
//            video: videoContent,
//        ) {
//
//            bindVideoNameTextView(binding.videoTitleTextView, video.videoName)
//            bindImage(binding.videoThumbnailImageView, video.assetFileStringUri.toUri())
//            bindVideoDurationTextView(binding.videoDurationTextView, video.videoDuration)
//            bindVideoSize(binding.videoSizeTextView, video.videoSize)
//
//
//            if(mAdapter.isSelected(absoluteAdapterPosition)) {
//                binding.moreMenuItemImageView.visibility = View.GONE
//                binding.checkItemImageView.visibility = View.VISIBLE
//            }
//            else {
//                binding.moreMenuItemImageView.visibility = View.VISIBLE
//                binding.checkItemImageView.visibility = View.GONE
//            }
//
//            binding.moreMenuItemImageView.setOnClickListener {
//                moreImageViewClickListener.OnClick(
//                    absoluteAdapterPosition
//                )
//            }
////            binding.videoItemRelativeLayout.setOnClickListener {
////                videoItemClickListener.OnClick(
////                    absoluteAdapterPosition
////                )
////            }
//        }
//    }
//}

//class VideoItem(val video: videoContent, val moreImageOnClickListener: OnClickListeners.OnClickListener) : AbstractItem<VideoItem.ViewHolder>() {
//
//
//    /** defines the type defining this item. must be unique. preferably an id */
//    override val type: Int
//        get() = R.id.video_item_relative_layout
//
//    /** defines the layout which will be used for this item in the list */
//    override val layoutRes: Int
//        get() = R.layout.video_item
//
//    override fun getViewHolder(v: View): ViewHolder {
//        return ViewHolder(VideoItemBinding.inflate(LayoutInflater.from(v.context), v.rootView as ViewGroup, false), video)
//    }
//
//    class ViewHolder(val binding: VideoItemBinding, val video: videoContent) :
//        FastAdapter.ViewHolder<VideoItem>(binding.root) {
//
//
//        override fun bindView(item: VideoItem, payloads: List<Any>) {
//            bindVideoNameTextView(binding.videoTitleTextView, video.videoName)
//            bindImage(binding.videoThumbnailImageView, video.assetFileStringUri.toUri())
//            bindVideoDurationTextView(binding.videoDurationTextView, video.videoDuration)
//            bindVideoSize(binding.videoSizeTextView, video.videoSize)
//            binding.root.isActivated = itemView.isSelected
//
//            if (itemView.isSelected) {
//                binding.moreMenuItemImageView.visibility = View.GONE
//                binding.checkItemImageView.visibility = View.VISIBLE
//            } else {
//                binding.moreMenuItemImageView.visibility = View.VISIBLE
//                binding.checkItemImageView.visibility = View.GONE
//            }
////
////            binding.moreMenuItemImageView.setOnClickListener {
////                moreImageViewClickListener.OnClick(
////                    absoluteAdapterPosition
////                )
////            }
////            binding.videoItemRelativeLayout.setOnClickListener {
////                videoItemClickListener.OnClick(
////                    absoluteAdapterPosition
////                )
////            }
//            //binding.moreMenuItemImageView.setOnClickListener { moreImageOnClickListener.OnClick(this) }
//        }
//
//        override fun unbindView(item: VideoItem) {
//
//        }
//    }
//}

class VideoItem(val video: videoContent, var moreImageOnClickListener: OnClickListeners.OnClickListener) : AbstractBindingItem<VideoItemBinding>() {

    override val type: Int
        get() = R.id.video_item_relative_layout

    override fun bindView(binding: VideoItemBinding, payloads: List<Any>) {
        bindVideoNameTextView(binding.videoTitleTextView, video.videoName)
        bindImage(binding.videoThumbnailImageView, video.assetFileStringUri.toUri())
        bindVideoDurationTextView(binding.videoDurationTextView, video.videoDuration)
        bindVideoSize(binding.videoSizeTextView, video.videoSize)

        binding.root.isActivated = isSelected

            if(isSelected) {
                binding.moreMenuItemImageView.visibility = View.GONE
                binding.checkItemImageView.visibility = View.VISIBLE
            }
            else {
                binding.moreMenuItemImageView.visibility = View.VISIBLE
                binding.checkItemImageView.visibility = View.GONE
            }
//
//            binding.moreMenuItemImageView.setOnClickListener {
//                moreImageViewClickListener.OnClick(
//                    absoluteAdapterPosition
//                )
//            }
//            binding.videoItemRelativeLayout.setOnClickListener {
//                videoItemClickListener.OnClick(
//                    absoluteAdapterPosition
//                )
//            }
        binding.moreMenuItemImageView.setOnClickListener { moreImageOnClickListener.OnClick(this) }

    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): VideoItemBinding {
        return VideoItemBinding.inflate(inflater, parent, false)
    }
}

