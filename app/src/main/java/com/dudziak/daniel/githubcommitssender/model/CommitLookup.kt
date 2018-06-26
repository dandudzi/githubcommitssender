package com.dudziak.daniel.githubcommitssender.model

import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup


class CommitLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Commit>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Commit>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view)
            if (viewHolder is CommitAdapter.ViewHolder) {
                return viewHolder.getItemDetails()
            }
        }

        return null
    }

}