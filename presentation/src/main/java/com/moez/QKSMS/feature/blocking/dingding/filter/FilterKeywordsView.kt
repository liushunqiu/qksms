package com.moez.QKSMS.feature.blocking.dingding.filter

import com.moez.QKSMS.common.base.QkViewContract
import io.reactivex.Observable

interface FilterKeywordsView : QkViewContract<FilterKeywordsState> {

    fun unfilterKeywords(): Observable<Long>

    fun addKeywords(): Observable<*>

    fun saveKeywords(): Observable<String>

    fun showAddKeyWordsDialog()
}