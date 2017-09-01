package com.kotobyte.main

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.databinding.ActivityMainSearchBinding
import com.kotobyte.utils.ErrorDialogFragment
import com.kotobyte.utils.ProgressDialogFragment
import com.kotobyte.word.WordSearchFragment

class MainSearchActivity : AppCompatActivity(), MainSearchContracts.View {

    private lateinit var binding: ActivityMainSearchBinding
    private lateinit var presenter: MainSearchContracts.Presenter

    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    private val searchQuery: String?
        get() = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getStringExtra(SearchManager.QUERY)

    private val onQueryTextListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String?): Boolean {
            collapseSearchViewAfterDelay()

            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_search)
        presenter = MainSearchPresenter(this, ServiceLocator.databaseProvider, searchQuery)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchableInfo = searchManager.getSearchableInfo(ComponentName(this, this.javaClass))

        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem?.actionView as SearchView

        searchView?.setSearchableInfo(searchableInfo)
        searchView?.setOnQueryTextListener(onQueryTextListener)

        presenter.onCreate()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }

    override fun onStop() {
        super.onStop()

        if (searchMenuItem?.isActionViewExpanded == true) {
            searchMenuItem?.collapseActionView()
        }
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if (fragment is ErrorDialogFragment) {
            fragment.isCancelable = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {

        R.id.action_about -> {
            presenter.onClickAboutMenuItem()

            true
        }

        R.id.action_search -> {
            presenter.onClickSearchMenuItem()

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun showMigrationProgressDialog(show: Boolean) {

        val fragmentTag = ProgressDialogFragment::class.java.simpleName
        var dialogFragment: ProgressDialogFragment? = supportFragmentManager.findFragmentByTag(fragmentTag) as? ProgressDialogFragment

        if (show) {
            if (dialogFragment == null) {

                dialogFragment = ProgressDialogFragment.create(
                        getString(R.string.main_migration_progress_title),
                        getString(R.string.main_migration_progress_message))
            }

            if (!dialogFragment.isAdded) {
                dialogFragment.show(supportFragmentManager, fragmentTag)
            }

        } else if (dialogFragment != null) {
            dialogFragment.dismiss()
        }
    }

    override fun showSearchResultsScreen(query: String) {

        val fragment = WordSearchFragment.create(query)
        val tag = WordSearchFragment::class.java.simpleName

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()

        supportActionBar?.title = getString(R.string.main_title, query)
    }

    override fun showAboutApplicationScreen() {
        AboutPageFragment().run { show(supportFragmentManager, this::class.java.simpleName) }
    }

    override fun expandSearchViewWithText(text: String?) {

        searchMenuItem?.expandActionView()
        searchView?.setQuery(text, false)
    }

    override fun showUnknownError(error: Throwable) {
        Log.e(TAG, error.localizedMessage, error)

        ErrorDialogFragment.create().run { show(supportFragmentManager, this::class.java.simpleName) }
    }

    override fun showMigrationError(error: Throwable) {
        Log.e(TAG, error.localizedMessage, error)

        val title = getString(R.string.main_migration_error_title)
        val message = getString(R.string.main_migration_error_message)

        ErrorDialogFragment.create(title, message).run {
            show(supportFragmentManager, this::class.java.simpleName)
        }
    }

    private fun collapseSearchViewAfterDelay() {
        binding.root.postDelayed({ searchMenuItem?.collapseActionView() }, 500)
    }

    companion object {
        private val TAG = MainSearchActivity::class.java.simpleName

        fun createIntent(context: Context, query: String): Intent =
            Intent(context, MainSearchActivity::class.java).putExtra(Intent.EXTRA_TEXT, query)
    }
}
