package com.kotobyte.word

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.kotobyte.R
import com.kotobyte.databinding.ActivityWordDetailsBinding
import com.kotobyte.models.Word

class WordDetailsActivity : AppCompatActivity() {

    private lateinit var word: Word
    private lateinit var binding: ActivityWordDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        word = intent.getParcelableExtra(EXTRA_WORD)
        binding = DataBindingUtil.setContentView<ActivityWordDetailsBinding>(this, R.layout.activity_word_details)

        binding.literalsTextView.text = WordLiteralsTextGenerator(this).createFrom(word)
        binding.sensesTextView.text = WordSensesTextGenerator(this).createFrom(word)
        binding.viewPager.adapter = WordDetailsPageAdapter(word, this, supportFragmentManager)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            finish()

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
