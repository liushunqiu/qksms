package com.moez.QKSMS.feature.blocking.dingding.filter

import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.interactor.MarkUnblocked
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.ForwardDingDingRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilterKeywordsPresenter @Inject constructor(
        private val forwardDingDingRepository: ForwardDingDingRepository,
        private val conversationRepo: ConversationRepository,
        private val markUnblocked: MarkUnblocked
) : QkPresenter<FilterKeywordsView, FilterKeywordsState>(
        FilterKeywordsState(keywords = forwardDingDingRepository.getKeyWords())
) {
    override fun bindIntents(view: FilterKeywordsView) {
        super.bindIntents(view)

        view.unfilterKeywords()
                .doOnNext { id ->
                    forwardDingDingRepository.getKeywords(id)?.word
                            ?.let(conversationRepo::getThreadId)
                            ?.let { threadId -> markUnblocked.execute(listOf(threadId)) }
                }
                .doOnNext(forwardDingDingRepository::unKeywords)
                .subscribeOn(Schedulers.io())
                .autoDisposable(view.scope())
                .subscribe()

        view.addKeywords()
                .autoDisposable(view.scope())
                .subscribe { view.showAddKeyWordsDialog() }

        view.saveKeywords()
                .subscribeOn(Schedulers.io())
                .autoDisposable(view.scope())
                .subscribe { word -> forwardDingDingRepository.keywords(word) }
    }

}