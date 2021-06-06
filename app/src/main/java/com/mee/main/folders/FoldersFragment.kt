package com.mee.main.folders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.mee.main.MainActivityViewModel
import com.mee.player.R
import com.mee.player.databinding.FoldersFragmentBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FoldersFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job

    lateinit var binding: FoldersFragmentBinding

    private var mViewModel: FoldersViewModel? = null

    private lateinit var mActivity: FragmentActivity

    lateinit var adapter: FoldersAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FoldersFragmentBinding.inflate(layoutInflater, container, false)

        binding.lifecycleOwner = this

        job = Job()

        mActivity = requireActivity()

        adapter = FoldersAdapter(FoldersAdapter.OnClickListener {
            findNavController().navigate(
                FoldersFragmentDirections.actionFoldersFragmentToFoldersVideosFragment(
                    it
                )
            )
        })
        binding.folderItemsRecyclerView.adapter = adapter

        setUpObservers()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(FoldersViewModel::class.java)
    }


    fun updateFoldersDatabase() {
        if (!MainActivityViewModel.isReadPermissionGranted.value!!)
            return

        launch {
            val foldersMutableList =
                MutableLiveData<MutableList<videoFolderContent>>(mutableListOf())

            withContext(Dispatchers.IO) {
                foldersMutableList.postValue(
                    MediaFacer
                        .withVideoContex(mActivity)
                        .getVideoFolders(VideoGet.externalContentUri)
                )
            }
            withContext(Dispatchers.IO) {
                for (item in foldersMutableList.value!!) {
                    item.videoFiles = MediaFacer.withVideoContex(mActivity)
                        .getAllVideoContentByBucket_id(item.getBucket_id())
                }
            }
            MainActivityViewModel.folders.value = foldersMutableList.value
        }
    }

    fun setUpObservers() {
        MainActivityViewModel.folders.observe(viewLifecycleOwner, {
            launch {
                if (it.size > 0) {
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

                withContext(Dispatchers.Main) {
                    adapter.submitList(MainActivityViewModel.folders.value!!.toList())
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (MainActivityViewModel.folders.value!!.size == 0)
            updateFoldersDatabase()
        else
            adapter.submitList(MainActivityViewModel.folders.value)
    }

    companion object {
        fun newInstance(): FoldersFragment {
            return FoldersFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }
}