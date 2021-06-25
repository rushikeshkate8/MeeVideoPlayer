package com.mee.ui.main.mainUtils

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
import androidx.documentfile.provider.DocumentFile
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mee.ui.main.bindVideoDate
import com.mee.ui.main.bindVideoNameTextView
import com.mee.ui.main.bindVideoPath
import com.mee.ui.main.bindVideoSize
import com.mee.player.R
import com.mee.player.databinding.FileInfoAlertDialogBinding
import com.mee.player.databinding.VideoItemMoreBottomSheetBinding


class VideoItemModelBottomSheet() :
    BottomSheetDialogFragment() {

    lateinit var binding: VideoItemMoreBottomSheetBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = VideoItemMoreBottomSheetBinding.inflate(inflater)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) binding.deleteBottomSheet.visibility =
//            View.GONE

        //binding.videoItem = video
        bindVideoNameTextView(binding.videoItemBottomSheetTitle, mVideo.videoName)

        binding.lifecycleOwner = this

        setUpOnClickListeners()

        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    companion object {
        const val TAG = "ModalBottomSheet"

        lateinit var mVideo: videoContent
        lateinit var mClickListener: OnClickListener

        @JvmStatic
        fun newInstance(video: videoContent, clickListener: OnClickListener) =
            VideoItemModelBottomSheet().apply {
                mVideo = video
                mClickListener = clickListener
            }

    }


    fun setUpOnClickListeners() {
        binding.shareBottomSheet.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, mVideo.path.toUri())
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
            ///binding.videoItem = video
            bindVideoDate(binding.fileInfoAlertDialogDate, mVideo.date_added)
            bindVideoNameTextView(binding.fileInfoAlertDialogVideoName, mVideo.videoName)
            bindVideoPath(binding.fileInfoAlertDialogPath, mVideo.path)
            bindVideoSize(binding.fileInfoAlertDialogSize, mVideo.videoSize)
            binding.fileInfoAlertDialogFormat.text =
                requireActivity().contentResolver.getType(mVideo.assetFileStringUri.toUri())


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


//    fun deleteVideoItem() {
//
////        val contentResolver = activity?.contentResolver
//        if(!fileExists(requireContext(), video.assetFileStringUri.toUri()))
//            return
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//
//
//            val contentResolver = activity?.contentResolver
//
//            if (contentResolver != null) {
//                val selectionArgsPdf = arrayOf<String>(video.videoName)
//
//                //DirectoryFileObserver(video.path.substring(0, video.path.lastIndexOf("/"))).startWatching()
//
//                contentResolver.delete(
//                   video.assetFileStringUri.toUri(), MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf
//                )
//
//                MainActivityViewModel.videos.value!!.removeAt(position)
//
//            //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
//                //MediaStoreCompat.fromFileName(requireContext(), MediaType.VIDEO, video.videoName)
//
//           MainActivityViewModel.videos.value = MainActivityViewModel.videos.value
//        }


//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//
//           val contentResolver = activity?.contentResolver
//
//            if (contentResolver != null) {
//                val selectionArgsPdf = arrayOf<String>(video.videoName)
//
//                DirectoryFileObserver(video.path.substring(0, video.path.lastIndexOf("/"))).startWatching()
//
//                contentResolver.delete(
//                   video.assetFileStringUri.toUri(), MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf
//                )
//
//                //updateDatabase()
//            }
//        }
//    }

    fun deleteVideoItem() = mClickListener.OnClick(mVideo)

    fun fileExists(context: Context, uri: Uri): Boolean {
        return if ("file" == uri.scheme) {
            val file = DocumentFile.fromSingleUri(requireContext(), uri)
            file!!.exists()
        } else {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream!!.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

//    fun updateDatabase() {
//        MediaLoader.getLoader()
//            .loadVideos(activity, object : OnVideoLoaderCallBack() {
//                override fun onResult(result: VideoResult) {
//                    MainActivityViewModel.videoResult.value = result
//                }
//            })
//    }


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
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString() + "/" + fileId
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

    class OnClickListener(val clicklistener: (videoContent) -> Unit) {
        fun OnClick(video: videoContent) = clicklistener(video)
    }


}