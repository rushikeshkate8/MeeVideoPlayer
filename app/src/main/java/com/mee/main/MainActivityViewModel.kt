package com.mee.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jiajunhui.xapp.medialoader.bean.VideoResult

class MainActivityViewModel : ViewModel() {
    companion object {
        var videoResult = MutableLiveData(VideoResult())
    }
    var isBackPressed: Boolean = false
}