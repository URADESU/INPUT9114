package com.kobayashi.user.put

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kobayashi.user.put.Constants.Companion.FAVORITE_FRAGMENT
import com.kobayashi.user.put.Constants.Companion.RESULT_FRAGMENT
import kotlinx.android.synthetic.main.favorite.*

/**
 * name: FavoriteFragment
 *
 * Find Usages: MainActivity
 *
 * What effect: お気に入り画面
 */
class FavoriteFragment : Fragment(), GeneralListener{
    companion object {
        fun newInstance(): FavoriteFragment {
            val fragment = FavoriteFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.favorite, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //PrefListから現在端末が保持しているお気に入りの単語を取得
        val prefList = PrefList(context).getItemList()
        val adapter = CustomAdapter(activity, R.layout.fav_item_layout, prefList, FAVORITE_FRAGMENT)

        //activityにfavoriteListのAdapterのインスタンスをストック
        (activity as MainActivity).setCustomAdapter(adapter, FAVORITE_FRAGMENT)
        //favoriteListにAdapterをセット
        favorite_list.adapter = adapter
    }

    /**
     * name: setResultAdapter
     *
     * Find Usages: ResultFragment
     *
     * What effect: favoriteListのAdapterにresultListのAdapterをセット (互いの操作に互換性を持たせるため)
     */
    override fun setResultAdapter(){
        (favorite_list.adapter as CustomAdapter).setAdapterFromFavorite(
                (activity as MainActivity).getCustomAdapter(RESULT_FRAGMENT)!!
        )
    }

}