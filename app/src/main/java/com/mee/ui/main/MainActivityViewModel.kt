package com.mee.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.mee.models.FolderVideoPair
import com.mee.ui.main.mainUtils.VideoItem

class MainActivityViewModel : ViewModel() {
    companion object {
//        var videoResult = MutableLiveData(VideoResult())
//
//        private var _updateDatabase = MutableLiveData(false)

        var isReadPermissionGranted = MutableLiveData(false)

        val videos = MutableLiveData<MutableList<videoContent>>(mutableListOf())
        val videoItems = MutableLiveData<MutableList<VideoItem>>(mutableListOf())

        val isVideosUpdated = MutableLiveData(false)

        val folders = MutableLiveData<MutableList<videoFolderContent>>(mutableListOf())
        var isFoldersUpdated = MutableLiveData(false)

        val toDeleteFolderVideoPair = mutableListOf<FolderVideoPair>()

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