package com.dudziak.daniel.githubcommitssender.model

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import com.dudziak.daniel.githubcommitssender.R


class CommitAdapter(private val commits: List<Commit>) :
    RecyclerView.Adapter<CommitAdapter.ViewHolder>() {

    var selectionTracker: SelectionTracker<Commit>? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var authorNameTextView: TextView = itemView.findViewById(R.id.author_name)
        private var shaValueTextView: TextView = itemView.findViewById(R.id.sha_value)
        private var commitMessageTextView: TextView = itemView.findViewById(R.id.commit_message)

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Commit> {
            return CommitDetail(adapterPosition, commits[adapterPosition])
        }

        fun bind(commit: Commit, isActive: Boolean) {
            itemView.isActivated = isActive
            authorNameTextView.text = commit.authorName
            shaValueTextView.text = commit.shaValue
            commitMessageTextView.text = commit.message
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommitAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.commit_layout, parent, false)
        return ViewHolder(contactView)
    }


    override fun onBindViewHolder(viewHolder: CommitAdapter.ViewHolder, position: Int) {
        val commit = commits[position]

        viewHolder.bind(commit, selectionTracker!!.isSelected(commit))
    }


    override fun getItemCount(): Int {
        return commits.count()
    }
}


