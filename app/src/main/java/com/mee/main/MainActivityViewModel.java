package com.mee.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jiajunhui.xapp.medialoader.bean.VideoResult;

public class MainActivityViewModel extends ViewModel {
    private static MutableLiveData<VideoResult> _videoResult = new MutableLiveData<>(new VideoResult());


    public static void setVideoResult(VideoResult videoResult) {
        _videoResult.setValue( videoResult );
    }

    public static LiveData<VideoResult> getVideoResultLiveData() {
        return _videoResult;
    }
}
