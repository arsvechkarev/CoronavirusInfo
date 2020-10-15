package com.arsvechkarev.stats.presentation

import com.arsvechkarev.common.GeneralInfoRepository
import com.arsvechkarev.common.WorldCasesInfoRepository
import core.BaseScreenState
import core.Failure
import core.Loading
import core.MIN_NETWORK_DELAY
import core.RxViewModel
import core.concurrency.AndroidSchedulers
import core.concurrency.Schedulers
import core.extenstions.assertThat
import core.model.DailyCase
import core.model.WorldCasesInfo
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class StatsViewModel(
  private val generalInfoRepository: GeneralInfoRepository,
  private val worldCasesInfoRepository: WorldCasesInfoRepository,
  private val schedulers: Schedulers = AndroidSchedulers,
  private val delay: Long = MIN_NETWORK_DELAY
) : RxViewModel() {
  
  fun startLoadingData() {
    rxCall {
      Observable.zip(
        generalInfoRepository.getGeneralInfo()
            .subscribeOn(schedulers.io()),
        worldCasesInfoRepository.getWorldDailyTotalCases()
            .map { totalCases -> Pair(totalCases, totalCases.toNewDailyCases()) }
            .subscribeOn(schedulers.io()),
        { info, cases -> WorldCasesInfo(info, cases.first, cases.second) }
      )
          .subscribeOn(schedulers.io())
          .delay(delay, TimeUnit.MILLISECONDS, schedulers.computation(), true)
          .map(::mapToWorldCasesInfo)
          .onErrorReturn(::Failure)
          .startWith(Loading())
          .observeOn(schedulers.mainThread())
          .smartSubscribe(_state::setValue)
    }
  }
  
  private fun mapToWorldCasesInfo(it: WorldCasesInfo): BaseScreenState = LoadedWorldCasesInfo(it)
  
  private fun List<DailyCase>.toNewDailyCases(): List<DailyCase> {
    val dailyCases = ArrayList<DailyCase>()
    dailyCases.add(DailyCase(this[0].cases / 2, this[0].date))
    for (i in 1 until this.size) {
      val curr = this[i]
      val prev = this[i - 1]
      val diff = curr.cases - prev.cases
      dailyCases.add(DailyCase(diff, curr.date))
    }
    assertThat(this.size == dailyCases.size)
    return dailyCases
  }
}
