package com.mee.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jiajunhui.xapp.medialoader.bean.VideoResult;

public class MainActivityViewModel extends ViewModel {
    public static MutableLiveData<VideoResult> _videoResult = new MutableLiveData<>(new VideoResult());
}
