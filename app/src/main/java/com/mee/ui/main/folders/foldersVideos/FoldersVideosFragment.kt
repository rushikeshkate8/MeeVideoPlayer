package com.mee.ui.main.folders.foldersVideos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import com.mee.player.databinding.FragmentFoldersVideosBinding
import com.mee.ui.main.MainActivityViewModel
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet
import com.mee.ui.main.videos.VideosAdapter
import com.mee.player.PlayerActivity
import java.io.File
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.player.R
import com.mee.ui.main.mainUtils.MyItemDetailsLookup
import com.mee.ui.main.mainUtils.OnClickListeners
import com.mee.ui.main.mainUtils.VideoItem
import com.mee.utils.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
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
    lateinit var fastAdapter: FastAdapter<VideoItem>
    lateinit var itemAdapter: ItemAdapter<VideoItem>
    private lateinit var selectExtension: SelectExtension<VideoItem>
    private val viewModel: FoldersVideosViewModel by lazy {
        ViewModelProvider(this).get(FoldersVideosViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectExtension.selectedItems.size > 0) {
                    selectExtension.deselect()
                    selectionStoped()
                }
                else {
                    //isEnabled = true
                    findNavController().navigateUp()
                }


        }})
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences =
            activity?.getSharedPreferences(Constants.VIDEO_SORT_ORDER_PREF, Context.MODE_PRIVATE)!!
        binding = FragmentFoldersVideosBinding.inflate(inflater, container, false)
        job = Job()
//        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())
//        binding.foldersVideoItemsRecyclerView.adapter = adapter
//        adapter.tracker = getTracker()
//        adapter.submitList(
//            MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.sort(
//                sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
//            )
//        )
        //create the ItemAdapter holding your Items
        itemAdapter = ItemAdapter()
        //create the managing FastAdapter, by passing in the itemAdapter
        fastAdapter = FastAdapter.with(itemAdapter)

        fastAdapter.setHasStableIds(true)

        binding.foldersVideoItemsRecyclerView.adapter = fastAdapter

        itemAdapter.add(
            MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.sort(
                sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
            )?.map { VideoItem(it, getMoreImageViewClickListener()) }?.toList()!!
        )

        binding.foldersVideosFragmentToolbar.title =
            MainActivityViewModel.folders.value?.get(args.position)?.folderName
        binding.foldersVideosFragmentToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setUpToolbarMenu()
        setUpObservers()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        selectionSetUp()
        setUpMultiSelectionToolbar()
        binding.foldersVideosFragmentToolbarMultiSelect.setNavigationOnClickListener {
            //adapter.tracker!!.clearSelection()
            selectExtension.deselect()
            selectionStoped()
        }
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

    fun getVideoItemClickListener(): OnClickListeners.OnClickListener {
        return OnClickListeners.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri =
                Uri.fromFile(
                    File(
                        it.video.path!!
                    )
                )
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    fun getMoreImageViewClickListener(): OnClickListeners.OnClickListener {
        return OnClickListeners.OnClickListener { videoItem ->
            if (selectExtension.selectedItems.size == 0) {
                val modalBottomSheet = VideoItemModelBottomSheet.newInstance(
                    videoItem.video,
                    VideoItemModelBottomSheet.OnClickListener {
                        deleteVideos(mutableListOf(videoItem))
                        selectExtension.select(itemAdapter.getAdapterPosition(videoItem))
                    })
                fragmentManager?.let { it1 ->
                    modalBottomSheet.show(
                        it1,
                        VideoItemModelBottomSheet.TAG
                    )
                }
            }
        }
    }

    fun setUpObservers() {
        MainActivityViewModel.folders.observe(viewLifecycleOwner, {
            //adapter.submitList(it.get(args.position).videoFiles.toList())
            itemAdapter.clear()
            itemAdapter.add(it.get(args.position).videoFiles.map { VideoItem(it, getMoreImageViewClickListener()) })
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


    fun deleteVideos(videoItems: List<VideoItem>) {

//        for (video in videos) {
//            if (!fileExists(mContext, video.assetFileStringUri.toUri())) {
//                videos.remove(video)
//                MainActivityViewModel.folders.value?.get(args.position)?.videoFiles?.remove(video)
//            }
//        }
//
//        if (videos.isEmpty()) {
//            submitNewFiles()
//            return
//        }


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
        val videos = videoItems.map { it.video }

        viewModel.videosToDelete = videos.toMutableList()

        val deviceVersionCode = Build.VERSION.SDK_INT

        when (deviceVersionCode) {
            in 21..29 -> {
                showDeleteAlertDialog(videoItems.size)
            }
            else -> viewModel.deleteMedia(videos, mContext)
        }


        //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
        //MediaStoreCompat.fromFileName(requireContext(), MediaType.VIDEO, video.videoName)
        //}
        //}
    }

    fun showDeleteAlertDialog(count: Int) {
        val title: String
        val message: String
        if (count == 1) {
            title = resources.getString(R.string.delete_song)
            message = String.format(resources.getString(R.string.delete_one_song), count)
        } else {
            title = resources.getString(R.string.delete_songs)
            message = String.format(resources.getString(R.string.delete_multiple_songs), count)
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setPositiveButton(R.string.delete, { dialog, which ->
                viewModel.deleteMedia(viewModel.videosToDelete, mContext)
                deleteSync()
            })
            .setNegativeButton(R.string.cancel, { dialog, which ->
                selectExtension.deselect()
            })
            .setMessage(message)
            .setOnDismissListener { selectExtension.deselect()
                selectionStoped()
            }
            .setOnCancelListener { selectExtension.deselect()
                selectionStoped() }
        val alertDialog = builder.create()
        alertDialog.setTitle(
            Html.fromHtml(
                "<font color='${resources.getColor(R.color.color_primary)}'>${
                    title
                }</font>"
            )
        )

        alertDialog.show()
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
            REQUEST_CODE_DELETE -> {
                if (resultCode == Activity.RESULT_OK)
                    deleteSync()
                else {
                    selectExtension.deselect()
                    selectionStoped()
                }
            }
        }
    }

    fun deleteSync() {
        //MainActivityViewModel.needVideoDatabaseUpdate.value = true
        val deletedItems = selectExtension.selectedItems.toList()
        selectExtension.deleteAllSelectedItems()


        launch {
            withContext(Dispatchers.Default) {
//                myloop@ for (videoToDelete in viewModel.videosToDelete) {
//                    for (videoItem in MainActivityViewModel.videos.value!!) {
//                        if (videoToDelete.videoId == videoItem.videoId) {
//                            MainActivityViewModel.videos.value?.remove(videoItem)
//                            continue@myloop
//                        }
//                    }
//                }
                val videoDatabase = MainActivityViewModel.videoItems.value
                myloop@ for (item in deletedItems) {
                    for (index in 0..videoDatabase!!.size - 1) {
                        if (item.video.videoId == videoDatabase.get(index).video.videoId) {
                            MainActivityViewModel.videoItems.value?.removeAt(index)
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
                //MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                if (viewModel.videosToDelete.size > 0)
                    viewModel.videosToDelete.clear()
            }
        }

    }

    fun setUpToolbarMenu() {
        val sortOrder =
            sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
        binding.foldersVideosFragmentToolbar.menu!!.updateCheckedStatus(sortOrder)
        val videoFiles = MainActivityViewModel.folders.value?.get(args.position)!!.videoFiles

        binding.foldersVideosFragmentToolbar.setOnMenuItemClickListener {
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
                            //adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Default) {
                            MainActivityViewModel.folders.postValue(MainActivityViewModel.folders.value)
                        }
                    }
                }
                R.id.sort_by_oldest_date_first_menu_item -> {
                    //adapter.submitList(listOf<videoContent>())
                    it.isChecked = true
                    sharedPreferences.saveInt(
                        Constants.SORT_ORDER,
                        SortOrder.OLDEST_DATE_FIRST.order
                    )
                    launch {
                        withContext(Dispatchers.Default) {
                            videoFiles.sortBy { it.date_added }
                            //adapter.submitList(listOf<videoContent>())
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
                           // adapter.submitList(listOf<videoContent>())
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
                            //adapter.submitList(listOf<videoContent>())
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
                            //adapter.submitList(listOf<videoContent>())
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
                            //adapter.submitList(listOf<videoContent>())
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

    fun setUpMultiSelectionToolbar() {
        binding.foldersVideosFragmentToolbarMultiSelect.setOnMenuItemClickListener {
            //selectionStoped()
            when (it.itemId) {
                R.id.multi_select_delete -> {

//                    mViewModel.isItAMultipleSelection.value = true
//                    Toast.makeText(context, "Delete Clicked", Toast.LENGTH_SHORT).show()
//                    val deletionVideoList = mutableListOf<videoContent>()
////                    for (position in adapter.tracker?.selection!!)
////                        deletionVideoList.add(adapter.currentList.get(position.toInt()))
////                    adapter.tracker?.clearSelection()
//                    for (position in flexibleAdapter.selectedPositions) {
//                        deletionVideoList.add((flexibleAdapter.currentItems.get(position) as VideoItem).video)
//                    }
//                    flexibleAdapter.removeAllSelectedItems()
//                    selectionStoped()
//                    deleteVideos(deletionVideoList)

                    deleteVideos(selectExtension.selectedItems.toMutableList())
                    return@setOnMenuItemClickListener true
                }
                R.id.multi_select_share -> {
                    val uriArrayList = arrayListOf<Uri>()
                    for (uri in selectExtension.selectedItems.map { it.video.assetFileStringUri.toUri() })
                        uriArrayList.add(uri)
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                    intent.type = "video/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    intent.putExtra(Intent.EXTRA_STREAM, uriArrayList)

                    ActivityCompat.startActivity(requireContext(), intent, null)

                    selectExtension.deselect()

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener true
            }
        }
    }

    fun getTracker(): SelectionTracker<Long> {
        val tracker = SelectionTracker.Builder<Long>(
            "foldersVideosSelection",
            binding.foldersVideoItemsRecyclerView,
            StableIdKeyProvider(binding.foldersVideoItemsRecyclerView),
            MyItemDetailsLookup(binding.foldersVideoItemsRecyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val nItems: Int = tracker.selection.size()
                    if (nItems == 2) {

                    }
                }
            })
        return tracker
    }

    fun selectionStarted() {
        binding.foldersVideosFragmentToolbar.visibility = View.GONE
        binding.foldersVideosFragmentToolbarMultiSelect.visibility = View.VISIBLE
        activity?.window?.statusBarColor = resources.getColor(R.color.color_primary)
        val decorView: View = activity?.window?.decorView!!
        var systemUiVisibilityFlags = decorView.systemUiVisibility
        systemUiVisibilityFlags =
            systemUiVisibilityFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        decorView.systemUiVisibility = systemUiVisibilityFlags
    }

    fun selectionStoped() {
        //flexibleAdapter.clearSelection()
        binding.foldersVideosFragmentToolbar.visibility = View.VISIBLE
        binding.foldersVideosFragmentToolbarMultiSelect.visibility = View.GONE
        activity?.window?.statusBarColor =
            resources.getColor(R.color.color_background)


        val nightModeFlags = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
            }
            else -> {
                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
//                            Configuration.UI_MODE_NIGHT_NO -> doStuff()
//                            Configuration.UI_MODE_NIGHT_UNDEFINED -> doStuff()
        }
    }

    private fun toggleSelection(position: Int) {
        val count = selectExtension.selectedItems.size
        if (count == 0) {
            selectionStoped()
        } else if (count == 1) {
            selectionStarted()
            binding.foldersVideosFragmentToolbarMultiSelect.title = String.format(
                resources.getString(R.string.items_selected),
                count.toString()
            )
        } else {
            if (binding.foldersVideosFragmentToolbarMultiSelect.visibility == View.GONE)
                selectionStarted()
            binding.foldersVideosFragmentToolbarMultiSelect.title = String.format(
                resources.getString(R.string.items_selected),
                count.toString()
            )
        }
    }

    fun selectionSetUp() {

        selectExtension = fastAdapter.getSelectExtension()
        selectExtension.apply {
            isSelectable = true
            selectWithItemUpdate = true
            multiSelect = true
            selectOnLongClick = true
            selectionListener = object : ISelectionListener<VideoItem> {
                override fun onSelectionChanged(item: VideoItem, selected: Boolean) {

                    Log.i(
                        "FastAdapter",
                        "SelectedCount: " + selectExtension.selections.size + " ItemsCount: " + selectExtension.selectedItems.size
                    )
                }
            }
        }
        fastAdapter.onClickListener =
            { v: View?, iAdapter: IAdapter<VideoItem>, videoItem: VideoItem, postion: Int ->
                if (selectExtension.selectedItems.size > 0) {
                    selectExtension.toggleSelection(postion)
                    toggleSelection(postion)
                } else {
                    val intent = Intent(activity, PlayerActivity::class.java)
                    val uri =
                        Uri.fromFile(File((videoItem).video.path))
                    intent.setDataAndType(uri, "video/*")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                true
            }
        fastAdapter.onLongClickListener =
            { v: View?, iAdapter: IAdapter<VideoItem>, videoItem: VideoItem, postion: Int ->
                toggleSelection(postion)
                true
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