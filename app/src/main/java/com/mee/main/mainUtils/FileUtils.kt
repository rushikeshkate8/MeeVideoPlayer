package com.mee.main.mainUtils

import android.net.Uri
import android.provider.BaseColumns
import android.provider.DocumentsContract
import java.io.File
//
//enum class UriLoadable {
//    YES, NO, MAYBE
//}
//
//fun isLoadable(uri: Uri, granted: List<Uri>): UriLoadable {
//
//    return when(uri.scheme) {
//        "content" -> {
//            if (DocumentsContract.isDocumentUri(this, uri))
//                if (documentUriExists(uri) && granted.contains(uri))
//                    UriLoadable.YES
//                else
//                    UriLoadable.NO
//            else // Content URI is not from a document provider
//                if (contentUriExists(uri))
//                    UriLoadable.YES
//                else
//                    UriLoadable.NO
//        }
//
//        "file" -> if (File(uri.path).exists()) UriLoadable.YES else UriLoadable.NO
//
//        // http, https, etc. No inexpensive way to test existence.
//        else -> UriLoadable.MAYBE
//    }
//}
//
//// All DocumentProviders should support the COLUMN_DOCUMENT_ID column
//fun documentUriExists(uri: Uri): Boolean =
//    resolveUri(uri, DocumentsContract.Document.COLUMN_DOCUMENT_ID)
//
//// All ContentProviders should support the BaseColumns._ID column
//fun contentUriExists(uri: Uri): Boolean =
//    resolveUri(uri, BaseColumns._ID)
//
//fun resolveUri(uri: Uri, column: String): Boolean {
//
//    val cursor = contentResolver.query(uri,
//        arrayOf(column), // Empty projections are bad for performance
//        null,
//        null,
//        null)
//
//    val result = cursor?.moveToFirst() ?: false
//
//    cursor?.close()
//
//    return result
//}