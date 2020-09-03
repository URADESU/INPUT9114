package com.kobayashi.user.put

import android.content.Context
import android.view.Gravity
import android.widget.Toast

/**
 * name: MakeToast
 *
 * What effect: トーストの生成をあらゆるクラスから簡易に行うためのクラス
 */
class MakeToast(context: Context): Toast(context){
    private val mContext = context
    var toast : Toast? = null

    fun show(message : String){
        toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT)
        toast!!.setGravity(Gravity.CENTER or Gravity.CENTER, 0, 0)
        toast!!.show()
    }
}