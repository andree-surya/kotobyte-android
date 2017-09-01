package com.kotobyte.word

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.kotobyte.R
import com.kotobyte.about.AboutPageFragment
import com.kotobyte.databinding.ActivityWordDetailsBinding
import com.kotobyte.models.Word

class WordDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_word_details)

        val word: Word = intent.getParcelableExtra(EXTRA_WORD)

        binding.literalsTextView.text = WordLiteralsTextGenerator(this).createFrom(word)
        binding.sensesTextView.text = WordSensesTextGenerator(this).createFrom(word)
        binding.viewPager.adapter = WordDetailsPageAdapter(word, this, supportFragmentManager)

        supportActionBar?.title = getString(R.string.word_details,
                (word.literals.firstOrNull() ?: word.readings.first()).text)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_word_details, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_about) {
            AboutPageFragment().run { show(supportFragmentManager, this::class.java.simpleName) }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val EXTRA_WORD = "word"

        fun createIntent(context: Context, word: Word): Intent =
                Intent(context, WordDetailsActivity::class.java).putExtra(EXTRA_WORD, word)
    }
}
