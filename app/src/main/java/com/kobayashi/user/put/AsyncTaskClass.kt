package com.kobayashi.user.put

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * name: AsyncTaskClass
 *
 * Find Usages: SearchFragment
 *
 * What effect: 非同期でネットワーク通信を行い、結果 (該当した韻のリスト) を返すクラス
 *
 * 参考サイト: https://qiita.com/a_nishimura/items/1548e02b96bebd0d43e4
 *             https://dev.classmethod.jp/smartphone/android/asynctask/
 *             http://javazuki.com/articles/pattern-and-matcher-usage.html
 */
class AsyncTaskClass(key: String, opt: String) : AsyncTask<Void, Void, String>() {
    private var callbacktask: CallBackTask? = null
    private val key = key   //検索画面でEditText内に入力された値
    private val opt = opt   // mae = 前方一致 / comp = 完全一致 / usiro = 後方一致
    private val REMOVE_TAGS = Pattern.compile("<.+?>")  //除去用に正規表現を登録
    private var words = ArrayList<String>() //結果が得られた場合に、データ(検索した韻)が格納される配列



    /**
     * コールバック用のstaticなclass
     */
    open class CallBackTask {
        open fun CallBack(result: String) {}
    }

    //メインスレッドとは別のスレッドで実行されます。
    //非同期で処理したい内容を記述します。 このメソッドだけは必ず実装する必要があります。
    override fun doInBackground(vararg params: Void): String? {
        return getHtml()
    }

    //doInBackgroundメソッドの実行後にメインスレッドで実行されます。
    //doInBackgroundメソッドの戻り値 (getHtml()) をこのメソッドの引数として受け取ります。
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        //検索した韻で結果が得られた場合、結果を格納
        if(result!!.indexOf("word_result") >= 0){
            words = getHitWord(result)
        }
        //結果が得られなかった場合、nothingを格納
        else {
            var cell = "nothing"

            words.add(cell)
        }

        callbacktask!!.CallBack(result)
    }

    //
    fun setOnCallBack(_cbj: CallBackTask) {
        callbacktask = _cbj
    }

    /**
     * name: getHtml
     *
     * What effect: htmlデータを取得
     */
    private fun getHtml(): String {
        val client = OkHttpClient()
        // $を使って文字列の中に変数の値や計算結果を埋め込める。トリプルクォートでも使える。
        val req = Request.Builder().url("https://kujirahand.com/web-tools/Words.php?m=boin-search&key=$key&opt=$opt&len=%3F").get().build()
        val resp = client.newCall(req).execute()

        return resp.body()!!.string()
    }

    /**
     * name: getHitWord
     *
     * What effect: 取得したHTMLソースから、ヒットした単語を抜き出す
     */
    private fun getHitWord(line: String): ArrayList<String> {
        val pattern = "<ruby>(.*?)</ruby>"
        val p: Pattern = Pattern.compile(pattern)
        val m: Matcher = p.matcher(line)
        val words = ArrayList<String>()

        while (m.find()) {
            words.add(
                    removeTags(m.group(1)).toString()
            )
        }
        return words
    }

    /**
     * name: removeTags
     *
     * What effect: タグを除去
     */
    private fun removeTags(string: String?): String? {
        val m = REMOVE_TAGS.matcher(string)

        return m.replaceAll("")
    }

    /**
     * name: getWordList
     *
     * Find Usages: SearchFragment
     *
     * What effect: 該当韻をSearchFragmentへ返す
     */
    fun getWordList(): ArrayList<String>{
        return words
    }

}