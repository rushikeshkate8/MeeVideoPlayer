package com.mee.main.mainUtils

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jiajunhui.xapp.medialoader.bean.VideoItem
import com.mee.player.R
import com.mee.player.databinding.VideoItemMoreBottomSheetBinding

class VideoItemModelBottomSheet(val videoItem: VideoItem) : BottomSheetDialogFragment() {
    lateinit var binding: VideoItemMoreBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VideoItemMoreBottomSheetBinding.inflate(inflater)
        binding.videoItem = videoItem
        setUpOnClickListeners()
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    fun setUpOnClickListeners() {
        binding.shareBottomSheet.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, videoItem.path.toUri())
            ActivityCompat.startActivity(requireContext(),  intent, null)
            dismiss()
        }

        binding.deleteBottomSheet.setOnClickListener {
            val contentResolver = activity?.contentResolver
            if (contentResolver != null) {
                contentResolver.delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.VideoColumns.DATA + "=?",
                    arrayOf(videoItem.path))
            }
            dismiss()
        }
    }
}