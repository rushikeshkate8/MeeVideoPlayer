package com.mee.main.videos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.main.MainActivityViewModel
import com.mee.main.mainUtils.VideoItemModelBottomSheet
import com.mee.player.PlayerActivity
import com.mee.player.R
import com.mee.player.databinding.VideosFragmentBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class VideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job

    private var mViewModel: VideosViewModel? = null
    var binding: VideosFragmentBinding? = null

    lateinit var adapter: VideosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VideosFragmentBinding.inflate(inflater, container, false)
        binding!!.lifecycleOwner = this

        job = Job()

        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())

        binding!!.videoItemsRecyclerView.adapter = adapter

        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(VideosViewModel::class.java)
    }

    companion object {
        fun newInstance(): VideosFragment {
            return VideosFragment()
        }
    }

    fun getVideoItemClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri =
                Uri.fromFile(File(MainActivityViewModel.videos.value?.get(it)?.path!!))
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    fun getMoreImageViewClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val modalBottomSheet = VideoItemModelBottomSheet(MainActivityViewModel.videos.value?.get(it)!!, VideoItemModelBottomSheet.OnClickListener {
                deleteVideoItem(it)
            })
            fragmentManager?.let { it1 ->
                modalBottomSheet.show(
                    it1,
                    VideoItemModelBottomSheet.TAG
                )
            }
        }
    }

    fun setUpObservers() {
        MainActivityViewModel.videos.observe(viewLifecycleOwner, {
            launch {
                withContext(Dispatchers.Main) {
                    adapter!!.submitList(MainActivityViewModel.videos.value?.toList())
                }
            }
        })
    }

    fun updateVideoDatabase() {
        launch {
            MainActivityViewModel.videos.value = async(Dispatchers.IO) {
                MediaFacer
                    .withVideoContex(activity)
                    .getAllVideoContent(VideoGet.externalContentUri)
            }.await()
        }
    }

    override fun onStart() {
        super.onStart()
        setUpObservers()
    }

    override fun onResume() {
        super.onResume()
        if(MainActivityViewModel.isReadPermissionGranted.value!!) {
            if(MainActivityViewModel.videos.value?.size == 0)
                updateVideoDatabase()
            else
                adapter.submitList(MainActivityViewModel.videos.value?.toList())
        }
    }

    fun deleteVideoItem(video: videoContent) {

        if (!fileExists(requireContext(), video.assetFileStringUri.toUri()))
            return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {


            val contentResolver = activity?.contentResolver

            if (contentResolver != null) {
                val selectionArgsPdf = arrayOf<String>(video.videoName)

                //DirectoryFileObserver(video.path.substring(0, video.path.lastIndexOf("/"))).startWatching()

                contentResolver.delete(
                    video.assetFileStringUri.toUri(),
                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?",
                    selectionArgsPdf
                )

                val folders = MainActivityViewModel.folders.value
                if(folders != null && folders.size != 0) {
                    for(folder in folders) {
                        for(videoItem in folder.videoFiles) {
                            if(video.videoId == videoItem.videoId) {
                                folder.videoFiles.remove(videoItem)
                                break
                            }
                        }
                    }
                }

                MainActivityViewModel.videos.value?.remove(video)

                //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
                //MediaStoreCompat.fromFileName(requireContext(), MediaType.VIDEO, video.videoName)

                MainActivityViewModel.videos.value = MainActivityViewModel.videos.value
            }
        }
    }

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

}
