package com.mee.main.mainUtils

import android.os.FileObserver
import android.util.Log
import com.mee.main.MainActivityViewModel
import java.io.File


//class FileObserverUtil(val path: String) {
//    lateinit var directoryFileObserverForFileCreate: DirectoryFileObserver
//    lateinit var directoryFileObserverForFileDelete: DirectoryFileObserver
//    lateinit var directoryFileObserverForFileDeleteSelf: DirectoryFileObserver
//
//    init {
//        directoryFileObserverForFileCreate = DirectoryFileObserver(path, FileObserver.CREATE)
//        directoryFileObserverForFileDelete = DirectoryFileObserver(path, FileObserver.DELETE)
//        directoryFileObserverForFileDeleteSelf = DirectoryFileObserver(path, FileObserver.DELETE_SELF)
//    }
//}

class DirectoryFileObserver(path: String) :
    FileObserver(path, ALL_EVENTS) {
    var aboslutePath = "path to your directory"

    override fun onEvent(event: Int, path: String?) {
            MainActivityViewModel.needDatabaseUpdate()
            stopWatching()
    }

    init {
        aboslutePath = path
        Log.i("FileObserver: ", "File Deleted")
    }
}