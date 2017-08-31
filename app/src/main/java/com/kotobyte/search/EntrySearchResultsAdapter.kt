package com.kotobyte.search

import android.support.v7.widget.RecyclerView
import com.kotobyte.models.Entry
import kotlin.properties.Delegates

abstract class EntrySearchResultsAdapter<T : Entry, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var entries: List<T> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun getItemCount(): Int = entries.size
    override fun getItemId(position: Int): Long = entries[position].ID
}
