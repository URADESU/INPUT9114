package com.kobayashi.user.put

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.ImageView
import com.kobayashi.user.put.Constants.Companion.ANIMATION_DURATION
import com.kobayashi.user.put.Constants.Companion.FAVORITE_FRAGMENT
import com.kobayashi.user.put.Constants.Companion.RESULT_FRAGMENT

/**
 * name: CustomAdapter
 *
 * Find Usages: ResultFragment, FavoriteFragment
 *
 * What effect: ListViewのAdapter
 *
 * 参考サイト: http://d.hatena.ne.jp/paraches/20130107
 */
class CustomAdapter(context: Context, textViewResourceId: Int, labelList: MutableList<String>, whichFragment: Int) : ArrayAdapter<String>(context,textViewResourceId,labelList){
    //itemのViewリソースをinflateするinflater
    private val mInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    //生成元のlistをメンバ変数に保持 (※同一のインスタンス)
    private val mLabelList = labelList
    //PrefListClassのインスタンス  (mLabelListと互換を保つため、端末に保存されている単語を操作するクラス)
    private val prefList = PrefList(context)
    //呼び出し元のFragmentを判別する整数
    private val mWhichFragment = whichFragment
    //画面下部に表示するトースト
    private val makeToast = MakeToast(context)
    //端末のクリップボードにアクセスする
    private val clipBoard = ClipBoard(context)

    private var favoriteAdapter : CustomAdapter? = null
    private var resultAdapter : CustomAdapter? = null

    private var itemResource: Int = 0

    private inner class ViewHolder {
        var needInflate: Boolean = false
        var text: TextView? = null
        var imageView: ImageView? = null
        var alreadyFav = false
        var prefNum: Int? = null
    }

    init {
        when (mWhichFragment) {
            RESULT_FRAGMENT -> itemResource = R.layout.result_item_layout
            FAVORITE_FRAGMENT -> itemResource = R.layout.fav_item_layout
            else -> itemResource = R.layout.result_item_layout
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val vh: ViewHolder
        val cell: String = getItem(position) as String      //itemのtextを保持
        val anim: Animation = AnimationUtils.loadAnimation(context, R.anim.item_motion)     //animフォルダのXMLを読み込む

        when {
            convertView == null -> {
                //convertViewが使いまわせないので、新規でinflateする
                view = mInflater.inflate(itemResource, parent, false)
                //ViewHolderにViewを登録する
                setViewHolder(view)
            }
            (convertView.tag as ViewHolder).needInflate -> {
                //needInflate が true なら、その潰れた View は使わずに新しく view を作る
                view = mInflater.inflate(itemResource, parent, false)
                //ViewHolderにViewを登録する
                setViewHolder(view)
            }
            (convertView.tag as ViewHolder).alreadyFav -> {
                view = mInflater.inflate(itemResource, parent, false)
                setViewHolder(view)
            }
            //convertViewを使いまわす
            else -> view = convertView
        }


        vh = view.tag as ViewHolder
        vh.text!!.text = cell       //viewにitemのtextを代入

        //FavoriteFragmentの処理
        if(mWhichFragment == FAVORITE_FRAGMENT){
            //imageViewに削除処理を実装
            vh.imageView!!.setOnClickListener({ deleteCell(view, cell) })
        }
        //ResultFragmentの処理
        else if(mWhichFragment == RESULT_FRAGMENT){
            val list = prefList.getItemList()
            var count = 0

            //TODO: 下記の処理は、効率的に考えて最悪な実装になっているため、余裕があれば修正しアップデートを行う
            //お気に入りに1単語以上登録されている場合、
            // 現在表示されている検索結果画面のリストの単語と比較を行う
            if(list.size >= 1){
                while (count < list.size){
                    //お気に入りリストと検索結果画面リストの単語が一致するか
                    if((vh.text!!.text).equals(list[count])){
                        vh.alreadyFav = true
                        vh.prefNum = count
                        break
                    }
                    count++
                }
            }

            //検索結果画面、item押下時の処理
            vh.imageView!!.setOnClickListener({

                //itemがすでに登録されている場合、お気に入りリストから当該itemを削除
                if(vh.alreadyFav){
                    vh.imageView!!.setImageResource(R.drawable.unfav_icon)
                    vh.alreadyFav = false
                    prefList.deleteItem(cell)
                    favoriteAdapter!!.deleteCellFromResult(cell)
                }
                else{

                    //prefListに30件以上、登録がある場合はトーストを表示する
                    if(prefList.getNum() >= 30){
                        makeToast.show("お気に入りに保存できる単語数は30までです。")
                    }
                    //登録を行う
                    else{
                        vh.imageView!!.setImageResource(R.drawable.fav_icon)
                        vh.alreadyFav = true
                        prefList.savingItem(cell)
                        favoriteAdapter!!.addCellFromResult(cell)
                    }

                }

            })
        }

        if(vh.alreadyFav){
            vh.imageView!!.setImageResource(R.drawable.fav_icon)
        }

        //TODO リストアイテム読み込みのアニメーションが必要かどうか検討
        // リストアイテムのアニメーションを開始
//        view.startAnimation(anim)

        return view
    }


    /**
     * name: deleteCell
     *
     * What effect: アニメーションを伴いながら、リストのアイテムを削除する(お気に入り画面で使用される関数)
     */
    private fun deleteCell(v: View, cell: String) {
        val al = object : Animation.AnimationListener {

            //削除アニメーション終了後、実行される関数
            override fun onAnimationEnd(arg0: Animation) {
                val vh = v.tag as ViewHolder

                vh.needInflate = true

                //お気に入り画面上と、端末内のpreferenceファイル上から当該itemを削除する
                mLabelList.remove(cell)
                prefList.deleteItem(cell)

                notifyDataSetChanged()

                //ResultAdapterがnullじゃない (検索結果画面が存在する) 場合、変更を適用
                if(resultAdapter != null){
                    resultAdapter!!.notifyDataSetChanged()
                }

            }

            // 下記関数は使用しない
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        }

        collapse(v, al)
    }

    /**
     * name: collapse
     *
     * What effect: item削除時のアニメーションの設定
     */
    private fun collapse(v: View, al: Animation.AnimationListener?) {
        val initialHeight = v.measuredHeight
        val anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        if (al != null) {
            anim.setAnimationListener(al)
        }
        anim.duration = ANIMATION_DURATION.toLong()
        v.startAnimation(anim)
    }

    /**
     * name: addCellFromResult, deleteCellFromResult
     *
     * What effect: 検索結果画面での操作 (お気に入りに登録、または解除) を同時にお気に入り画面にも適用させる関数
     */
    private fun addCellFromResult(cell : String){
        mLabelList.add(cell)
        prefList.savingItem(cell)
        notifyDataSetChanged()
    }
    private fun deleteCellFromResult(cell : String){
        mLabelList.remove(cell)
        prefList.deleteItem(cell)
        notifyDataSetChanged()
    }

    /**
     * name: setViewHolder
     *
     * What effect: 表示されているListViewの情報を保持するための関数
     */
    private fun setViewHolder(view: View) {
        val vh = ViewHolder()
        vh.text = view.findViewById(R.id.tv) as TextView
        vh.imageView = view.findViewById(R.id.cell_button) as ImageView

        //itemを長押しで端末のクリップボードにコピーができるよう、リスナーを実装
        view.setOnLongClickListener {

            clipBoard.copy(vh.text!!.text as String)

            makeToast.show(vh.text!!.text as String + " をクリップボードにコピーしました。")

            return@setOnLongClickListener true
        }

        if(mWhichFragment == RESULT_FRAGMENT){
            if(vh.alreadyFav){
                vh.imageView!!.setImageResource(R.drawable.fav_icon)
            }else{
                vh.imageView!!.setImageResource(R.drawable.unfav_icon)
            }
        }
        else if(mWhichFragment == FAVORITE_FRAGMENT){
            vh.imageView!!.setImageResource(R.drawable.trash_icon)
        }

        vh.needInflate = false
        view.tag = vh
    }

    /**
     * name: setAdapterFromResult
     *
     * Find Usages: ResultFragment
     *
     * What effect: favoriteListのAdapterをメンバに登録する
     */
    fun setAdapterFromResult(adapter: CustomAdapter){
        this.favoriteAdapter = adapter
    }

    /**
     * name: setAdapterFromFavorite
     *
     * Find Usages: FavoriteFragment
     *
     * What effect: resultListのAdapterをメンバに登録する
     */
    fun setAdapterFromFavorite(adapter: CustomAdapter){
        this.resultAdapter = adapter
    }

}