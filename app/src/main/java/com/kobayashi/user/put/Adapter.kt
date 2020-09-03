package com.kobayashi.user.put

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentTransaction

/**
 * name: Adapter
 *
 * Find Usages: MainActivity
 *
 * What effect: AdapterViewなどのViewとデータの橋渡しを行う
 *
 * 参考サイト: http://outofmem.tumblr.com/post/77346065262/androidfragmentpageradapterの中身を動的に変更する
 */
class Adapter(fm : FragmentManager, pagerId: Int): FragmentPagerAdapter(fm) {
    private val fragments = mutableListOf<Fragment>()  //ViewPagerに表示されるFragmentを格納するﾘｽﾄ

    private var _ft: FragmentTransaction? = null
    private var _fm: FragmentManager? = fm
    private var pagerId: Int = pagerId


    override fun getItem(position: Int): Fragment{
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItemPosition(`object`: Any?): Int {
        return POSITION_NONE
    }

    override fun notifyDataSetChanged() {
        if(_ft != null){
            _ft!!.commitAllowingStateLoss()
            _ft = null
        }

        super.notifyDataSetChanged()
    }

    /**
     * name: add
     *
     * Find Usages: MainActivity
     *
     * What effect: FragmentリストにFragmentを追加
     */
    fun add(fragment: Fragment){
        fragments.add(fragment)
    }

}