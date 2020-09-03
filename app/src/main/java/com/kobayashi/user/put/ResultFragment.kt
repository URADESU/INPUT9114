package com.kobayashi.user.put

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.result.*
import com.kobayashi.user.put.Constants.Companion.FAVORITE_FRAGMENT
import com.kobayashi.user.put.Constants.Companion.RESULT_FRAGMENT

/**
 * name: ResultFragment
 *
 * Find Usages: SearchFragment
 *
 * What effect: 検索結果画面
 */
class ResultFragment : Fragment() {

    companion object {
        fun newInstance(): ResultFragment {
            val fragment = ResultFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.result, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        //SearchFragmentの非同期通信で得られた結果を取り出す
        val items = bundle.getSerializable("words") as ArrayList<String>
        val adapter = CustomAdapter(activity, R.layout.result_item_layout, items, RESULT_FRAGMENT)
        //FavoriteFragmentのインスタンスをactivityから取得
        val listener  = (activity as MainActivity).getFavoriteFragment() as GeneralListener

        //resultListのAdapterにfavoriteListのAdapterをセット   (互いの操作に互換性を持たせるため)
        adapter.setAdapterFromResult((activity as MainActivity).getCustomAdapter(FAVORITE_FRAGMENT)!!)
        //activityにresultListのAdapterのインスタンスをストック
        (activity as MainActivity).setCustomAdapter(adapter, RESULT_FRAGMENT)
        //resultListにAdapterをセット
        result_list.adapter = adapter
        //listener (FavoriteFragment) のメソッド (resultListのAdapterを取得) を実行
        listener!!.setResultAdapter()

    }

}