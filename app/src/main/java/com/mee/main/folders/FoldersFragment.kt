package com.mee.main.folders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
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

        adapter = FoldersAdapter()
        binding.folderItemsRecyclerView.adapter = adapter

        setUpObservers()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(FoldersViewModel::class.java)
    }


    fun updateFoldersDatabase() {
        if (!MainActivityViewModel.isReadPermissionGranted)
            return

        launch {
            withContext(Dispatchers.IO) {
                MainActivityViewModel.folders.postValue(
                    MediaFacer
                        .withVideoContex(mActivity)
                        .getVideoFolders(VideoGet.externalContentUri)
                )
            }
            withContext(Dispatchers.IO) {
                for (item in MainActivityViewModel.folders.value!!) {
                    item.setVideoFiles(
                        MediaFacer.withVideoContex(mActivity)
                            .getAllVideoContentByBucket_id(item.getBucket_id())
                    )
                }
            }
        }.invokeOnCompletion {
            MainActivityViewModel.isFoldersUpdated.value = true
        }
    }

    fun setUpObservers() {
        MainActivityViewModel.isFoldersUpdated.observe(viewLifecycleOwner, {
            if(it) {
                launch {
                    withContext(Dispatchers.Main) {
                        adapter.submitList(MainActivityViewModel.folders.value!!.toList())
                    }
                }.invokeOnCompletion { MainActivityViewModel.isFoldersUpdated.value = false }
            }
        } )
    }

    override fun onStart() {
        super.onStart()
        if(MainActivityViewModel.folders.value!!.size == 0)
            updateFoldersDatabase()
        else
            adapter.submitList(MainActivityViewModel.folders.value)
    }

    companion object {
        fun newInstance(): FoldersFragment {
            return FoldersFragment()
        }
    }
}