package com.example.handbook

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SimplePagerAdapterHybrid(
    private val pages: MutableList<PageType>,
    private val coverStartLayout: Any?, // can be Int or View
    private val coverEndLayout: Any?,   // can be Int or View
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return when (val page = pages[position]) {
            is PageType.CoverStart -> createCoverFragment(coverStartLayout)
            is PageType.CoverEnd -> createCoverFragment(coverEndLayout)
            is PageType.NotePage -> NotePageFragment.newInstance(page.id)
        }
    }

    private fun createCoverFragment(layout: Any?): Fragment {
        return when (layout) {
            is Int -> CoverPageFragment.newInstance(layout)
            is View -> CoverPageFragment.fromView(layout)
            else -> CoverPageFragment.fromView(null)
        }
    }

    override fun getItemId(position: Int): Long = pages[position].id
    override fun containsItem(itemId: Long): Boolean = pages.any { it.id == itemId }

    fun insertNoteAt(position: Int) {
        if (position < 1 || position >= pages.size) return
        pages.add(position, PageType.NotePage(pageNumber = position + 1))
        notifyItemInserted(position)
    }
}
