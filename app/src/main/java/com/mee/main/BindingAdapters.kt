package com.mee.main

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.mee.player.R
import kotlinx.coroutines.*
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

val job = Job()
val coroutineContext: CoroutineContext = Dispatchers.Main + job

@BindingAdapter("imagePath")
fun bindImage(imageView: ImageView, imagePath: Uri) {
    Glide.with(imageView.context).load(imagePath).into(imageView)
}

@BindingAdapter("videoDuration")
fun bindVideoDurationTextView(textView: TextView, duration: Long) {
//    val job = Job()
//    val coroutineContext: CoroutineContext = Dispatchers.Main + job

    CoroutineScope(coroutineContext).launch {

            val hr = async(Dispatchers.Default) {TimeUnit.MILLISECONDS.toHours(duration)}
            val min = async(Dispatchers.Default) {TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(duration)
            )}
            val sec = async(Dispatchers.Default) {TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )}

            val formatedDuration: Deferred<String>

            if (hr.await() == 0L) {
                formatedDuration = async(Dispatchers.Default){String.format("%02d:%02d", min.await(), sec.await())}
            } else {
                formatedDuration = async(Dispatchers.Default){String.format("%02d:%02d:%02d", hr.await(), min.await(), sec.await())}
        }
        textView.text = formatedDuration.await()
    }


//    val hr = TimeUnit.MILLISECONDS.toHours(duration)
//    val min = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
//        TimeUnit.MILLISECONDS.toHours(duration)
//    )
//    val sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
//        TimeUnit.MILLISECONDS.toMinutes(duration)
//    )
//
//    val formatedDuration: String
//
//    if (hr == 0L) {
//        formatedDuration = String.format("%02d:%02d", min, sec)
//    } else {
//        formatedDuration = String.format("%02d:%02d:%02d", hr, min, sec)
//    }
//    textView.text = formatedDuration
}

@BindingAdapter("videoDisplayName")
fun bindVideoNameTextView(textView: TextView, videoDisplayName: String?){
    CoroutineScope(coroutineContext).launch {
        if (async(Dispatchers.Default){videoDisplayName!!.indexOf(".")}.await() > 0) {
            textView.text = async(Dispatchers.Default){videoDisplayName!!.substring(0, videoDisplayName!!.lastIndexOf("."))}.await()
        } else {
            textView.text = videoDisplayName
        }
    }
}

@BindingAdapter("videoDateModifiedInMilliSec")
fun bindVideoDate(textView: TextView, dateLong: Long) {
    val simpleDateFormat = SimpleDateFormat("HH:mm  MMM dd yyyy")
    val time = dateLong * 1000L
    val date = simpleDateFormat.format(time)

    textView.setText(date)
}

@BindingAdapter("videoPath")
fun bindVideoPath(textView: TextView, path: String?) {
    val location = path!!.substring(0, path.lastIndexOf("/"))
    textView.text = location
}

@BindingAdapter("videoSize")
fun bindVideoSize(textView: TextView, size: Long?) {
    CoroutineScope(coroutineContext).launch {
        textView.text = async(Dispatchers.Default){android.text.format.Formatter.formatFileSize(textView.context, size!!)}.await()
    }
}

@BindingAdapter("videoCount")
fun bindVideoCountTextView(textView: TextView, count: Int) {
    val context = textView.context
    if(count > 1) {
        textView.setText(String.format(context.resources.getString(R.string.videos_count), count.toString()))
    }
    else {
        textView.setText(String.format(context.resources.getString(R.string.video_count), count.toString()))
    }
}
