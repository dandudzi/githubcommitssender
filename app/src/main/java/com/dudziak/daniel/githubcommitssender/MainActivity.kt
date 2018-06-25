package com.dudziak.daniel.githubcommitssender

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dudziak.daniel.githubcommitssender.controllers.MainController


class MainActivity : AppCompatActivity() {

    private val mainController = MainController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbar()

        mainController.init()

        mainController.handleIntent(intent)
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
}
