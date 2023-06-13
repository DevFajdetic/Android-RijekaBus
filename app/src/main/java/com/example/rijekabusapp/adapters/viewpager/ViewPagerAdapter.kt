package com.example.rijekabusapp.adapters.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>,
    private val item: Any
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = fragments[position]
        val args = Bundle()
        if (item is Line) {
            args.putSerializable(EXTRA_LINE, item as? Line)
        } else if (item is Station) {
            args.putSerializable(EXTRA_STATION, item as? Station)
        }
        fragment.arguments = args
        return fragment
    }
}
