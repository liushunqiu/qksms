package com.moez.QKSMS.feature.blocking.dingding

import com.moez.QKSMS.model.Keywords
import io.realm.RealmResults

data class ForwardDingDingState(
        val switch: Boolean = false,
        val keywords: RealmResults<Keywords>? = null,
        val token: String = ""
)
