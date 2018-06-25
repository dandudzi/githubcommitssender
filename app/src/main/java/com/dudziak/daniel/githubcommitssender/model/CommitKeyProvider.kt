package com.dudziak.daniel.githubcommitssender.model

import android.support.annotation.NonNull
import androidx.recyclerview.selection.ItemKeyProvider


class CommitKeyProvider(scope: Int, private val itemList: List<Commit>) : ItemKeyProvider<Commit>(scope) {
    override fun getPosition(key: Commit): Int {
        return itemList.indexOf(key)
    }

    override fun getKey(position: Int): Commit? {
        return itemList[position]
    }
}