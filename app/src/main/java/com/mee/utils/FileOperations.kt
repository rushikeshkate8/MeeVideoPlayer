package com.mee.utils

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.CodeBoy.MediaFacer.mediaHolders.videoContent

object FileOperations {


    @SuppressLint("NewApi") //method only call from API 29 onwards
    suspend fun deleteMedia(context: Context, video: videoContent): IntentSender? {
        var result: IntentSender? = null
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.delete(video.assetFileStringUri.toUri(), "${MediaStore.Video.Media._ID} = ?",
                    arrayOf(video.videoId.toString()))
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException ?: throw securityException

                    result = recoverableSecurityException.userAction.actionIntent.intentSender
                } else {
                    throw securityException
                }
            }
        }

        return result
    }

    @SuppressLint("NewApi") //method only call from API 30 onwards
    fun deleteMediaBulk(context: Context, videos: List<videoContent>): IntentSender {
        val uris = videos.map { it.assetFileStringUri.toUri() }
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }
}