package com.dudziak.daniel.githubcommitssender.controllers

import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.selection.SelectionTracker
import android.content.Context
import com.dudziak.daniel.githubcommitssender.model.Commit


class ActionController(
    private val context: Context,
    private val selectionTracker: SelectionTracker<Commit>
) : ActionMode.Callback {

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        selectionTracker.clearSelection()
    }
}