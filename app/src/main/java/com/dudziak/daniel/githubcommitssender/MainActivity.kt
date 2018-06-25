package com.dudziak.daniel.githubcommitssender

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.OnItemActivatedListener
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.dudziak.daniel.githubcommitssender.controllers.MainController
import com.dudziak.daniel.githubcommitssender.model.Commit
import com.dudziak.daniel.githubcommitssender.model.CommitAdapter
import com.dudziak.daniel.githubcommitssender.model.CommitKeyProvider
import com.dudziak.daniel.githubcommitssender.model.CommitLookup
import android.support.annotation.NonNull
import androidx.recyclerview.selection.OnDragInitiatedListener
import android.content.ClipData.Item
import android.os.PersistableBundle
import android.support.v4.app.FragmentActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.*
import com.dudziak.daniel.githubcommitssender.controllers.ActionController
import java.nio.file.Files.size


class MainActivity : AppCompatActivity() {
    private val mainController = MainController(this)

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: CommitAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var actionMode: ActionMode? = null
    var selectionTracker: SelectionTracker<Commit>? = null
    val list = listOf(
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "6dcb09b5b57875f334f61aebed695e2e4193db5e",
            "Daniel"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kddasfasdfklasdfjaskldfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel2"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kdfasdfklasd;flasd;lkfasdkfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel3"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kdfasdfklasd;flasd;lkfasdkfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel4"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kdfasdfklasd;flasd;lkfasdkfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel5"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kdfasdfklasd;flasd;lkfasdkfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel6"
        ),
        Commit(
            "dfklja dfajklfjas kdf adasjf aksdjfas kdfasdfklasd;flasd;lkfasdkfasd|\nfaskdjfa fakfj asdfadfjad lkfadfjas dfja",
            "asdf92rj3908uaf902r30j2fuw890132",
            "Daniel7"
        )
    )

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
            itemAnimator = DefaultItemAnimator()
        }

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this@MainActivity,
                LinearLayoutManager.VERTICAL
            )
        )


        var selectionTracker = SelectionTracker.Builder<Commit>(
            "my-selection-id",
            recyclerView,
            CommitKeyProvider(1, list),
            CommitLookup(recyclerView),
            StorageStrategy.createParcelableStorage(Commit::class.java)
        )

            .withOnDragInitiatedListener {
                Log.d("COS TAM COS TAM", "onDragInitiated")
                true
            }.withOnItemActivatedListener { item, e ->
                Log.d("DAJ", "Selected ItemId: " + selectionTracker!!.hasSelection())
                selectionTracker!!.select(item.selectionKey!!)
                recyclerView.invalidateItemDecorations()
                recyclerView.invalidate()
                recyclerView.adapter!!.notifyDataSetChanged()

                true
            }
            .build()



        viewAdapter.selectionTracker = selectionTracker
        this.selectionTracker = selectionTracker

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Commit>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (selectionTracker.hasSelection() && actionMode == null) {
                    actionMode = startSupportActionMode(
                        ActionController(
                            this@MainActivity,
                            selectionTracker
                        )
                    )
                } else if (!selectionTracker.hasSelection() && actionMode != null) {
                    actionMode!!.finish()
                    actionMode = null
                }
                val itemIterable = selectionTracker.selection.iterator()
                while (itemIterable.hasNext()) {
                    Log.i("NEXT NEXT", itemIterable.next().toString())
                }
            }

            override fun onSelectionRefresh() {
                super.onSelectionRefresh()
            }

            override fun onItemStateChanged(key: Commit, selected: Boolean) {
                super.onItemStateChanged(key, selected)
            }

            override fun onSelectionRestored() {
                super.onSelectionRestored()
            }
        })

        mainController.handleIntent(intent)
        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun setUpToolbar() {
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
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
        mainController.handleIntent(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        mainController.menu = menu
        val searchItem = menu!!.findItem(R.id.search_action)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu!!.findItem(R.id.search_action).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
        searchView.isSubmitButtonEnabled = true


        //deal with bug - while searching you choose another menu item, search icon disappears
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
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
                mainController.clearSearchHistory()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
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
}
