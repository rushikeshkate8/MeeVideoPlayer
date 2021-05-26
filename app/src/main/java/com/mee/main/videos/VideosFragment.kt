package com.mee.main.videos

import android.content.ContentResolver
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jiajunhui.xapp.medialoader.bean.VideoItem
import com.mee.main.MainActivityViewModel
import com.mee.player.PlayerActivity
import com.mee.player.databinding.VideosFragmentBinding
import java.io.File


class VideosFragment : Fragment() {
    private var mViewModel: VideosViewModel? = null
    var binding: VideosFragmentBinding? = null
    var adapter: VideosAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VideosFragmentBinding.inflate(inflater, container, false)

        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())

        binding!!.videoItemsRecyclerView.adapter = adapter
        MainActivityViewModel._videoResult.observe(
            viewLifecycleOwner,
            { videoResult -> adapter!!.submitList(videoResult.items) })
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(VideosViewModel::class.java)
        // TODO: Use the ViewModel
    }

    companion object {
        fun newInstance(): VideosFragment {
            return VideosFragment()
        }
    }

    fun getVideoItemClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri = Uri.fromFile(File(it.path))
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    fun getMoreImageViewClickListener(): VideosAdapter.OnClickListener {
        return  VideosAdapter.OnClickListener {
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.type = "video/*"
//            intent.putExtra(Intent.EXTRA_STREAM, it.path.toUri())
//            ActivityCompat.startActivity(requireContext(),  intent, null)

            val contentResolver = activity?.contentResolver
            if (contentResolver != null) {
                contentResolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.VideoColumns.DATA + "=?",
                    arrayOf(it.path))
            }
            //requestDeletePermission(listOf(it.path.toUri()))
            Toast.makeText(context, "Toast", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestDeletePermission(uriList: List<Uri>) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = activity?.let { MediaStore.createDeleteRequest(it.getContentResolver(), uriList) }
            try {
                if (pi != null) {
                    startIntentSenderForResult(
                        pi.intentSender, 101, null, 0, 0,
                        0, null)
                }
            } catch (e: SendIntentException) {
            }
        }
    }

    fun deleteFileUsingDisplayName(videoItem: VideoItem): Boolean {
        val uri = videoItem.path.toUri()
        if (uri != null) {
            val resolver: ContentResolver? = context?.getContentResolver()
            val selectionArgsPdf = arrayOf(videoItem.path)
            try {
                if (resolver != null) {
                    resolver.delete(
                        uri,
                        MediaStore.Files.FileColumns.DISPLAY_NAME + "=?",
                        selectionArgsPdf
                    )
                }
                return true
            } catch (ex: Exception) {
                ex.printStackTrace()
                // show some alert message
            }
        }
        return false
    }
    }
