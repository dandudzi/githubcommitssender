package com.dudziak.daniel.githubcommitssender.viewModel

import androidx.recyclerview.selection.ItemDetailsLookup
import com.dudziak.daniel.githubcommitssender.model.Commit


class CommitDetail(private val adapterPosition: Int, private val selectionKey: Commit) :
    ItemDetailsLookup.ItemDetails<Commit>() {

    override fun getPosition(): Int {
        return adapterPosition
    }

    override fun getSelectionKey(): Commit? {
        return selectionKey
    }

    override fun toString(): String {
        return selectionKey.toString()
    }


}