package com.mee.ui.main.folders.foldersVideos

import android.content.Context
import android.content.IntentSender
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.mee.utils.FileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FoldersVideosViewModel: ViewModel() {
    lateinit var folder: videoFolderContent

//    fun deleteMedia(videos: List<videoContent>, activity: FragmentActivity) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
//            val intentSender = FileOperations.deleteMediaBulk(activity.baseContext, videos)
//            activity.startIntentSenderForResult(intentSender, 100, null, 0, 0, 0, null)
//        } else {
//            viewModelScope.launch {
//                for (item in videos) {
//                    val intentSender = FileOperations.deleteMedia(activity.baseContext, item)
//                }
//            }
//        }
//    }

    val intentSender = MutableLiveData<IntentSender>()

    var videosToDelete = mutableListOf<videoContent>()

    fun deleteMedia(videos: List<videoContent>, context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            intentSender.value = FileOperations.deleteMediaBulk(context, videos)

            //activity.startIntentSenderForResult(intentSender, REQUEST_CODE_DELETE, null, 0, 0, 0, null)
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    for (item in videos) {
                        FileOperations.deleteMedia(context, item)
                    }
                }
            }

        }
    }
}