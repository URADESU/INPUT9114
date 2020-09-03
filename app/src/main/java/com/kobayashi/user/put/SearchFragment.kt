package com.kobayashi.user.put

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


/**
 * name: SearchFragment
 *
 * Find Usages: MainActivity
 *
 * What effect: 検索画面
 *
 * 参考サイト: https://qiita.com/tsumuchan/items/f22d62259a38b9012760
 */
class SearchFragment : Fragment(){
    private var key: String = ""        //検索文字を保持
    private var opt: String = "comp"    //検索オプション、初期値は "完全一致"

    private lateinit var gotouButton: Button
    private lateinit var kanzenButton: Button
    private lateinit var gobiButton: Button
    private lateinit var putButton: Button

    private var makeToast : MakeToast? = null

    companion object {
        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            return fragment
        }
    }

    //FragmentのUIが描写される際に呼び出される関数
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search, container, false)
    }

    //FragmentのライフサイクルでいうとonActivityCreatedの次
    override fun onStart() {
        super.onStart()

        var keyForm: EditText = activity.findViewById(R.id.key_form)

        gotouButton = activity.findViewById(R.id.goto_button)
        kanzenButton = activity.findViewById(R.id.kanzen_button)
        gobiButton = activity.findViewById(R.id.gobi_button)
        putButton = activity.findViewById(R.id.put_button)

        makeToast = MakeToast(context)

        //ButtonViewの更新
        optReflect(opt)

        //語頭ボタン押下時
        gotouButton.setOnClickListener({
            opt = "mae"
            optReflect(opt)
        })

        //完全ボタン押下時
        kanzenButton.setOnClickListener({
            opt = "comp"
            optReflect(opt)
        })

        //語尾ボタン押下時
        gobiButton.setOnClickListener({
            opt = "usiro"
            optReflect(opt)
        })

        //PUTボタン押下時
        putButton.setOnClickListener({
            //検索フォーム内から文字を取得
            key = keyForm.text.toString()

            //未入力、または1文字以下の場合はﾄｰｽﾄを表示する(検索を行わない)
            if(key == "" || key.length <= 1){

                if(key == ""){
//                    makeToast("文字を入力してください")
                    makeToast!!.show("文字を入力してください")
                }else if(key.length <= 1){
                    makeToast!!.show("1文字以上入力してください")
                }

            }else{
                val isConnected = NetworkManager.isConnected(context)

                //オフラインでなければ、検索を行う
                if(isConnected){
                    execute()
                }else{
                    makeToast!!.show("オフライン状態です。ネットワーク接続を確認してください。")
                }

            }
        })

    }

    /**
     * name: optReflect
     *
     * What effect: 語頭、完全、語尾ボタンのいずれかを押下した際、それぞれのviewを更新する
     */
    private fun optReflect(opt: String){

        when (opt) {
            "mae" -> {
                gotouButton.setBackgroundResource(R.drawable.pressed_flame_style_button_left)
                kanzenButton.setBackgroundResource(R.drawable.ripple_search_button_center)
                gobiButton.setBackgroundResource(R.drawable.ripple_search_button_right)
            }
            "comp" -> {
                kanzenButton.setBackgroundResource(R.drawable.pressed_flame_style_button_center)
                gotouButton.setBackgroundResource(R.drawable.ripple_search_button_left)
                gobiButton.setBackgroundResource(R.drawable.ripple_search_button_right)
            }
            "usiro" -> {
                gobiButton.setBackgroundResource(R.drawable.pressed_flame_style_button_right)
                gotouButton.setBackgroundResource(R.drawable.ripple_search_button_left)
                kanzenButton.setBackgroundResource(R.drawable.ripple_search_button_center)
            }
        }
    }

    /**
     * name: execute
     *
     * What effect: ネットワーク通信を行い、結果を取得しResultFragmentへトランザクションを切り替える
     */
    private fun execute(){
        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
        var words = ArrayList<String>()     //外部のhtmlデータから該当した韻を保持するリスト

        val resultFragment = ResultFragment.newInstance()   //検索結果画面のインスタンス
        val bundle = Bundle()
        val atClass = AsyncTaskClass(key, opt)      //非同期通信を行うクラスのインスタンス

        //通信終了後のコールバック処理
        atClass.setOnCallBack(object : AsyncTaskClass.CallBackTask() {

            override fun CallBack(result: String) {
                super.CallBack(result)
                // ※１
                // resultにはdoInBackgroundの返り値が入ります。
                // ここからAsyncTask処理後の処理を記述します。

                words = atClass.getWordList()

                //検索結果画面に表示する結果リストをbundleにセット
                bundle.putSerializable("words", words)
                resultFragment.arguments = bundle

                //FragmentTransactionの入れ替え（SearchFragment → ResultFragment）
                ft.setCustomAnimations(R.anim.slide_in_ue, R.anim.slide_out_sita,
                        R.anim.slide_in_sita, R.anim.slide_out_ue)
                ft.replace(R.id.search_layout, resultFragment)
                ft.addToBackStack(null)
                ft.commit()
            }

        })

        //非同期通信処理を開始
        atClass.execute()
    }

}