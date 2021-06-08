package com.mee.ui.main.folders.foldersVideos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mee.player.databinding.FragmentFoldersVideosBinding
import com.mee.ui.main.MainActivityViewModel
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet
import com.mee.ui.main.videos.VideosAdapter
import com.mee.player.PlayerActivity
import java.io.File
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.player.R
import com.mee.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val REQUEST_CODE_DELETE = 100

/**
 * A simple [Fragment] subclass.
 * Use the [FoldersVideosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoldersVideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job
    lateinit var mContext: Context

    // TODO: Rename and change types of parameters
    private lateinit var binding: FragmentFoldersVideosBinding
    lateinit var adapter: VideosAdapter
    val args: FoldersVideosFragmentArgs by navArgs()
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: FoldersVideosViewModel by lazy {
        ViewModelProvider(this).get(FoldersVideosViewModel::class.java)
    }

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
        sharedPreferences =
            activity?.getSharedPreferences(Constants.VIDEO_SORT_ORDER_PREF, Context.MODE_PRIVATE)!!
        binding = FragmentFoldersVideosBinding.inflate(inflater, container, false)
        job = Job()
        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())
        binding.foldersVideoItemsRecyclerView.adapter = adapter
        adapter.submitList(
            MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.sort(
                sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
            )
        )
        binding.toolbarFoldersVideosFragment.title =
            MainActivityViewModel.folders.value?.get(args.position)?.folderName
        binding.toolbarFoldersVideosFragment.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setUpToolbarMenu()
        setUpObservers()
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
                Uri.fromFile(
                    File(
                        MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.get(
                            it
                        )?.path!!
                    )
                )
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    fun getMoreImageViewClickListener(): VideosAdapter.OnClickListener {
        return VideosAdapter.OnClickListener {
            val modalBottomSheet = VideoItemModelBottomSheet.newInstance(
                MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.get(it)!!,
                VideoItemModelBottomSheet.OnClickListener {
                    deleteVideoItem(mutableListOf(it))
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
        viewModel.intentSender.observe(viewLifecycleOwner, {
            if (it != null) {
                startIntentSenderForResult(
                    it,
                    REQUEST_CODE_DELETE, null, 0, 0, 0, null
                )
                viewModel.intentSender.value = null
            }
        })
    }


    fun deleteVideoItem(videos: MutableList<videoContent>) {

        for (video in videos) {
            if (!fileExists(mContext, video.assetFileStringUri.toUri())) {
                videos.remove(video)
                MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.remove(video)
            }
        }

        if (videos.isEmpty()) {
            submitNewFiles()
            return
        }


//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//
//
//            val contentResolver = activity?.contentResolver
//
//            if (contentResolver != null) {
//                val selectionArgsPdf = arrayOf<String>(video.videoName)
//
//                contentResolver.delete(
//                    video.assetFileStringUri.toUri(),
//                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?",
//                    selectionArgsPdf
//                )

        viewModel.videosToDelete = videos

        val deviceVersionCode = Build.VERSION.SDK_INT

        when (deviceVersionCode) {
            in 21..29 -> {
                viewModel.deleteMedia(videos, mContext)
                deleteSync()
            }
            else -> viewModel.deleteMedia(videos, mContext)
        }


        //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
        //MediaStoreCompat.fromFileName(requireContext(), MediaType.VIDEO, video.videoName)
        //}
        //}
    }

    fun fileExists(context: Context, uri: Uri): Boolean {
        return if ("file" == uri.scheme) {
            val file = DocumentFile.fromSingleUri(mContext, uri)
            file!!.exists()
        } else {
            try {
                val inputStream = mContext.contentResolver.openInputStream(uri)
                inputStream!!.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DELETE -> deleteSync()
        }
    }

    fun deleteSync() {

        launch {
            withContext(Dispatchers.Default) {
                myloop@ for (videoToDelete in viewModel.videosToDelete) {
                    for (videoItem in MainActivityViewModel.videos.value!!) {
                        if (videoToDelete.videoId == videoItem.videoId) {
                            MainActivityViewModel.videos.value?.remove(videoItem)
                            continue@myloop
                        }
                    }
                }
            }
            withContext(Dispatchers.Default) {
                for (video in viewModel.videosToDelete) {
                    MainActivityViewModel.folders.value?.get(args.position)!!.videoFiles.remove(
                        video
                    )
                }
            }
            withContext(Dispatchers.Default) {
                MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                if (viewModel.videosToDelete.size > 0)
                    viewModel.videosToDelete.clear()
            }
        }

    }

    fun setUpToolbarMenu() {
        val sortOrder =
            sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
        binding.toolbarFoldersVideosFragment.menu!!.updateCheckedStatus(sortOrder)
        val videoFiles = MainActivityViewModel.folders.value?.get(args.position)!!.videoFiles

        binding.toolbarFoldersVideosFragment.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sort_by_newest_date_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(
                        Constants.SORT_ORDER,
                        SortOrder.NEWEST_DATE_FIRST.order
                    )
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortByDescending { it.date_added }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_oldest_date_first_menu_item -> {
                    adapter.submitList(listOf<videoContent>())
                    it.isChecked = true
                    sharedPreferences.saveInt(
                        Constants.SORT_ORDER,
                        SortOrder.OLDEST_DATE_FIRST.order
                    )
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortBy { it.date_added }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_largest_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.LARGEST_FIRST.order)
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortByDescending { it.videoSize }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_smallest_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.SMALLEST_FIRST.order)
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortBy { it.videoSize }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_name_a_to_z_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.NAME_A_TO_Z.order)
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortBy { it.videoName }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_name_z_to_a_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.NAME_Z_TO_A.order)
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortByDescending { it.videoName }
                            adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    fun submitNewFiles() {
        adapter.submitList(MainActivityViewModel.folders.value?.get(args.position)?.videoFiles)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}