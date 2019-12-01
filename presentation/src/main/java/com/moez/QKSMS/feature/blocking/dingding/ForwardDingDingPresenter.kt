package com.moez.QKSMS.feature.blocking.dingding

import com.moez.QKSMS.common.base.QkPresenter
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

/**
 * 钉钉
 */
class ForwardDingDingPresenter @Inject constructor(
        private val prefs: Preferences
) : QkPresenter<ForwardDingDingView, ForwardDingDingState>(ForwardDingDingState(
        switch = prefs.switch.get(),
        token = prefs.token.get()
)) {
    init {
        disposables += prefs.switch.asObservable()
                .subscribe { s -> newState { copy(switch = s) } }
        disposables += prefs.token.asObservable()
                .subscribe { t -> newState { copy(token = t) } }
    }

    override fun bindIntents(view: ForwardDingDingView) {
        super.bindIntents(view)
        view.tokenClickedIntent
                .autoDisposable(view.scope())
                .subscribe { view.showTokenDialog(prefs.token.get()) }
        view.switchClickedIntent
                .autoDisposable(view.scope())
                .subscribe { prefs.switch.set(!prefs.switch.get()) }

        view.filterKeywordsIntent
                .autoDisposable(view.scope())
                .subscribe { view.openFilterKeywords() }

        view.tokenSet()
                .doOnNext(prefs.token::set)
                .autoDisposable(view.scope())
                .subscribe()
    }
}