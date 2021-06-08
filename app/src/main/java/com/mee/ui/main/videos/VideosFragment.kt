package com.mee.ui.main.videos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.ui.main.MainActivityViewModel
import com.mee.models.FolderVideoPair
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet.Companion.TAG
import com.mee.player.PlayerActivity
import com.mee.player.R
import com.mee.player.databinding.VideosFragmentBinding
import com.mee.utils.Constants
import com.mee.utils.SortOrder
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

private val REQUEST_CODE_DELETE = 100

class VideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job
    private lateinit var mContext: Context
    private var mViewModel: VideosViewModel? = null
    var binding: VideosFragmentBinding? = null
    private lateinit var adapter: VideosAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            val modalBottomSheet = VideoItemModelBottomSheet.newInstance(
                MainActivityViewModel.videos.value?.get(it)!!,
                VideoItemModelBottomSheet.OnClickListener {
                    deleteVideoItem(mutableListOf(it))
                })
            fragmentManager?.let { it1 ->
                modalBottomSheet.show(
                    it1,
                    TAG
                )
            }
        }
    }

    fun setUpObservers() {
        MainActivityViewModel.videos.observe(viewLifecycleOwner, {
            launch {
                withContext(Dispatchers.Main) {
                    adapter.submitList(MainActivityViewModel.videos.value?.toList())
                }
            }
        })
        mViewModel?.intentSender?.observe(viewLifecycleOwner, {
            if (it != null) {
                startIntentSenderForResult(it, REQUEST_CODE_DELETE, null, 0, 0, 0, null)
                mViewModel!!.intentSender.value = null
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


    fun deleteVideoItem(videos: MutableList<videoContent>) {

        for (video in videos) {
            if (!fileExists(mContext, video.assetFileStringUri.toUri())) {
                videos.remove(video)
                MainActivityViewModel.videos.value?.remove(video)
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

        mViewModel?.videosToDelete = videos.toMutableList()

        val deviceVersionCode = Build.VERSION.SDK_INT

        when (deviceVersionCode) {
            in 21..29 -> {
                mViewModel?.deleteMedia(videos, mContext)
                deleteSync(mViewModel?.videosToDelete!!)
            }
            else -> mViewModel?.deleteMedia(videos, mContext)
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
            REQUEST_CODE_DELETE -> deleteSync(mViewModel?.videosToDelete!!)
        }
    }

    fun deleteSync(videos: List<videoContent>) {
        val folders = MainActivityViewModel.folders.value

        for (video in videos) {
            val mPair = FolderVideoPair(File(video.path).parentFile.name, video.videoId)
            MainActivityViewModel.toDeleteFolderVideoPair.add(mPair)
        }

        if (folders != null && folders.size != 0) {
            launch {
                withContext(Dispatchers.Default) {
                    myloop@ for (pair in MainActivityViewModel.toDeleteFolderVideoPair) {
                        for (folder in MainActivityViewModel.folders.value!!) {
                            if (pair.folderName.equals(folder.folderName)) {
                                for (videoItem in folder.videoFiles) {
                                    if (videoItem.videoId == pair.videoId) {
                                        folder.videoFiles.remove(videoItem)
                                        continue@myloop
                                    }
                                }
                            }
                        }
                    }
                }
                withContext(Dispatchers.Default) {
                    MainActivityViewModel.toDeleteFolderVideoPair.clear()
                }
            }
        }

        for (video in videos) {
            MainActivityViewModel.videos.value?.remove(video)
        }

        MainActivityViewModel.videos.value = MainActivityViewModel.videos.value

        if (mViewModel?.videosToDelete?.size!! > 0)
            mViewModel?.videosToDelete?.clear()
    }

    fun setUpToolbarMenu() {
        val menu = binding?.toolbarVideosFragment?.menu!!
        val sortOrder =
            sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
        when (sortOrder) {
            SortOrder.NEWEST_DATE_FIRST.order ->
                menu.findItem(R.id.sort_by_newest_date_first_menu_item).isChecked = true
            SortOrder.OLDEST_DATE_FIRST.order ->
                menu.findItem(R.id.sort_by_oldest_date_first_menu_item).isChecked = true
            SortOrder.LARGEST_FIRST.order ->
                menu.findItem(R.id.sort_by_largest_first_menu_item).isChecked = true
            SortOrder.SMALLEST_FIRST.order ->
                menu.findItem(R.id.sort_by_smallest_first_menu_item).isChecked = true
            SortOrder.NAME_A_TO_Z.order ->
                menu.findItem(R.id.sort_by_name_a_to_z_menu_item).isChecked = true
            SortOrder.NAME_Z_TO_A.order ->
                menu.findItem(R.id.sort_by_name_z_to_a_menu_item).isChecked = true
        }
        binding!!.toolbarVideosFragment.setOnMenuItemClickListener {
            val edit: SharedPreferences.Editor = sharedPreferences.edit()
            when (it.itemId) {
                R.id.sort_by_newest_date_first_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
                }
                R.id.sort_by_oldest_date_first_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.OLDEST_DATE_FIRST.order)
                }
                R.id.sort_by_largest_first_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.LARGEST_FIRST.order)
                }
                R.id.sort_by_smallest_first_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.SMALLEST_FIRST.order)
                }
                R.id.sort_by_name_a_to_z_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.NAME_A_TO_Z.order)
                }
                R.id.sort_by_name_z_to_a_menu_item -> {
                    it.isChecked = true
                    edit.putInt(Constants.SORT_ORDER, SortOrder.NAME_Z_TO_A.order)
                }
            }
            edit.apply()
            edit.commit()
            return@setOnMenuItemClickListener true
        }
    }

    fun submitNewFiles() {
        adapter.submitList(MainActivityViewModel.videos.value)
    }

    override fun onStart() {
        super.onStart()
        setUpObservers()
        sharedPreferences =
            activity?.getSharedPreferences(Constants.VIDEO_SORT_ORDER_Pref, Context.MODE_PRIVATE)!!
        setUpToolbarMenu()
    }

    override fun onResume() {
        super.onResume()
        if (MainActivityViewModel.isReadPermissionGranted.value!!) {
            if (MainActivityViewModel.videos.value?.size == 0)
                updateVideoDatabase()
            else
                adapter.submitList(MainActivityViewModel.videos.value?.toList())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }
}
