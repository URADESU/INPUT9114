package com.kobayashi.user.put

import android.content.Context.MODE_PRIVATE
import android.R.id.edit
import android.content.Context
import org.json.JSONException
import org.json.JSONArray
import android.R.attr.keySet
import android.R.attr.languageTag
import android.os.Bundle

/**
 * name: PrefList
 *
 * Find Usages: CustomAdapter, FavoriteFragment
 *
 * What effect: 内部ファイルのpreferenceをリストとして利用する
 */
class PrefList(context: Context){
    private val mContext = context
    //現在のお気に入りリストを取得
    private val favList = getItemList()

    /**
     * name: savingItem
     *
     * Find Usages: CustomAdapter
     *
     * What effect: 引数として渡されたitemをpreferenceに追加
     */
    fun savingItem(item: String){
        //お気に入りリストにアイテムを追加
        favList.add(item)
        //リストをアップデート
        listUpdate()
    }

    /**
     * name: deleteItem
     *
     * Find Usages: CustomAdapter
     *
     * What effect: 引数として渡されたitemをpreferenceから削除
     */
    fun deleteItem(item : String){
        favList.remove(item)
        //リストをアップデート
        listUpdate()
    }

    /**
     * name: deleteItem
     *
     * What effect: PreferenceListの更新
     */
    private fun listUpdate(){
        val array = JSONArray()
        var i = 0
        val length = favList.size

        while (i < length) {
            try {
                array.put(i, favList.get(i))
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            i++
        }
        val editor = mContext.getSharedPreferences("shared_preference", MODE_PRIVATE).edit()

        editor.putString("list", array.toString())  //key名を"list"としてシリアライズ化したデータを保存
        editor.commit()
    }

    /**
     * name: getItemList
     *
     * Find Usages: CustomAdapter, FavoriteFragment
     *
     * What effect: 最新のprefListの取得
     */
    fun getItemList(): ArrayList<String>{
        val bundle = Bundle()  //保存用のバンドル
        val prefKV = mContext.getSharedPreferences("shared_preference", Context.MODE_PRIVATE).getAll()
        val keys = prefKV.keys
        for (key in keys) {
            val value = prefKV.get(key)
            if (value is String) {
                bundle.putString(key, value as String)
            } else if (value is Int) {
                bundle.putInt(key, value as Int)
            }
        }

        val stringList = bundle.getString("list")  //key名が"list"のものを取り出す
        val list = ArrayList<String>()
        try {
            val array = JSONArray(stringList)
            var i = 0
            val length = array.length()
            while (i < length) {
                list.add(array.optString(i))
                i++
            }
        } catch (e1: JSONException) {
            e1.printStackTrace()
        } catch (e2: NullPointerException){
            e2.printStackTrace()
        }

        return list
    }

    /**
     * name: getNum
     *
     * Find Usages: CustomAdapter, FavoriteFragment
     *
     * What effect: prefListの数を返す
     */
    fun getNum(): Int{
        return getItemList().size
    }
}