package com.dudziak.daniel.githubcommitssender.model

import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.support.annotation.NonNull
import androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
import androidx.recyclerview.selection.ItemDetailsLookup


class CommitLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Commit>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Commit>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is CommitAdapter.ViewHolder) {
                return (viewHolder as CommitAdapter.ViewHolder).getItemDetails()
            }
        }

        return null
    }

}