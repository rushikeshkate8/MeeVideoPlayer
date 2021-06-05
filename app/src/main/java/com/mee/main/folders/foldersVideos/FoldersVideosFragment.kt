package com.mee.main.folders.foldersVideos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mee.player.R
import com.mee.player.databinding.FragmentFoldersVideosBinding
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.mee.main.MainActivityViewModel
import com.mee.main.mainUtils.VideoItemModelBottomSheet
import com.mee.main.videos.VideosAdapter
import com.mee.player.PlayerActivity
import java.io.File
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FoldersVideosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoldersVideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job

    // TODO: Rename and change types of parameters
    private lateinit var binding: FragmentFoldersVideosBinding

   // private lateinit var viewModel: FoldersVideosViewModel

    lateinit var adapter: VideosAdapter

    val args: FoldersVideosFragmentArgs by navArgs()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoldersVideosBinding.inflate(inflater, container, false)

        job = Job()
        //viewModel = ViewModelProvider(this).get(FoldersVideosViewModel::class.java)

       // viewModel.folder = MainActivityViewModel.folders.value?.get(args.position)!!

        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())
        binding.foldersVideoItemsRecyclerView.adapter = adapter
        adapter.submitList(MainActivityViewModel.folders.value?.get(args.position)?.videoFiles)

        binding.toolbarFoldersVideosFragment.title = MainActivityViewModel.folders.value?.get(args.position)?.folderName
        binding.toolbarFoldersVideosFragment.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setUpObservers()

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FoldersVideosFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FoldersVideosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun getVideoItemClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri =
                Uri.fromFile(File(MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.get(it)?.path!!))
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    fun getMoreImageViewClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val modalBottomSheet = VideoItemModelBottomSheet(
                MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.get(it)!!,
                VideoItemModelBottomSheet.OnClickListener {
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
        MainActivityViewModel.folders.observe(viewLifecycleOwner, {
            adapter.submitList(it.get(args.position).videoFiles.toList())
        })
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


                for (videoItem in MainActivityViewModel.videos.value!!) {
                    if(video.videoId == videoItem.videoId) {
                        MainActivityViewModel.videos.value?.remove(videoItem)
                        break
                    }
                }

                MainActivityViewModel.folders.value?.get(args.position)!!.videoFiles.remove(video)

                //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
                //MediaStoreCompat.fromFileName(requireContext(), MediaType.VIDEO, video.videoName)

                MainActivityViewModel.folders.value = MainActivityViewModel.folders.value
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