package com.zimmy.splitmoney.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.fragments.ActivityFragment
import com.zimmy.splitmoney.fragments.FriendFragment
import com.zimmy.splitmoney.fragments.GroupFragment

private val TAB_TITLES = arrayOf(
    R.string.Friends,
    R.string.Groups,
    R.string.Activity
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        var fragment: Fragment? = null
        fragment = when (position) {
            0 -> FriendFragment()
            1 -> GroupFragment()
            else -> {
                ActivityFragment()
            }
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 3
    }
}