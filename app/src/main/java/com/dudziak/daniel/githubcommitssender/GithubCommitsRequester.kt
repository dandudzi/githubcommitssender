package com.dudziak.daniel.githubcommitssender

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import com.dudziak.daniel.githubcommitssender.model.Commit
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.util.JsonReader
import android.util.Log
import com.dudziak.daniel.githubcommitssender.util.JsonReaderHelper


private const val ACTION_REQUEST_COMMITS =
    "com.dudziak.daniel.githubcommitssender.action.requestCommits"
private const val REPOSITORY_NAME = "com.dudziak.daniel.githubcommitssender.extra.commits"


class GithubCommitsRequester : IntentService("GithubCommitsRequester") {

    private val commits = mutableListOf<Commit>()
    private var repositoryID: String = ""
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_REQUEST_COMMITS -> {
                val repositoryName = intent.getStringExtra(REPOSITORY_NAME)
                handleActionRequestCommits(repositoryName)
            }
        }
    }

    private var responseCode = 200

    private fun handleActionRequestCommits(repositoryName: String) {
        fetchRepositoryID(repositoryName)
        fetchCommits("$repositoryName/commits")
        sendCommitsToActivity()
    }

    private fun fetchRepositoryID(repositoryName: String) {
        val connection = setUpConnection(repositoryName)
        val responseBody = connection.getInputStream()
        if (connection.responseCode == 200) {
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            val jsonReader = JsonReader(responseBodyReader)
            repositoryID = JsonReaderHelper.readKey(jsonReader, "id")
        } else {
            responseCode = connection.responseCode
        }
        connection.disconnect()
    }

    private fun setUpConnection(repositoryName: String): HttpsURLConnection {
        val github = URL("https://api.github.com/repos/$repositoryName")
        val connection = github.openConnection() as HttpsURLConnection
        connection.setRequestProperty("User-Agent", getString(R.string.MyApp))
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("Contact-Me", "daniel.dudziak2@gamil.com")
        connection.requestMethod = "GET"
        return connection
    }


    private fun fetchCommits(commitsURL: String) {
        val connection = setUpConnection(commitsURL)
        val responseBody = connection.getInputStream()
        if (connection.responseCode == 200) {
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            val jsonReader = JsonReader(responseBodyReader)
            JsonReaderHelper.readCommits(jsonReader, commits)
        } else {
            responseCode = connection.responseCode
        }
        connection.disconnect()
    }

    private fun sendCommitsToActivity() {
        val intent = Intent(ACTION_SEND_COMMITS)
        intent.putExtra(COMMITS_LIST, commits.toTypedArray())
        intent.putExtra(REPOSITORY_ID, repositoryID)
        intent.putExtra(RESPONSE_CODE, responseCode)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        const val ACTION_SEND_COMMITS = "com.dudziak.daniel.githubcommitssender.action.sendCommits"
        const val COMMITS_LIST = "com.dudziak.daniel.githubcommitssender.extra.commits"
        const val REPOSITORY_ID = "com.dudziak.daniel.githubcommitssender.extra.repositoryID"
        const val RESPONSE_CODE = "com.dudziak.daniel.githubcommitssender.extra.responseCode"
        @JvmStatic
        fun startActionRequestCommits(context: Context, repositoryName: String) {
            val intent = Intent(context, GithubCommitsRequester::class.java).apply {
                action = ACTION_REQUEST_COMMITS
                putExtra(REPOSITORY_NAME, repositoryName)
            }
            context.startService(intent)
        }


    }
}
