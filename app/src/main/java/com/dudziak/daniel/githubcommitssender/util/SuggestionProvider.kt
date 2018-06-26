package com.dudziak.daniel.githubcommitssender.util

import android.content.SearchRecentSuggestionsProvider


class SuggestionProvider : SearchRecentSuggestionsProvider() {
    companion object {
        const val AUTHORITY = "com.dudziak.daniel.githubcommitssender.SuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(
            AUTHORITY,
            MODE
        )
    }
}