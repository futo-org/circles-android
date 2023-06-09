package org.futo.circles.feature.timeline.post.report

import androidx.lifecycle.ViewModel
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.extensions.Response
import org.futo.circles.extensions.launchBg

class ReportViewModel(
    private val reportDataSource: ReportDataSource
) : ViewModel() {

    val reportLiveData = SingleEventLiveData<Response<Unit?>>()
    val reportCategoriesLiveData = reportDataSource.reportCategoriesLiveData

    fun report(score: Int) {
        launchBg { reportLiveData.postValue(reportDataSource.report(score)) }
    }

    fun toggleReportCategory(categoryId: Int) {
        reportDataSource.toggleReportCategory(categoryId)
    }
}