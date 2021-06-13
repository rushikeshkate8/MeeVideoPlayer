package com.mee.ui.main.videos

import android.content.Context
import android.content.IntentSender
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.utils.FileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val REQUEST_CODE_DELETE = 100

class VideosViewModel : ViewModel() { // TODO: Implement the ViewModel
    //    private static MutableLiveData<List<VideoItem>> _videoItems = new MutableLiveData<>( Collections.emptyList());
    //
    //    public static LiveData<List<VideoItem>> getVideoItemsLiveData() {
    //        return _videoItems;
    //    }
    //
    //    public static void setVideoItems(List<VideoItem> videoItems) {
    //        _videoItems.setValue( videoItems );
    //    }
    var isItAMultipleSelection = MutableLiveData(false)

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