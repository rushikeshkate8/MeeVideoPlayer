package com.mee.main.mainUtils

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jiajunhui.xapp.medialoader.MediaLoader
import com.jiajunhui.xapp.medialoader.bean.VideoItem
import com.jiajunhui.xapp.medialoader.bean.VideoResult
import com.jiajunhui.xapp.medialoader.callback.OnVideoLoaderCallBack
import com.mee.main.MainActivityViewModel
import com.mee.player.R
import com.mee.player.databinding.FileInfoAlertDialogBinding
import com.mee.player.databinding.VideoItemMoreBottomSheetBinding


class VideoItemModelBottomSheet(val position: Int) : BottomSheetDialogFragment() {
    lateinit var binding: VideoItemMoreBottomSheetBinding
    lateinit var videoItem: VideoItem
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoItem = MainActivityViewModel.videoResult.value?.items?.get(position)!!
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
            ActivityCompat.startActivity(requireContext(), intent, null)
            dismiss()
        }

        binding.deleteBottomSheet.setOnClickListener {
            val contentResolver = activity?.contentResolver
            if (contentResolver != null) {
                contentResolver.delete(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.VideoColumns.DATA + "=?",
                    arrayOf(videoItem.path)
                )
                MediaLoader.getLoader().loadVideos(activity, object : OnVideoLoaderCallBack() {
                    override fun onResult(result: VideoResult) {
                        MainActivityViewModel.videoResult.value = result
                    }
                })
            }
            dismiss()
        }

        binding.fileInfoBottomSheet.setOnClickListener {
            val binding = FileInfoAlertDialogBinding.inflate(LayoutInflater.from(context))
            binding.videoItem = videoItem

            val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setView(binding.root)
                .setPositiveButton(R.string.close, { dialog, which ->

                })
            val alertDialog = builder.create()
            alertDialog.setTitle(
                Html.fromHtml(
                    "<font color='${resources.getColor(R.color.color_primary)}'>${
                        resources.getString(
                            R.string.information
                        )
                    }</font>"
                )
            )


            alertDialog.show()

            dismiss()
        }
    }
}