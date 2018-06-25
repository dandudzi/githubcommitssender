package com.dudziak.daniel.githubcommitssender

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private var titleRefresher: RequestCounter.ActionBarTitleRefresher? = null
    private var requestCounter: RequestCounter? = null
    var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbar()

        handleIntent(intent)
    }

    private fun setUpToolbar() {
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (titleRefresher == null || requestCounter == null) {
            requestCounter = RequestCounter(getPreferences(Context.MODE_PRIVATE))
            titleRefresher =
                    RequestCounter.ActionBarTitleRefresher(supportActionBar, requestCounter)
        }
        titleRefresher!!.refreshActionBarTitle()
        toolbar!!.setOnClickListener {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.unauathenticated_msg),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (Intent.ACTION_SEARCH == intent!!.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            val suggestions = SearchRecentSuggestions(
                this,
                SuggestionProvider.AUTHORITY,
                SuggestionProvider.MODE
            )
            val regex = Regex("\\w+/\\w+")
            if (regex.matches(query)) {
                suggestions.saveRecentQuery(query, null)
                doMySearch(query)
            } else {
                Toast.makeText(
                    this@MainActivity,
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

    private fun clearSearchHistory() {
        val suggestions = SearchRecentSuggestions(
            this,
            SuggestionProvider.AUTHORITY,
            SuggestionProvider.MODE
        )
        suggestions.clearHistory()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        this.menu = menu
        val seachIteam = menu!!.findItem(R.id.search_action)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu!!.findItem(R.id.search_action).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
        //searchView.isQueryRefinementEnabled = true
        searchView.isSubmitButtonEnabled = true


        seachIteam.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                // menu.findItem(R.id.search_action).isVisible = false
                menu.findItem(R.id.clear_history_action).isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                menu.findItem(R.id.clear_history_action).isVisible = true
                invalidateOptionsMenu()
                return true
            }
        })
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear_history_action -> {
                clearSearchHistory()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        requestCounter!!.persist()
    }
}
