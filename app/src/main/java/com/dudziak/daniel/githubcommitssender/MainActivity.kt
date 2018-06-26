package com.dudziak.daniel.githubcommitssender

import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.dudziak.daniel.githubcommitssender.controllers.MainController
import com.dudziak.daniel.githubcommitssender.model.Commit
import com.dudziak.daniel.githubcommitssender.viewModel.CommitAdapter
import com.dudziak.daniel.githubcommitssender.viewModel.CommitKeyProvider
import com.dudziak.daniel.githubcommitssender.viewModel.CommitLookup
import android.os.PersistableBundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.*
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    private val mainController = MainController(this)

    private lateinit var recyclerView: RecyclerView
    private lateinit var repositoryID: TextView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var progressBar: ProgressBar

    lateinit var viewAdapter: CommitAdapter

    var selectionTracker: SelectionTracker<Commit>? = null
    val list = mutableListOf<Commit>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            mainController.handleIntent(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbar()

        mainController.init()

        viewManager = LinearLayoutManager(this)

        viewAdapter = CommitAdapter(list)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val selectionTracker = setUpSelectionTracker()

        viewAdapter.selectionTracker = selectionTracker
        this.selectionTracker = selectionTracker

        setUpSendsCommitButton(selectionTracker)

        progressBar = findViewById(R.id.progressBar)
        repositoryID = findViewById(R.id.repository_id)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(GithubCommitsRequester.ACTION_SEND_COMMITS)
        )

        mainController.handleIntent(intent)

        if (savedInstanceState != null) {
            selectionTracker!!.onRestoreInstanceState(savedInstanceState)
        }
    }


    private fun setUpToolbar() {
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setOnClickListener {
            showToast(getString(R.string.unauthenticated_msg))
        }
    }

    private fun setUpSelectionTracker(): SelectionTracker<Commit>? {
        return SelectionTracker.Builder(
            "my-selection-id",
            recyclerView,
            CommitKeyProvider(1, list),
            CommitLookup(recyclerView),
            StorageStrategy.createParcelableStorage(Commit::class.java)
        )
            .withOnItemActivatedListener { item, e ->
                Log.d("DAJ", "Selected ItemId: " + this.selectionTracker!!.hasSelection())
                this.selectionTracker!!.select(item.selectionKey!!)
                recyclerView.invalidateItemDecorations()
                true
            }
            .build()
    }

    private fun setUpSendsCommitButton(selectionTracker: SelectionTracker<Commit>?) {
        val img = findViewById<ImageView>(R.id.send_message)
        img.setOnClickListener { mainController.handleSendMessageAction(selectionTracker) }
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        mainController.handleIntent(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        mainController.menu = menu
        val searchItem = menu!!.findItem(R.id.search_action)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search_action).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
        searchView.isSubmitButtonEnabled = true


        //deal with bug - while searching you choose another menu item, search icon disappears
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                menu.findItem(R.id.clear_history_action).isVisible = false
                menu.findItem(R.id.clear_database_action).isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                menu.findItem(R.id.clear_history_action).isVisible = true
                menu.findItem(R.id.clear_database_action).isVisible = true
                invalidateOptionsMenu()
                return true
            }
        })
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_history_action -> {
                mainController.clearSearchHistory()
                true
            }
            R.id.clear_database_action -> {
                mainController.clearDatabase()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mainController.persist()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        selectionTracker!!.onSaveInstanceState(outState)
    }

    fun showToast(msg: String) {
        Toast.makeText(
            this,
            msg,
            Toast.LENGTH_LONG
        )
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastReceiver)
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun closeProgressbar() {
        progressBar.visibility = View.INVISIBLE
    }

    fun isProgressbarVisible(): Boolean = progressBar.visibility == View.VISIBLE
    fun setRepositoryID(repositoryID: String?) {
        if (repositoryID.isNullOrEmpty()) {
            this.repositoryID.text = ""
        } else {
            this.repositoryID.text = "Repository ID: $repositoryID"
        }
    }

}
