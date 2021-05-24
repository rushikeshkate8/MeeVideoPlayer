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

        adapter = VideosAdapter(VideosAdapter.OnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            val uri = Uri.fromFile(File(it.path))
            intent.setDataAndType(uri, "video/*")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        })

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
}