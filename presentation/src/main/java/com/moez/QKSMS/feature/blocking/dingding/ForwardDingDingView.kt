package com.moez.QKSMS.feature.blocking.dingding

import com.moez.QKSMS.common.base.QkViewContract
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.Observable

/**
 * 钉钉
 */
interface ForwardDingDingView : QkViewContract<ForwardDingDingState> {


    val switchClickedIntent: Observable<*>
    //过滤关键字
    val filterKeywordsIntent: Observable<*>

    fun openFilterKeywords()

    //钉钉token
    val tokenClickedIntent: Observable<*>

    fun showTokenDialog(token: String)

    fun tokenSet(): Observable<String>
}