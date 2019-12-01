package com.moez.QKSMS.feature.blocking.dingding.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.model.Keywords
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_number_list_item.view.*
import kotlinx.android.synthetic.main.filtering_keywords_list_item.*
import kotlinx.android.synthetic.main.filtering_keywords_list_item.view.*


class FilterKeywordsAdapter : QkRealmAdapter<Keywords>(){

    val unfilterKeywords: Subject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filtering_keywords_list_item, parent, false)
        return QkViewHolder(view).apply {
            containerView.unfilter.setOnClickListener {
                val keywords = getItem(adapterPosition) ?: return@setOnClickListener
                unfilterKeywords.onNext(keywords.id)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.keywords.text = item.word
    }

}