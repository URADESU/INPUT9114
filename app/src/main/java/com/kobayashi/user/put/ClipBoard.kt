package com.kobayashi.user.put

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * name: ClipBoard
 *
 * Find Usages: CustomAdapter
 *
 * What effect: ListViewのitemを長押しすると、端末のクリップボードにコピーすることができる
 *
 * 参考サイト: https://qiita.com/CUTBOSS/items/97669c712449510fe7f0
 */
class ClipBoard(context : Context){
    private val clipBoardManager : ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun copy(text : String){
        clipBoardManager.primaryClip = ClipData.newPlainText("", text)
    }
}