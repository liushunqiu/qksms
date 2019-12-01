package com.moez.QKSMS.feature.blocking.dingding.filter

import com.moez.QKSMS.model.Keywords
import io.realm.RealmResults

data class FilterKeywordsState(
        val keywords: RealmResults<Keywords>? = null
)