package com.matuyuhi.shiftapp.component

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private val titleList: MutableList<String> = mutableListOf()

    var onScrollStart: (() -> Unit)? = null
    var onScrollEnd: (() -> Unit)? = null
    var onChangePosition: ((Int) -> Unit)? = null

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        titleList.add(title)
    }

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemId(position: Int): Long {
        // デフォルトの実装は、RecyclerView.Adapter.NO_IDを返します。
        // ただし、FragmentStateAdapterでは、各フラグメントに一意のIDが必要です。
        // このため、ここで一意のIDを返す必要があります。
        return fragmentList[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long) = fragmentList.any { it.hashCode().toLong() == itemId }

    fun getPageTitle(position: Int) = titleList[position]

    var pageChangeFunc = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onChangePosition?.let { it(position) }
            super.onPageSelected(position)
            //Log.d("position", position.toString())

        }
        var state: Int = 0
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (this.state != state){
                this.state = state
                if (state == 0) {
                    onScrollEnd?.let { it() }
                } else {
                    onScrollStart?.let { it() }
                }
            }
            // Log.d("state", state.toString())
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//            Log.d("position", position.toString())
//            Log.d("positionOffset", positionOffset.toString())
//            Log.d("positionOffsetPixels", positionOffsetPixels.toString())
        }
    }
}