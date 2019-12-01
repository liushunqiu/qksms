package com.moez.QKSMS.feature.blocking.dingding.filter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.filter_keywords_add_dialog.view.*
import kotlinx.android.synthetic.main.filter_keywords_controller.*
import javax.inject.Inject

class FilterKeywordsController : QkController<FilterKeywordsView, FilterKeywordsState, FilterKeywordsPresenter>(),
        FilterKeywordsView {
    @Inject
    override lateinit var presenter: FilterKeywordsPresenter
    @Inject
    lateinit var colors: Colors

    private val adapter = FilterKeywordsAdapter()
    private val saveKeywordsSubject: Subject<String> = PublishSubject.create()

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.filter_keywords_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle("拦截关键词")
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()
        keywords_add.setBackgroundTint(colors.theme().theme)
        keywords_add.setTint(colors.theme().textPrimary)
        adapter.emptyView = keywords_empty
        keywords.adapter = adapter
    }

    override fun render(state: FilterKeywordsState) {
        adapter.updateData(state.keywords)
    }

    override fun unfilterKeywords(): Observable<Long>  = adapter.unfilterKeywords

    override fun addKeywords(): Observable<*> = keywords_add.clicks()

    override fun saveKeywords(): Observable<String> = saveKeywordsSubject

    override fun showAddKeyWordsDialog() {
        val layout = LayoutInflater.from(activity).inflate(R.layout.filter_keywords_add_dialog, null)
        val textWatcher = KeywordsTextWatcher(layout.keywords_input)
        val dialog = AlertDialog.Builder(activity!!)
                .setView(layout)
                .setPositiveButton("拦截") { _, _ ->
                    saveKeywordsSubject.onNext(layout.keywords_input.text.toString())
                }
                .setNegativeButton(R.string.button_cancel) { _, _ -> }
                .setOnDismissListener { textWatcher.dispose() }
        dialog.show()
    }
}