package com.moez.QKSMS.repository

import com.moez.QKSMS.model.Keywords
import io.realm.RealmResults

/**
 * 钉钉
 */
interface ForwardDingDingRepository {

    fun getKeyWords(): RealmResults<Keywords>

    fun getKeywords(id: Long): Keywords?

    fun unKeywords(id: Long)

    fun keywords(vararg word: String)
}