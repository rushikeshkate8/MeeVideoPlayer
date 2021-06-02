package com.mee.main.videos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mee.main.MainActivityViewModel
import com.mee.main.mainUtils.VideoItemModelBottomSheet
import com.mee.player.PlayerActivity
import com.mee.player.R
import com.mee.player.databinding.VideosFragmentBinding
import java.io.File
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.main.job
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class VideosFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    lateinit var job: Job

    private var mViewModel: VideosViewModel? = null
    var binding: VideosFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VideosFragmentBinding.inflate(inflater, container, false)
        binding!!.lifecycleOwner = this

        job = Job()

        adapter = VideosAdapter(getVideoItemClickListener(), getMoreImageViewClickListener())

        binding!!.videoItemsRecyclerView.adapter = adapter

        MainActivityViewModel.videos.observe(viewLifecycleOwner, {
            launch {
                withContext(Dispatchers.Main) {
                    adapter!!.submitList(it.toList())
                }
            }
        })
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

        var adapter: VideosAdapter? = null
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
            val modalBottomSheet = VideoItemModelBottomSheet(it)
            fragmentManager?.let { it1 ->
                modalBottomSheet.show(
                    it1,
                    VideoItemModelBottomSheet.TAG
                )
            }
        }
    }
}
