package com.dudziak.daniel.githubcommitssender

import android.content.SearchRecentSuggestionsProvider


class SuggestionProvider : SearchRecentSuggestionsProvider() {
    companion object {
        val AUTHORITY = "com.dudziak.daniel.githubcommitssender.SuggestionProvider"
        val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}