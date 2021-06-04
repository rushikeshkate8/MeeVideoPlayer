package com.mee.main.folders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mee.player.databinding.FoldersFragmentBinding
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent
import com.mee.main.bindVideoCountTextView
import com.mee.player.databinding.FolderItemBinding

class FoldersAdapter :
    ListAdapter<videoFolderContent, FoldersAdapter.FolderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(FolderItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(holder.absoluteAdapterPosition)

        holder.binding.folderTitleTextView.text = folder.folderName

        bindVideoCountTextView(holder.binding.folderVideoCountTextView, folder.videoFiles.size)
    }

    class FolderViewHolder(val binding: FolderItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<videoFolderContent>() {
        override fun areItemsTheSame(
            oldItem: videoFolderContent,
            newItem: videoFolderContent
        ): Boolean {
            return oldItem.bucket_id == newItem.bucket_id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: videoFolderContent,
            newItem: videoFolderContent
        ): Boolean {
            return oldItem == newItem
        }

    }
}