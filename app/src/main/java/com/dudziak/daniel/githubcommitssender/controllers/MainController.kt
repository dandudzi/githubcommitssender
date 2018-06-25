package com.dudziak.daniel.githubcommitssender.controllers

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.widget.Toast
import com.dudziak.daniel.githubcommitssender.MainActivity
import com.dudziak.daniel.githubcommitssender.R
import com.dudziak.daniel.githubcommitssender.util.RequestCounter
import com.dudziak.daniel.githubcommitssender.util.SuggestionProvider

class MainController(val mainActivity: MainActivity) {
    private var titleRefresher: RequestCounter.ActionBarTitleRefresher? = null
    private var requestCounter: RequestCounter? = null
    var menu: Menu? = null

    fun init(){
        requestCounter = RequestCounter(
            mainActivity.getPreferences(Context.MODE_PRIVATE)
        )
        titleRefresher =
                RequestCounter.ActionBarTitleRefresher(
                    mainActivity.supportActionBar,
                    requestCounter
                )
        titleRefresher!!.refreshActionBarTitle()
    }

    fun handleIntent(intent: Intent?) {
        if (Intent.ACTION_SEARCH == intent!!.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            val suggestions = SearchRecentSuggestions(
                mainActivity,
                SuggestionProvider.AUTHORITY,
                SuggestionProvider.MODE
            )
            val regex = Regex("\\w+/\\w+")
            if (regex.matches(query)) {
                suggestions.saveRecentQuery(query, null)
                doMySearch(query)
            } else {
                Toast.makeText(
                    mainActivity,
                    """This "$query" not look like owner/repository""",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            if (menu != null) {
                //close menu after search
                (menu!!.findItem(R.id.search_action)).collapseActionView()
            }
        }
    }

    private fun doMySearch(query: String) {
        requestCounter!!.addOneRequest()
        titleRefresher!!.refreshActionBarTitle()
    }

    fun clearSearchHistory() {
        val suggestions = SearchRecentSuggestions(
            mainActivity,
            SuggestionProvider.AUTHORITY,
            SuggestionProvider.MODE
        )
        suggestions.clearHistory()
    }

    fun persist() {
        requestCounter!!.persist()
    }

}