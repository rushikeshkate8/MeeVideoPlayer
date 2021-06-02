package com.mee.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jiajunhui.xapp.medialoader.bean.VideoResult
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent

class MainActivityViewModel : ViewModel() {
    companion object {
        var videoResult = MutableLiveData(VideoResult())
        private var _updateDatabase = MutableLiveData(false)

        val videos = MutableLiveData<MutableList<videoContent>>(mutableListOf())
//        val videos: LiveData<ArrayList<videoContent>>
//            get() = _videos

        val updateDatabase: LiveData<Boolean>
            get() = _updateDatabase

        fun needDatabaseUpdate() {
            _updateDatabase.postValue(true)
        }

        fun databaseUpdateHandled() {
            _updateDatabase.postValue(false)
        }
    }

    var isBackPressed: Boolean = false

}