package com.dudziak.daniel.githubcommitssender.controllers

import android.app.ActivityManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import androidx.recyclerview.selection.SelectionTracker
import com.dudziak.daniel.githubcommitssender.GithubCommitsRequester
import com.dudziak.daniel.githubcommitssender.MainActivity
import com.dudziak.daniel.githubcommitssender.R
import com.dudziak.daniel.githubcommitssender.model.Commit
import com.dudziak.daniel.githubcommitssender.util.RequestCounter
import com.dudziak.daniel.githubcommitssender.util.SuggestionProvider
import com.dudziak.daniel.githubcommitssender.viewModel.RepositoriesDbHelper


class MainController(private val mainActivity: MainActivity) {
    private var titleRefresher: RequestCounter.ActionBarTitleRefresher? = null
    private var requestCounter: RequestCounter? = null
    var menu: Menu? = null

    fun init() {
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
            handleSearchAction(intent)
        } else if (GithubCommitsRequester.ACTION_SEND_COMMITS == intent.action) {
            handleCommitsIntent(intent)
        }
    }

    private fun handleSearchAction(intent: Intent) {
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
            mainActivity.showToast("""This "$query" not look like owner/repository""")
        }
        if (menu != null) {
            //close menu after search
            (menu!!.findItem(R.id.search_action)).collapseActionView()
        }
    }

    private fun doMySearch(query: String) {
        if (mainActivity.isProgressbarVisible() || isServiceRunning(GithubCommitsRequester::class.java.name)) {
            mainActivity.showToast("Ups we already working on your previous request")
        } else if (!isInternetConnection()) {
            mainActivity.showToast("Ups there is not an internet connection")
        } else {
            GithubCommitsRequester.startActionRequestCommits(mainActivity, query)
            mainActivity.showProgressBar()
        }
    }

    private fun isInternetConnection(): Boolean {
        val cm =
            mainActivity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun handleCommitsIntent(intent: Intent) {
        val responseCode = intent.extras.getInt(GithubCommitsRequester.RESPONSE_CODE)
        if(responseCode == 200){
            val repositoryID = intent.extras.getString(GithubCommitsRequester.REPOSITORY_ID)
            mainActivity.setRepositoryID(repositoryID)

            val list = intent.extras.getParcelableArray(GithubCommitsRequester.COMMITS_LIST)
            mainActivity.list.clear()
            for (item in list) {
                mainActivity.list.add(item as Commit)
            }
            mainActivity.selectionTracker!!.clearSelection()
            mainActivity.viewAdapter.notifyDataSetChanged()
            requestCounter!!.addOneRequest()
            titleRefresher!!.refreshActionBarTitle()

        } else {
            mainActivity.showToast("Ups something went wrong")
        }
        mainActivity.closeProgressbar()
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

    fun handleSendMessageAction(selectionTracker: SelectionTracker<Commit>?) {
        val itemIterable = selectionTracker!!.selection.iterator()
        val stringBuilder = StringBuilder()
        while (itemIterable.hasNext()) {
            stringBuilder.append((itemIterable.next() as Commit).toMessage())
        }

        val shareBody = stringBuilder.toString()
        if (shareBody.isNotEmpty()) {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Commits")
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            mainActivity.startActivity(
                Intent.createChooser(
                    sharingIntent,
                    "Share commits to..."
                )
            )
        } else {
            mainActivity.showToast("First choose commits to send")
        }

    }

    fun isServiceRunning(serviceClassName: String): Boolean {
        val activityManager =
            mainActivity.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Integer.MAX_VALUE)

        for (runningServiceInfo in services) {
            Log.d("DAJ", runningServiceInfo.service.className)
            if (runningServiceInfo.service.className.equals(serviceClassName)) {
                return true
            }
        }
        return false
    }

    fun clearDatabase() {
        val db = RepositoriesDbHelper(mainActivity.baseContext)
        RepositoriesDbHelper.clearDataBase(db)
        db.close()
    }

}