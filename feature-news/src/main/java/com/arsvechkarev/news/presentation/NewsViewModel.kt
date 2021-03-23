package com.arsvechkarev.news.presentation

import base.RxViewModel
import base.extensions.startWithIf
import base.extensions.withNetworkDelay
import base.extensions.withRequestTimeout
import base.extensions.withRetry
import com.arsvechkarev.news.domain.NewYorkTimesNewsRepository
import core.BaseScreenState
import core.Failure
import core.Loading
import core.NetworkAvailabilityNotifier
import core.NetworkListener
import core.Schedulers

class NewsViewModel(
  private val newsRepository: NewYorkTimesNewsRepository,
  private val networkAvailabilityNotifier: NetworkAvailabilityNotifier,
  private val schedulers: Schedulers
) : RxViewModel(), NetworkListener {
  
  private var currentPage = 0
  
  init {
    networkAvailabilityNotifier.registerListener(this)
  }
  
  override fun onNetworkAvailable() {
    if (_state.value is FailureLoadingNextPage) {
      schedulers.mainThread().scheduleDirect(::tryLoadNextPage)
    } else if (_state.value is Failure) {
      schedulers.mainThread().scheduleDirect(::startLoadingData)
    }
  }
  
  override fun isItemLoading(item: Any?): Boolean {
    return item is Loading || item is LoadingNextPage
  }
  
  fun startLoadingData() {
    rxCall {
      newsRepository.requestLatestNews(currentPage)
          .toObservable()
          .subscribeOn(schedulers.io())
          .withNetworkDelay(schedulers)
          .withRetry()
          .withRequestTimeout()
          .map<BaseScreenState>(::LoadedNews)
          .startWith(Loading)
          .onErrorReturn(::Failure)
          .observeOn(schedulers.mainThread())
          .smartSubscribe(_state::setValue)
    }
  }
  
  fun tryLoadNextPage() {
    rxCall {
      newsRepository.requestLatestNews(++currentPage)
          .toObservable()
          .subscribeOn(schedulers.io())
          .withNetworkDelay(schedulers)
          .withRetry()
          .withRequestTimeout()
          .map<BaseScreenState>(::LoadedNextPage)
          .observeOn(schedulers.mainThread())
          .startWithIf(LoadingNextPage) { currentPage < newsRepository.maxPagesCount }
          .doOnError { currentPage-- }
          .onErrorReturn(::FailureLoadingNextPage)
          .smartSubscribe(_state::setValue)
    }
  }
  
  override fun onCleared() {
    networkAvailabilityNotifier.unregisterListener(this)
  }
}