package com.mee.main.mainUtils

import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) binding.deleteBottomSheet.visibility =
            View.GONE

        binding.videoItem = videoItem

        binding.lifecycleOwner = this

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

            deleteVideoItem()

//                val selectionArgsPdf = arrayOf<String>(videoItem.displayName)
//
//                contentResolver.delete(
//                    getUriFromDisplayName(
//                        requireContext(),
//                        videoItem.displayName
//                    )!!, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf
//                )

//                for android 11
//                requestDeletePermission(listOf(videoItem.path.toUri()))

//                Other method for deleting video item
//                contentResolver.delete(
//                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                    MediaStore.Video.VideoColumns.DATA + "=?",
//                    arrayOf(videoItem.path)
//                )
//

            dismiss()
        }

        binding.fileInfoBottomSheet.setOnClickListener {
            val binding = FileInfoAlertDialogBinding.inflate(LayoutInflater.from(context))
            binding.videoItem = videoItem

            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
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


    fun deleteVideoItem() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            val contentResolver = activity?.contentResolver

            if (contentResolver != null) {
                val selectionArgsPdf = arrayOf<String>(videoItem.displayName)

                contentResolver.delete(
                    getUriFromDisplayName(
                        requireContext(),
                        videoItem.displayName
                    )!!, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf
                )

                updateDatabase()
            }
        }
    }


    fun updateDatabase() {
        MediaLoader.getLoader()
            .loadVideos(activity, object : OnVideoLoaderCallBack() {
                override fun onResult(result: VideoResult) {
                    MainActivityViewModel.videoResult.value = result
                }
            })
    }


    fun getUriFromDisplayName(context: Context, displayName: String): Uri? {
        val projection: Array<String>
        projection = arrayOf(MediaStore.Files.FileColumns._ID)

        // TODO This will break if we have no matching item in the MediaStore.
        val cursor: Cursor = context.getContentResolver().query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
            MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", arrayOf(displayName), null
        )!!
        cursor.moveToFirst()
        return if (cursor.getCount() > 0) {
            val columnIndex: Int = cursor.getColumnIndex(projection[0])
            val fileId: Long = cursor.getLong(columnIndex)
            cursor.close()
            Uri.parse(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString().toString() + "/" + fileId
            )
        } else {
            null
        }
    }

    private fun requestDeletePermission(uriList: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(requireActivity().contentResolver, uriList)
            try {
                startIntentSenderForResult(
                    pi.intentSender, 101, null, 0, 0,
                    0, null
                )
            } catch (e: SendIntentException) {
            }
        }
    }
}