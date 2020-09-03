package com.kobayashi.user.put

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.kobayashi.user.put.Constants.Companion.FAVORITE_FRAGMENT
import com.kobayashi.user.put.Constants.Companion.FAVORITE_TAB
import com.kobayashi.user.put.Constants.Companion.RESULT_FRAGMENT
import com.kobayashi.user.put.Constants.Companion.SEARCH_TAB
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


/**
 * 参考サイト
 * http://outofmem.tumblr.com/post/77346065262/androidfragmentpageradapterの中身を動的に変更する
 */

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var mainLayout: LinearLayout
    private var selected: Int = 0   //Fragmentのページ番号(0 = SearchFragment / 1 = FavoriteFragment)
    private lateinit var adapter: Adapter   //ViewPager内のFragmentを管理するAdapter

    private var favCustomAdapter : CustomAdapter? = null
    private var resCustomAdapter : CustomAdapter? = null

    private var favoriteFragment : FavoriteFragment? = null     //ResultFragmentへインスタンスを渡すため、メンバ変数として保持

    private lateinit var imm: InputMethodManager
    /* -----InputMethodManagerとは-----
        * アプリケーションと現在の入力メソッドとの間の相互作用を調停する、
        * 全体的な入力メソッドフレームワーク（IMF）アーキテクチャに対する中央システムAPI
    */

    private var mAdView : AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViews()
    }

    /**
     * name: setViews
     *
     * What effect: 初期表示時のViewの設定、メンバ変数の初期化を行う
     */
    private fun setViews(){
        val fragmentManager = supportFragmentManager
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val searchFragment = SearchFragment.newInstance()
        favoriteFragment = FavoriteFragment.newInstance()
        mainLayout = findViewById(R.id.main_layout)
        toolbar = findViewById(R.id.toolBar)
        toolbarText = findViewById(R.id.toolBarText)
        tabLayout = findViewById(R.id.tabLayout)
        adapter = Adapter(fragmentManager, viewPager.id)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager



        MobileAds.initialize(this, resources.getString(R.string.YOUR_ADMOB_APP_ID))
        mAdView = findViewById(R.id.adView)
        val adRequest : AdRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)





        //キーボードを隠す  (※ 起動時、editTextにフォーカスされ、キーボードが表示されてしまうため)
        hideKeyboard()

        //ViewPagerに設置するFragmentをAdapterにセット
        adapter.add(searchFragment)
        adapter.add(favoriteFragment!!)

        //ViewPagerに表示されているFragmentのインデックスをメンバに保持
        selected = viewPager.currentItem

        setSupportActionBar(toolbar)
        /* setSupportActionBarについて
        * アクティビティの setSupportActionBar() メソッドを呼び出し、
        * アクティビティのツールバーを渡します。このメソッドにより、
        * ツールバーがアクティビティのアプリバーとして設定されます。
        *                                               by Developers
        * */

        //上記で作成したAdapterをViewPagerにセット
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            //ページが切り替わったタイミングで実行される処理
            override fun onPageSelected(position: Int) {

                 /******************************************************
                  * タブのイベント処理に関して、
                  * TabLayout自体が、ViewPagerと
                  * 密接に結びついているので、
                  * ViewPagerクラスのaddOnPageChangeListener
                  * メソッドを使用しイベントをハンドリングする
                  * ↓詳細
                  * https://qiita.com/wasnot/items/a518e81b713a09a29b34
                  ******************************************************/

                //ViewPagerに表示されているFragmentのインデックスをメンバ変数に保持
                selected = position
                //ツールバーとタブの表示を更新
                setUpTabAndToolbar()

            }

        })

        //ViewPagerとTabLayoutを関連付ける
        tabLayout.setupWithViewPager(viewPager)
        //ツールバーとタブの表示を設定
        setUpTabAndToolbar()

        supportActionBar!!.setTitle(R.string.emp)

    }

    //バックキー押下時の処理
    override fun onBackPressed() {
        val fm:FragmentManager = supportFragmentManager
        var fragments: MutableList<Fragment>
        var fragment: Fragment
        val fragmentManager: FragmentManager

        //Activity内のFragmentをListに格納
        fragments = fm.fragments
        //現在表示されているFragmentを取得
        fragment = fragments.get(selected)
        //子Fragmentが存在してる場合、子FragmentのchildFragmentManagerを取得
        fragmentManager = fragment.childFragmentManager

        //子fragmentが存在しているか
        if(0 < fragmentManager.backStackEntryCount){
            //子fragmentをスタックから取り出す
            fragmentManager.popBackStack()
        }else{
            //デフォルトのバックキーの処理
            super.onBackPressed()
        }
    }

    //※ 現状、onTouchEventが反応しない(原因不明)ため、下記メソッドで対応
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //キーボードを隠す
        hideKeyboard()

        return super.dispatchTouchEvent(ev)
    }

    /**
     * name: hideKeyboard
     *
     * What effect: ソフトウェアキーボードを非表示にし、フォーカスをactivityのレイアウトに移す
     */
    private fun hideKeyboard(){
        //キーボードを隠す
        imm.hideSoftInputFromWindow(mainLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        //SearchFragment内のEditTextから、Activityのレイアウトにフォーカスを移す
        mainLayout.requestFocus()
        mainLayout.isFocusableInTouchMode = true
    }

    /**
     * name: setUpTabAndToolbar
     *
     * What effect: 表示されているFragmentに合わせて、ツールバーのテキストとタブのアイコンを変更
     */
    private fun setUpTabAndToolbar(){
        
        when(selected){
            SEARCH_TAB -> {
                toolbarText.text = "Search"
                tabLayout.getTabAt(SEARCH_TAB)?.setIcon(R.drawable.search_icon)
                tabLayout.getTabAt(FAVORITE_TAB)?.setIcon(R.drawable.favorite_icon_un)
            }
            FAVORITE_TAB -> {
                toolbarText.text = "Favorite"
                tabLayout.getTabAt(FAVORITE_TAB)?.setIcon(R.drawable.favorite_icon)
                tabLayout.getTabAt(SEARCH_TAB)?.setIcon(R.drawable.search_icon_un)
            }
        }
        
    }

    /**
     * name: setCustomAdapter
     *
     * Find Usages: ResultFragment, FavoriteFragment
     *
     * What effect: ResultFragmentとFavoriteFragmentのCustomAdapterのインスタンスを保持
     */
    fun setCustomAdapter(adapter : CustomAdapter, which : Int){

        if(which == FAVORITE_FRAGMENT){
            this.favCustomAdapter = adapter
        }
        else if(which == RESULT_FRAGMENT){
            this.resCustomAdapter = adapter
        }

    }

    /**
     * name: getCustomAdapter
     *
     * Find Usages: ResultFragment, FavoriteFragment
     *
     * What effect: ResultFragmentとFavoriteFragmentのCustomAdapterのインスタンスをリターン
     */
    fun getCustomAdapter(which: Int) : CustomAdapter?{
        return if(which == FAVORITE_FRAGMENT){
            this.favCustomAdapter
        }
        else{
            this.resCustomAdapter
        }
    }

    /**
     * name: getFavoriteFragment
     *
     * Find Usages: ResultFragment
     *
     * What effect: FavoriteFragmentのインスタンスをリターン
     */
    fun getFavoriteFragment() : FavoriteFragment? {
        return this.favoriteFragment
    }

}
