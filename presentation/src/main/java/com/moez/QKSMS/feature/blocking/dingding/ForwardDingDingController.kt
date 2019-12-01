package com.moez.QKSMS.feature.blocking.dingding

import android.app.Activity
import android.content.Context
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkChangeHandler
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.widget.FieldDialog
import com.moez.QKSMS.feature.blocking.dingding.filter.FilterKeywordsController
import com.moez.QKSMS.feature.blocking.numbers.BlockedNumbersController
import com.moez.QKSMS.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.forward_dingding_controller.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

/**
 * 钉钉
 */
class ForwardDingDingController : QkController<ForwardDingDingView, ForwardDingDingState, ForwardDingDingPresenter>(),
        ForwardDingDingView {

    @Inject
    override lateinit var presenter: ForwardDingDingPresenter

    @Inject
    lateinit var context: Context

    private val activityResumedSubject: PublishSubject<Unit> = PublishSubject.create()

    private val tokenSubject: Subject<String> = PublishSubject.create()

    private val tokenDialog: FieldDialog by lazy {
        FieldDialog(activity!!, "机器人Token", tokenSubject::onNext)
    }

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.forward_dingding_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle("转发至钉钉")
        showBackButton(true)
    }

    override fun onActivityResumed(activity: Activity) {
        activityResumedSubject.onNext(Unit)
    }

    override fun render(state: ForwardDingDingState) {
        switchDingDing.checkbox.isChecked = state.switch

        token.summary = state.token.takeIf { it.isNotBlank() }
                ?: "请配置钉钉机器人Token"
    }

    override val switchClickedIntent by lazy { switchDingDing.clicks() }

    override val filterKeywordsIntent by lazy { filterKeyWords.clicks() }

    override val tokenClickedIntent by lazy { token.clicks() }

    override fun showTokenDialog(token: String) = tokenDialog.setText(token).show()

    override fun tokenSet(): Observable<String> = tokenSubject

    override fun openFilterKeywords() {
        router.pushController(RouterTransaction.with(FilterKeywordsController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }


}