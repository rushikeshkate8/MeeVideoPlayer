package com.mee.ui.main.videos

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
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.models.FolderVideoPair
import com.mee.player.PlayerActivity
import com.mee.player.R
import com.mee.player.databinding.VideosFragmentBinding
import com.mee.ui.main.MainActivityViewModel
import com.mee.ui.main.mainUtils.MyItemDetailsLookup
import com.mee.ui.main.mainUtils.OnClickListeners
import com.mee.ui.main.mainUtils.VideoItem
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet
import com.mee.ui.main.mainUtils.VideoItemModelBottomSheet.Companion.TAG
import com.mee.utils.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

private const val REQUEST_CODE_DELETE = 100

class VideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job
    private lateinit var mContext: Context
    lateinit var mTracker: SelectionTracker<Long>
    private lateinit var binding: VideosFragmentBinding
    private lateinit var adapter: VideosAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val mViewModel: VideosViewModel by lazy { ViewModelProvider(this).get(VideosViewModel::class.java) }
    lateinit var fastAdapter: FastAdapter<VideoItem>
    lateinit var itemAdapter: ItemAdapter<VideoItem>
    private lateinit var selectExtension: SelectExtension<VideoItem>
    lateinit var mParentFragmentManager: FragmentManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VideosFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        job = Job()

        //adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())
        //binding.videoItemsRecyclerView.adapter = adapter
        //mTracker = getTracker()

//        adapter.tracker = mTracker

        sharedPreferences =
            activity?.getSharedPreferences(Constants.VIDEO_SORT_ORDER_PREF, Context.MODE_PRIVATE)!!

        setUpToolbarMenu()

        setUpMultiSelectionToolbar()

        binding.videoFragmentToolbarMultiSelect.setNavigationOnClickListener {
            //adapter.tracker!!.clearSelection()
            selectExtension.deselect()
            selectionStoped()
        }

        //create the ItemAdapter holding your Items
        itemAdapter = ItemAdapter()
        //create the managing FastAdapter, by passing in the itemAdapter
        fastAdapter = FastAdapter.with(itemAdapter)

        fastAdapter.setHasStableIds(true)

        binding.videoItemsRecyclerView.adapter = fastAdapter

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        selectionSetUp()
//        if (MainActivityViewModel.videoItems.value?.size == 0)
//        if (MainActivityViewModel.videoItems.value?.isEmpty()!!) {
//            updateVideoDatabase()
//        } else {
        if (!MainActivityViewModel.videoItems.value?.isEmpty()!!) {
            MainActivityViewModel.videoItems.value!!.map {
                it.moreImageOnClickListener = getMoreImageViewClickListener()
            }
            MainActivityViewModel.videoItems.value =
                MainActivityViewModel.videoItems.value!!.sortVideoItems(
                    sharedPreferences.getInt(
                        Constants.SORT_ORDER,
                        SortOrder.NEWEST_DATE_FIRST.order
                    )
                )
        }
        setUpObservers()
//        }
    }

    private fun getVideoItemClickListener(): OnClickListeners.OnClickListener {
        return OnClickListeners.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri =
                Uri.fromFile(File(it.video.path!!))
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

//    private fun getMoreImageViewClickListener(): VideosAdapter.OnClickListener {
//        return VideosAdapter.OnClickListener {
//            val modalBottomSheet = VideoItemModelBottomSheet.newInstance(
//                MainActivityViewModel.videos.value?.get(it)!!,
//                VideoItemModelBottomSheet.OnClickListener {
//                    deleteVideos(mutableListOf(it))
//                })
//            parentFragmentManager.let { it1 ->
//                modalBottomSheet.show(
//                    it1,
//                    TAG
//                )
//            }
//        }
//    }

    private fun getMoreImageViewClickListener(): OnClickListeners.OnClickListener {
        return OnClickListeners.OnClickListener { videoItem ->
            if (selectExtension.selectedItems.size == 0) {
                val modalBottomSheet = VideoItemModelBottomSheet.newInstance(
                    videoItem.video,
                    VideoItemModelBottomSheet.OnClickListener {
                        val adapterPosition = itemAdapter.getAdapterPosition(videoItem)
                        deleteVideos(mutableListOf(videoItem))
                        selectExtension.select(adapterPosition)
                    })

                modalBottomSheet.show(
                    mParentFragmentManager,
                    TAG
                )

            }
        }
    }

    fun setUpObservers() {
        {
//        MainActivityViewModel.videos.observe(viewLifecycleOwner, {
//            launch {
//                withContext(Dispatchers.Main) {
////                    if(mViewModel.isItAMultipleSelection.value!!) {
////                        adapter = VideosAdapter(
////                            getVideoItemClickListener(),
////                            getMoreImageViewClickListener()
////                        )
////                        adapter.tracker = mTracker
////                        binding.videoItemsRecyclerView.adapter = adapter
////                        adapter.submitList(MainActivityViewModel.videos.value?.toList())
////                        mViewModel.isItAMultipleSelection.value = false
////                    }
////                    else adapter.submitList(MainActivityViewModel.videos.value?.toList())
//                    //create the ItemAdapter holding your Items
//
////                    val mutableList = mutableListOf<IFlexible<*>?>()
////                    for (video in MainActivityViewModel.videos.value!!) {
////                        mutableList.add(VideoItem(video, getMoreImageViewClickListener()))
////                    }
////                    flexibleAdapter = FlexibleAdapter(mutableList)
////                    binding.videoItemsRecyclerView.adapter = flexibleAdapter
////                    flexibleAdapter.addListener(this@VideosFragment)
////                    flexibleAdapter.mode = SelectableAdapter.Mode.MULTI
//                    itemAdapter.clear()
//                    val mutableList = mutableListOf<VideoItem>()
//                    for (video in it) {
//                        mutableList.add(VideoItem(video))
//                    }
//                    itemAdapter.add(mutableList)
//                }
//            }
//        })
        }
        MainActivityViewModel.videoItems.observe(viewLifecycleOwner, {
            itemAdapter.clear()
            itemAdapter.add(it)
        })
        mViewModel.intentSender.observe(viewLifecycleOwner, {
            if (it != null) {
                startIntentSenderForResult(it, REQUEST_CODE_DELETE, null, 0, 0, 0, null)
                mViewModel.intentSender.value = null
            }
        })
        MainActivityViewModel.isReadPermissionGranted.observe(viewLifecycleOwner, {
            if (it && MainActivityViewModel.videoItems.value?.isEmpty() == true)
                updateVideoDatabase()
        })
    }

    fun updateVideoDatabase() {
        launch {
            withContext(Dispatchers.IO) {
                MainActivityViewModel.videos.postValue(
                    async(Dispatchers.IO) {
                    MediaFacer
                        .withVideoContex(activity)
                        .getAllVideoContent(VideoGet.externalContentUri)}
                        .await()?.sort(
                            sharedPreferences.getInt(
                                Constants.SORT_ORDER,
                                SortOrder.NEWEST_DATE_FIRST.order
                            )
                        )
                )
            }

            withContext(Dispatchers.Default) {
                MainActivityViewModel.videoItems.value?.clear()
                MainActivityViewModel.videoItems.postValue(
                    MainActivityViewModel.videos.value?.map {
                        VideoItem(
                            it,
                            getMoreImageViewClickListener()
                        )
                    }?.toMutableList()
                )
            }

        }
    }


    fun deleteVideos(videoItems: MutableList<VideoItem>) {

//        for (video in videos) {
//            if (!fileExists(video.assetFileStringUri.toUri())) {
//                videos.remove(video)
//                MainActivityViewModel.videos.value?.remove(video)
//            }
//
//        }

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

        //mViewModel.videosToDelete = videos.toMutableList()
        val videos = videoItems.map { it.video }

        when (Build.VERSION.SDK_INT) {
            in 21..29 -> {
                showDeleteAlertDialog(videoItems.size, videos)
            }
            else -> mViewModel.deleteMedia(videos, mContext)
        }

        //val file = MediaStoreCompat.fromMediaId(requireContext(), MediaType.VIDEO, video.videoId)
        //MediaStoreCompat.fromFileName(xt(), MediaType.VIDEO, video.videoName)
        //}
        //}
    }

    private fun fileExists(uri: Uri): Boolean {
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
                    deleteSync(selectExtension.selectedItems.toMutableList().map { it.video })
                else {
                    selectExtension.deselect()
                    selectionStoped()
                }
            }
        }
    }

    fun showDeleteAlertDialog(count: Int, videos: List<videoContent>) {
        val title: String
        val message: String
        if (count == 1) {
            title = mContext.resources.getString(R.string.delete_song)
            message = String.format(mContext.resources.getString(R.string.delete_one_song), count)
        } else {
            title = resources.getString(R.string.delete_songs)
            message = String.format(resources.getString(R.string.delete_multiple_songs), count)
        }

        val builder = AlertDialog.Builder(mContext, R.style.AlertDialogStyle)
            .setPositiveButton(R.string.delete, { dialog, which ->
                mViewModel.deleteMedia(videos, mContext)
                deleteSync(videos)
            })
            .setNegativeButton(R.string.cancel, { dialog, which ->
                selectExtension.deselect()
            })
            .setMessage(message)
            .setOnDismissListener {
                selectExtension.deselect()
                selectionStoped()
            }
            .setOnCancelListener {
                selectExtension.deselect()
                selectionStoped()
            }
        val alertDialog = builder.create()
        alertDialog.setTitle(
            Html.fromHtml(
                "<font color='${mContext.resources.getColor(R.color.color_primary)}'>${
                    title
                }</font>"
            )
        )

        alertDialog.show()
    }

    fun deleteSync(videos: List<videoContent>) {

        for (item in selectExtension.selectedItems) {
            MainActivityViewModel.videoItems.value?.remove(item)
        }
        selectExtension.deleteAllSelectedItems()
        selectionStoped()

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
//
//        for (video in videos) {
//            //MainActivityViewModel.videos.value?.remove(video)
//        }
//
//        //MainActivityViewModel.videos.value = MainActivityViewModel.videos.value
//
//        if (mViewModel.videosToDelete.size > 0)
//            mViewModel.videosToDelete.clear()
    }

    fun setUpToolbarMenu() {
        val sortOrder =
            sharedPreferences.getInt(Constants.SORT_ORDER, SortOrder.NEWEST_DATE_FIRST.order)
        binding.toolbarVideosFragment.menu!!.updateCheckedStatus(sortOrder)

        binding.toolbarVideosFragment.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sort_by_newest_date_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(
                        Constants.SORT_ORDER,
                        SortOrder.NEWEST_DATE_FIRST.order
                    )
                    launch {
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.value?.sortByDescending { it.date_added }
                            MainActivityViewModel.videoItems.value?.sortByDescending { it.video.date_added }
                            //adapter.submitList(listOf<videoContent>())
                        }
                        withContext(Dispatchers.Main) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
                R.id.sort_by_oldest_date_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(
                        Constants.SORT_ORDER,
                        SortOrder.OLDEST_DATE_FIRST.order
                    )
                    launch {
                        withContext(Dispatchers.Default) {
//                            MainActivityViewModel.videos.value?.sortBy { it.date_added }
//                            adapter.submitList(listOf<videoContent>())
                            MainActivityViewModel.videoItems.value?.sortBy { it.video.date_added }
                        }
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
                R.id.sort_by_largest_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.LARGEST_FIRST.order)
                    launch {
                        withContext(Dispatchers.Default) {
//                            MainActivityViewModel.videos.value?.sortByDescending { it.videoSize }
//                            adapter.submitList(listOf<videoContent>())
                            MainActivityViewModel.videoItems.value?.sortByDescending { it.video.videoSize }
                        }
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
                R.id.sort_by_smallest_first_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.SMALLEST_FIRST.order)
                    launch {
                        withContext(Dispatchers.Default) {
//                            MainActivityViewModel.videos.value?.sortBy { it.videoSize }
//                            adapter.submitList(listOf<videoContent>())
                            MainActivityViewModel.videoItems.value?.sortBy { it.video.videoSize }
                        }
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
                R.id.sort_by_name_a_to_z_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.NAME_A_TO_Z.order)
                    launch {
                        withContext(Dispatchers.Default) {
//                            MainActivityViewModel.videos.value?.sortBy { it.videoName }
//                            adapter.submitList(listOf<videoContent>())
                            MainActivityViewModel.videoItems.value?.sortBy { it.video.videoName }
                        }
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
                R.id.sort_by_name_z_to_a_menu_item -> {
                    it.isChecked = true
                    sharedPreferences.saveInt(Constants.SORT_ORDER, SortOrder.NAME_Z_TO_A.order)
                    launch {
                        withContext(Dispatchers.Default) {
//                            MainActivityViewModel.videos.value?.sortByDescending { it.videoName }
//                            adapter.submitList(listOf<videoContent>())
                            MainActivityViewModel.videoItems.value?.sortByDescending { it.video.videoName }
                        }
                        withContext(Dispatchers.Default) {
                            //MainActivityViewModel.videos.postValue(MainActivityViewModel.videos.value)
                            MainActivityViewModel.videoItems.postValue(MainActivityViewModel.videoItems.value)
                        }
                    }
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    fun setUpMultiSelectionToolbar() {
        binding.videoFragmentToolbarMultiSelect.setOnMenuItemClickListener {
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

                    ActivityCompat.startActivity(mContext, intent, null)

                    selectExtension.deselect()

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener true
            }
        }
    }

    fun submitNewFiles() {
        //adapter.submitList(MainActivityViewModel.videos.value)
    }

    fun getTracker(): SelectionTracker<Long> {
        val tracker = SelectionTracker.Builder<Long>(
            "videoSelection",
            binding.videoItemsRecyclerView,
            StableIdKeyProvider(binding.videoItemsRecyclerView),
            MyItemDetailsLookup(binding.videoItemsRecyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val nItems: Int = tracker.selection.size()
                    if (nItems == 1) {
                        binding.toolbarVideosFragment.visibility = View.GONE
                        binding.videoFragmentToolbarMultiSelect.visibility = View.VISIBLE
                        activity?.window?.statusBarColor = resources.getColor(R.color.color_primary)
                        val decorView: View = activity?.window?.decorView!!
                        var systemUiVisibilityFlags = decorView.systemUiVisibility
                        systemUiVisibilityFlags =
                            systemUiVisibilityFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        decorView.systemUiVisibility = systemUiVisibilityFlags
                    }
                    if (nItems == 0) {
                        binding.toolbarVideosFragment.visibility = View.VISIBLE
                        binding.videoFragmentToolbarMultiSelect.visibility = View.GONE
                        activity?.window?.statusBarColor =
                            resources.getColor(R.color.color_background)


                        val nightModeFlags = mContext!!.resources.configuration.uiMode and
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
                    if (nItems > 0) {
                        binding.videoFragmentToolbarMultiSelect.title = String.format(
                            resources.getString(R.string.items_selected),
                            nItems.toString()
                        )
                    }
                }
            })
        return tracker
    }


    override fun onResume() {
        super.onResume()
//        if (MainActivityViewModel.isReadPermissionGranted.value!!) {
////            if (MainActivityViewModel.videos.value?.size == 0)
////                updateVideoDatabase()
////            else
////                adapter.submitList(
////                    MainActivityViewModel.videos.value?.sort(
////                        sharedPreferences.getInt(
////                            Constants.SORT_ORDER,
////                            SortOrder.NEWEST_DATE_FIRST.order
////                        )
////                    )
////                )
//            if (MainActivityViewModel.videoItems.value?.size == 0) {
//                    updateVideoDatabase()
//            } else {
//                if(itemAdapter.adapterItemCount == 0) {
//                    itemAdapter.add(MainActivityViewModel.videoItems.value!!)
//                }
//            }
//        }
    }

    fun onBackPressed(): Boolean {
//        if (adapter.tracker?.selection?.size()!! > 0) {
//            adapter.tracker!!.clearSelection()
//            return true
//        }
        if (selectExtension.selectedItems.size > 0) {
            selectExtension.deselect()
            selectionStoped()
            return true
        } else return false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mParentFragmentManager = parentFragmentManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }


//    private fun toggleSelection(position: Int) {
//        // Mark the position selected
//        flexibleAdapter.notifyItemChanged(position)
//        flexibleAdapter.toggleSelection(position)
//        val count: Int = flexibleAdapter.getSelectedItemCount()
//        if (count == 0) {
//            selectionStoped()
//        } else {
//            binding.videoFragmentToolbarMultiSelect.title = String.format(
//                resources.getString(R.string.items_selected),
//                count.toString()
//            )
//        }
//    }

    private fun toggleSelection(position: Int) {
        val count = selectExtension.selectedItems.size
        if (count == 0) {
            selectionStoped()
        } else if (count == 1) {
            selectionStarted()
            binding.videoFragmentToolbarMultiSelect.title = String.format(
                resources.getString(R.string.items_selected),
                count.toString()
            )
        } else {
            if (binding.videoFragmentToolbarMultiSelect.visibility == View.GONE)
                selectionStarted()
            binding.videoFragmentToolbarMultiSelect.title = String.format(
                resources.getString(R.string.items_selected),
                count.toString()
            )
        }
    }

    fun selectionStarted() {
        binding.toolbarVideosFragment.visibility = View.GONE
        binding.videoFragmentToolbarMultiSelect.visibility = View.VISIBLE
        activity?.window?.statusBarColor = resources.getColor(R.color.color_primary)
        val decorView: View = activity?.window?.decorView!!
        var systemUiVisibilityFlags = decorView.systemUiVisibility
        systemUiVisibilityFlags =
            systemUiVisibilityFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        decorView.systemUiVisibility = systemUiVisibilityFlags
    }

    fun selectionStoped() {
        //flexibleAdapter.clearSelection()
        binding.toolbarVideosFragment.visibility = View.VISIBLE
        binding.videoFragmentToolbarMultiSelect.visibility = View.GONE
        activity?.window?.statusBarColor =
            resources.getColor(R.color.color_background)


        val nightModeFlags = mContext.resources.configuration.uiMode and
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

}
