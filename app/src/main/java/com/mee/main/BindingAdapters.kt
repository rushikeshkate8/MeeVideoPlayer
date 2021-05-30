package com.mee.main

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("imagePath")
fun bindImage(imageView: ImageView, imagePath: String) {
    Glide.with(imageView.context).load(File(imagePath)).into(imageView)
}

@BindingAdapter("videoDuration")
fun bindVideoDurationTextView(textView: TextView, duration: Long) {
    val hr = TimeUnit.MILLISECONDS.toHours(duration)
    val min = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
        TimeUnit.MILLISECONDS.toHours(duration)
    )
    val sec = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(duration)
    )

    val formatedDuration: String

    if (hr == 0L) {
        formatedDuration = String.format("%02d:%02d", min, sec)
    } else {
        formatedDuration = String.format("%02d:%02d:%02d", hr, min, sec)
    }
    textView.text = formatedDuration
}

@BindingAdapter("videoDisplayName")
fun bindVideoDisplayNameTextView(textView: TextView, videoDisplayName: String){
    if (videoDisplayName.indexOf(".") > 0) {
         textView.text = videoDisplayName.substring(0, videoDisplayName.lastIndexOf("."))
    } else {
         textView.text = videoDisplayName
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
fun bindVideoLocation(textView: TextView, path: String) {
    val location = path.substring(0, path.lastIndexOf("/"))
    textView.text = location
}

@BindingAdapter("videoSize")
fun bindVideoSize(textView: TextView, size: Long) {
    textView.text = android.text.format.Formatter.formatFileSize(textView.context, size)
}