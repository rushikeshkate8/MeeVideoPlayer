package com.mee.ui.main.mainUtils

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.mee.ui.main.videos.VideosAdapter

class MyItemKeyProvider(private val adapter: VideosAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED)
{
    override fun getKey(position: Int): Long =
        adapter.currentList[position].videoId
    override fun getPosition(key: Long): Int =
        adapter.currentList.indexOfFirst {it.videoId == key}
}
class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as VideosAdapter.VideoItemViewHolder)
                .getItemDetails()
        }
        return null
    }
}
