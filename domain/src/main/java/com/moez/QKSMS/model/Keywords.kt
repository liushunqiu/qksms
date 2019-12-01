package com.moez.QKSMS.model


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * 钉钉过滤关键词
 */
open class Keywords(
        @PrimaryKey var id: Long = 0,
        var word: String = ""
) : RealmObject()