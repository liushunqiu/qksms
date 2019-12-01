package com.moez.QKSMS.repository

import com.moez.QKSMS.model.Keywords
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject

/**
 * 钉钉
 */
class ForwardDingDingRepositoryImpl @Inject constructor() : ForwardDingDingRepository {
    override fun keywords(vararg word: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val keywords = realm.where(Keywords::class.java).findAll()
            val newKeywords = word.filter { address ->
                keywords.none { word -> word.equals(address) }
            }

            val maxId = realm.where(Keywords::class.java)
                    .max("id")?.toLong() ?: -1

            realm.executeTransaction {
                realm.insert(newKeywords.mapIndexed { index, word ->
                    Keywords(maxId + 1 + index, word)
                })
            }
        }
    }

    override fun unKeywords(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Keywords::class.java)
                        .equalTo("id", id)
                        .findAll()
                        .deleteAllFromRealm()
            }
        }
    }

    override fun getKeywords(id: Long): Keywords? {
        return Realm.getDefaultInstance()
                .where(Keywords::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    override fun getKeyWords(): RealmResults<Keywords> {
        return Realm.getDefaultInstance()
                .where(Keywords::class.java)
                .findAllAsync()
    }

}