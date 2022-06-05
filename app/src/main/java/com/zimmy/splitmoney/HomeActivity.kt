package com.zimmy.splitmoney

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.zimmy.splitmoney.databinding.ActivityHomeBinding
import com.zimmy.splitmoney.fragments.ActivityFragment
import com.zimmy.splitmoney.fragments.FriendFragment
import com.zimmy.splitmoney.fragments.GroupFragment
import com.zimmy.splitmoney.fragments.SectionsPagerAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var numTab: Int = 0
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        fab = binding.newBt

        tabs.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                numTab = tab.position
                if (numTab == 0) {
                    fab.setImageResource(R.drawable.friend)
                } else if (numTab == 1) {
                    fab.setImageResource(R.drawable.group)
                } else {
                    fab.setImageResource(R.drawable.activity)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        fab.setOnClickListener { view ->
            if (numTab == 0) {
                FriendFragment.addNewFriend(baseContext)
            } else if (numTab == 1) {
                GroupFragment.addNewGroup(baseContext)
            } else {
                ActivityFragment.addNewActivity(baseContext)
            }
        }
    }
}