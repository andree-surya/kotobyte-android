package com.kotobyte.search

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.FragmentKanjiDetailBinding
import com.kotobyte.models.Kanji
import com.kotobyte.utils.vector.VectorPathParser


class KanjiDetailDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            if (isBottomSheetEnabled) createContentView() else super.onCreateView(inflater, container, savedInstanceState)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (isBottomSheetEnabled) {

            return BottomSheetDialog(context, theme).apply {

                requestWindowFeature(DialogFragment.STYLE_NO_TITLE)
                setContentView(createContentView())
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        if (!isBottomSheetEnabled) {
            val window = dialog.window

            if (window != null) {
                val minWidth = resources.getDimension(R.dimen.kanji_detail_min_width)

                val windowAttributes = window.attributes

                if (windowAttributes.width < minWidth) {
                    windowAttributes.width = minWidth.toInt()
                    window.attributes = windowAttributes
                }
            }
        }
    }

    private fun createContentView(): View {

        val binding = DataBindingUtil.inflate<FragmentKanjiDetailBinding>(
                LayoutInflater.from(context),
                R.layout.fragment_kanji_detail, null,
                false)

        val kanji = arguments.getParcelable<Kanji>(KANJI_KEY)

        if (kanji != null) {
            binding.literalTextView.text = kanji.character.toString()
            binding.readingsTextView.text = KanjiReadingsTextGenerator(context, kanji).spannableString
            binding.meaningsTextView.text = KanjiMeaningsTextGenerator(context, kanji).spannableString
            binding.kanjiStrokesTextureView.setVectorPaths(VectorPathParser().parse(kanji.strokes))
        }

        return binding.root
    }

    private val isBottomSheetEnabled: Boolean
        get() = resources.getBoolean(R.bool.bottom_sheet_enabled)

    companion object {
        private val KANJI_KEY = "kanji"

        internal fun create(kanji: Kanji) = KanjiDetailDialogFragment().apply {

            arguments = Bundle().apply {
                putParcelable(KANJI_KEY, kanji)
            }
        }
    }
}
