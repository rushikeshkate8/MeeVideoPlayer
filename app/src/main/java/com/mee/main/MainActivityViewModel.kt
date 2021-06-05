package com.mee.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.jiajunhui.xapp.medialoader.bean.VideoResult

class MainActivityViewModel : ViewModel() {
    companion object {
//        var videoResult = MutableLiveData(VideoResult())
//
//        private var _updateDatabase = MutableLiveData(false)

        var isReadPermissionGranted = MutableLiveData(false)

        val videos = MutableLiveData<MutableList<videoContent>>(mutableListOf())
        val isVideosUpdated = MutableLiveData(false)

        val folders = MutableLiveData<MutableList<videoFolderContent>>(mutableListOf())
        var isFoldersUpdated = MutableLiveData(false)

//        val videos: LiveData<ArrayList<videoContent>>
//            get() = _videos

//        val updateDatabase: LiveData<Boolean>
//            get() = _updateDatabase
//
//        fun needDatabaseUpdate() {
//            _updateDatabase.postValue(true)
//        }
//
//        fun databaseUpdateHandled() {
//            _updateDatabase.postValue(false)
//        }
//    }
    }
        var isBackPressed: Boolean = false

}