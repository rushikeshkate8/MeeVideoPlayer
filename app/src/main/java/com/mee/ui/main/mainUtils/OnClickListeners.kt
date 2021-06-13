package com.mee.ui.main.mainUtils
import com.CodeBoy.MediaFacer.mediaHolders.videoContent

class OnClickListeners {
    class OnClickListener(val clickListener: (videoItem: VideoItem) -> Unit) {
        fun OnClick(videoItem: VideoItem) = clickListener(videoItem)
    }
}