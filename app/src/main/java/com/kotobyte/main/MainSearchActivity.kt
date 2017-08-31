package com.kotobyte.main

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toolbar

import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.databinding.ActivityMainScreenBinding
import com.kotobyte.word.WordSearchFragment
import com.kotobyte.utils.ErrorDialogFragment
import com.kotobyte.utils.ProgressDialogFragment

class MainSearchActivity : FragmentActivity(), MainScreenContracts.View {

    private lateinit var binding: ActivityMainScreenBinding
    private lateinit var presenter: MainScreenContracts.Presenter

    private val plainTextFromClipboard: CharSequence?

        get() {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            return if (clipboardManager.hasPrimaryClip()) {
                clipboardManager.primaryClip.getItemAt(0).text

            } else null
        }

    private val queryTextWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable) {
            presenter.onChangeTextOnQueryEditor(s)
        }
    }

    private val onEditorActionListener = TextView.OnEditorActionListener { textView, actionId, event ->

        if (textView.length() > 0) {

            val actionFromSoftKeyboard = actionId == EditorInfo.IME_ACTION_SEARCH
            val actionFromHardKeyboard = actionId == EditorInfo.IME_ACTION_UNSPECIFIED
            val isPressedDownEvent = event != null && event.action == KeyEvent.ACTION_DOWN

            if (actionFromSoftKeyboard || actionFromHardKeyboard && isPressedDownEvent) {
                presenter.onReceiveSearchRequest(textView.text)

                return@OnEditorActionListener true
            }
        }

        false
    }

    private val onButtonClickListener = View.OnClickListener { v ->

        if (v.id == R.id.search_button) {
            presenter.onReceiveSearchRequest(binding.queryEditor?.text ?: "")
        }

        if (v.id == R.id.clear_button) {
            presenter.onClickClearButton()
        }
    }

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->

        if (item.itemId == R.id.action_about) {
            presenter.onClickAboutMenuItem()
        }

        if (item.itemId == R.id.action_paste) {
            presenter.onClickPasteMenuItem(plainTextFromClipboard ?: "")
        }

        true
    }

    private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragment is WordSearchFragment) {
            setTextOnQueryEditor(fragment.query)

        } else {
            setTextOnQueryEditor(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)
        binding.toolbar.inflateMenu(R.menu.menu_main_screen)
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
        binding.queryEditor.setOnEditorActionListener(onEditorActionListener)
        binding.queryEditor.addTextChangedListener(queryTextWatcher)
        binding.clearButton.setOnClickListener(onButtonClickListener)
        binding.searchButton.setOnClickListener(onButtonClickListener)

        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        prepareAndStartPresenter()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()

        supportFragmentManager.removeOnBackStackChangedListener(onBackStackChangedListener)
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if (fragment is ErrorDialogFragment) {

            fragment.isCancelable = false
            fragment.onClose = { presenter.onClickRetryButton() }
        }
    }

    override fun enableSearchButton(enable: Boolean) {
        binding.searchButton.isEnabled = enable
    }

    override fun showClearButton(show: Boolean) {
        binding.clearButton.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun setTextOnQueryEditor(text: CharSequence?) {

        binding.queryEditor.setText(text)
        binding.queryEditor.selectAll()
    }

    override fun assignFocusToQueryEditor(focus: Boolean) {

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (focus) {
            binding.queryEditor.requestFocus()

            inputMethodManager.showSoftInput(binding.queryEditor, InputMethodManager.SHOW_IMPLICIT)

        } else {
            binding.queryEditor.clearFocus()

            inputMethodManager.hideSoftInputFromWindow(binding.queryEditor.windowToken, 0)
        }
    }

    override fun showError(error: Throwable) {
        Log.e(TAG, error.localizedMessage, error)

        ErrorDialogFragment.create().show(supportFragmentManager, ErrorDialogFragment::class.java.simpleName)
    }

    override fun showMigrationErrorDialog() {

        val errorDialogFragment = ErrorDialogFragment.create(
                getString(R.string.main_migration_error_title),
                getString(R.string.main_migration_error_message))

        errorDialogFragment.show(supportFragmentManager, ErrorDialogFragment::class.java.simpleName)
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

    override fun showSearchResultsScreen(query: CharSequence) {

        val fragment = WordSearchFragment.create(query.toString())

        val fragmentTransaction = supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, WordSearchFragment::class.java.simpleName)

        if (Intent.ACTION_MAIN == intent.action) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }

    override fun showAboutApplicationScreen() =
            AboutPageFragment().show(supportFragmentManager, AboutPageFragment::class.java.simpleName)

    private fun prepareAndStartPresenter() {

        presenter = MainScreenPresenter(this, ServiceLocator.databaseProvider)
        presenter.onCreate()

        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            presenter.onReceiveSearchRequest(intent.getStringExtra(Intent.EXTRA_TEXT))
        }
    }

    companion object {
        private val TAG = MainSearchActivity::class.java.simpleName

        fun createIntent(context: Context, query: String): Intent =
            Intent(context, MainSearchActivity::class.java).putExtra(Intent.EXTRA_TEXT, query)
    }
}
