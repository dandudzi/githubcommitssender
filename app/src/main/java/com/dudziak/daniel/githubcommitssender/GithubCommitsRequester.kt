package com.dudziak.daniel.githubcommitssender

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.JsonReader
import com.dudziak.daniel.githubcommitssender.model.Commit
import com.dudziak.daniel.githubcommitssender.util.DateHelper
import com.dudziak.daniel.githubcommitssender.util.JsonReaderHelper
import com.dudziak.daniel.githubcommitssender.viewModel.RepositoriesDbHelper
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


private const val ACTION_REQUEST_COMMITS =
    "com.dudziak.daniel.githubcommitssender.action.requestCommits"
private const val REPOSITORY_NAME = "com.dudziak.daniel.githubcommitssender.extra.commits"


class GithubCommitsRequester : IntentService("GithubCommitsRequester") {

    private val commits = mutableListOf<Commit>()
    private var repositoryID: String = ""
    private lateinit var date: Date
    private lateinit var db: RepositoriesDbHelper
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
        db = RepositoriesDbHelper(this.baseContext)
        fetchRepositoryID(repositoryName)
        if (responseCode == 200) {
            if (RepositoriesDbHelper.isUpToDateRepositoryInDB(db, repositoryID, date)) {
                RepositoriesDbHelper.getCommitsFromDB(db, repositoryID, commits)
            } else {
                RepositoriesDbHelper.insertRepositoryInfo(
                    db,
                    repositoryID,
                    DateHelper.dateToString(date)
                )
                fetchCommits("$repositoryName/commits")
                RepositoriesDbHelper.insertCommits(db, repositoryID, commits)
            }

        }
        sendCommitsToActivity()
        db.close()
    }

    private fun fetchRepositoryID(repositoryName: String) {
        val connection = setUpConnection(repositoryName)
        connection.getResponseCode() //we must force the getFunction to be called, if not "connection.response" code freezes
        if (connection.responseCode == 200) {
            val responseBody = connection.getInputStream()
            val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
            val jsonReader = JsonReader(responseBodyReader)
            val pair = JsonReaderHelper.readRepositoryIDAndDateOfLastUpdate(jsonReader)
            repositoryID = pair.first
            date = DateHelper.stringToDate(pair.second)
        } else {
            responseCode = connection.responseCode
        }
        connection.disconnect()
    }


    private fun setUpConnection(repositoryName: String): HttpsURLConnection {
        val github = URL("https://api.github.com/repos/$repositoryName?per_page=100")
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
