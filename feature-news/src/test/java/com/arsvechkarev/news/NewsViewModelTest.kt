package com.arsvechkarev.news

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arsvechkarev.news.presentation.FailureLoadingNextPage
import com.arsvechkarev.news.presentation.LoadedNewNews
import com.arsvechkarev.news.presentation.LoadedNews
import com.arsvechkarev.news.presentation.LoadingNextPage
import com.arsvechkarev.news.presentation.NewsViewModel
import com.arsvechkarev.test.FakeSchedulers
import com.arsvechkarev.test.FakeScreenStateObserver
import com.arsvechkarev.test.currentState
import com.arsvechkarev.test.hasCurrentState
import com.arsvechkarev.test.hasStateAtPosition
import com.arsvechkarev.test.hasStatesCount
import com.arsvechkarev.test.state
import config.RxConfigurator
import core.Failure
import core.FailureReason.NO_CONNECTION
import core.FailureReason.UNKNOWN
import core.Loading
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.UnknownHostException

class NewsViewModelTest {
  
  @Rule
  @JvmField
  val instantExecutorRule = InstantTaskExecutorRule()
  
  private val maxRetryCount = 3
  
  @Before
  fun setUp() {
    RxConfigurator.configureNetworkDelay(0)
    RxConfigurator.configureRetryCount(maxRetryCount.toLong())
  }
  
  @Test
  fun `Basic flow`() {
    val viewModel = createViewModel()
    val observer = createObserver()
    
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    
    with(observer) {
      hasStatesCount(2)
      hasStateAtPosition<Loading>(0)
      hasStateAtPosition<LoadedNews>(1)
      hasCurrentState<LoadedNews>()
      val news = state<LoadedNews>(1).news
      assertArrayEquals(FakeNewsListPages[0].toTypedArray(), news.toTypedArray())
    }
  }
  
  @Test
  fun `Error handling`() {
    val viewModel = createViewModel(
      totalRetryCount = maxRetryCount + 1,
      error = Throwable()
    )
    val observer = createObserver()
    
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    
    with(observer) {
      hasStatesCount(2)
      hasStateAtPosition<Loading>(0)
      assertEquals(UNKNOWN, currentState<Failure>().reason)
    }
  }
  
  @Test
  fun `Testing retry`() {
    val viewModel = createViewModel(
      totalRetryCount = maxRetryCount + 1,
      error = UnknownHostException()
    )
    val observer = createObserver()
  
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    viewModel.retryLoadingData()
  
    with(observer) {
      hasStatesCount(4)
      hasStateAtPosition<Loading>(0)
      hasStateAtPosition<Failure>(1)
      hasStateAtPosition<Loading>(2)
      hasStateAtPosition<LoadedNews>(3)
      assertEquals(NO_CONNECTION, state<Failure>(1).reason)
      val news = state<LoadedNews>(3).news
      assertArrayEquals(FakeNewsListPages[0].toTypedArray(), news.toTypedArray())
    }
  }
  
  @Test
  fun `Testing network availability callback`() {
    val viewModel = createViewModel(
      totalRetryCount = maxRetryCount + 1,
      error = UnknownHostException(),
    )
    val observer = createObserver()
    
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    viewModel.onNetworkAvailable()
    
    with(observer) {
      hasStatesCount(4)
      hasStateAtPosition<Loading>(0)
      hasStateAtPosition<Failure>(1)
      hasStateAtPosition<Loading>(2)
      assertEquals(NO_CONNECTION, state<Failure>(1).reason)
      val news = state<LoadedNews>(3).news
      assertArrayEquals(FakeNewsListPages[0].toTypedArray(), news.toTypedArray())
    }
  }
  
  @Test
  fun `Testing paging`() {
    val viewModel = createViewModel()
    val observer = createObserver()
    
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    viewModel.tryLoadNextPage()
    
    with(observer) {
      hasStatesCount(4)
      hasStateAtPosition<Loading>(0)
      hasStateAtPosition<LoadedNews>(1)
      hasStateAtPosition<LoadingNextPage>(2)
      hasStateAtPosition<LoadedNewNews>(3)
  
      val news = state<LoadedNews>(1).news
      assertArrayEquals(FakeNewsListPages[0].toTypedArray(), news.toTypedArray())
      val newNews = state<LoadedNewNews>(3).news
      val newList = FakeNewsListPages[0] + FakeNewsListPages[1]
      assertArrayEquals(newList.toTypedArray(), newNews.toTypedArray())
    }
  }
  
  @Test
  fun `Testing paging errors`() {
    val fakeNewYorkTimesUseCase = FakeNewsUseCase()
    val viewModel = NewsViewModel(fakeNewYorkTimesUseCase, FakeSchedulers)
    val observer = createObserver()
  
    viewModel.state.observeForever(observer)
    viewModel.startLoadingData()
    fakeNewYorkTimesUseCase.setNextCallAsError(UnknownHostException())
    RxConfigurator.configureRetryCount(0)
    viewModel.tryLoadNextPage()
  
    with(observer) {
      hasStatesCount(4)
      hasStateAtPosition<Loading>(0)
      hasStateAtPosition<LoadedNews>(1)
      hasStateAtPosition<LoadingNextPage>(2)
      hasStateAtPosition<FailureLoadingNextPage>(3)
      val news = state<LoadedNews>(1).news
      assertArrayEquals(FakeNewsListPages[0].toTypedArray(), news.toTypedArray())
      assertEquals(NO_CONNECTION, currentState<FailureLoadingNextPage>().reason)
    }
  }
  
  private fun createViewModel(
    totalRetryCount: Int = 0,
    error: Throwable = Throwable(),
    newsDataSource: FakeNewsUseCase = FakeNewsUseCase(
      totalRetryCount = totalRetryCount,
      errorFactory = { error }
    )
  ): NewsViewModel {
    return NewsViewModel(
      newsDataSource,
      FakeSchedulers
    )
  }
  
  private fun createObserver(): FakeScreenStateObserver {
    return FakeScreenStateObserver()
  }
}