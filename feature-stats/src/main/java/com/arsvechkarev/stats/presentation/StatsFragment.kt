package com.arsvechkarev.stats.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import base.BaseFragment
import base.behaviors.ScrollableContentBehavior
import base.behaviors.TitleHeaderBehavior
import base.drawables.BaseLoadingStub.Companion.setLoadingDrawable
import base.drawables.MainStatsInfoLoadingStub
import base.drawables.StatsGraphLoadingStub
import base.extensions.getMessageRes
import base.hostActivity
import base.views.CustomNestedScrollView
import base.views.charts.DailyCasesChart.Type.NEW_CASES
import base.views.charts.DailyCasesChart.Type.TOTAL_CASES
import com.arsvechkarev.stats.R
import com.arsvechkarev.stats.di.StatsComponent
import com.arsvechkarev.viewdsl.animateChildrenInvisible
import com.arsvechkarev.viewdsl.animateChildrenVisible
import com.arsvechkarev.viewdsl.animateInvisible
import com.arsvechkarev.viewdsl.animateVisible
import com.arsvechkarev.viewdsl.behavior
import com.arsvechkarev.viewdsl.getBehavior
import com.arsvechkarev.viewdsl.gone
import com.arsvechkarev.viewdsl.paddings
import com.arsvechkarev.viewdsl.statusBarHeight
import com.arsvechkarev.viewdsl.visible
import core.BaseScreenState
import core.Failure
import core.FailureReason.NO_CONNECTION
import core.FailureReason.TIMEOUT
import core.FailureReason.UNKNOWN
import core.Loading
import core.model.data.GeneralInfo
import core.model.data.MainStatistics
import kotlinx.android.synthetic.main.fragment_stats.statsContentView
import kotlinx.android.synthetic.main.fragment_stats.statsErrorLayout
import kotlinx.android.synthetic.main.fragment_stats.statsErrorMessage
import kotlinx.android.synthetic.main.fragment_stats.statsGeneralStatsView
import kotlinx.android.synthetic.main.fragment_stats.statsHeader
import kotlinx.android.synthetic.main.fragment_stats.statsIconDrawer
import kotlinx.android.synthetic.main.fragment_stats.statsImageFailure
import kotlinx.android.synthetic.main.fragment_stats.statsMainInfoLoadingStub
import kotlinx.android.synthetic.main.fragment_stats.statsNewCasesChart
import kotlinx.android.synthetic.main.fragment_stats.statsNewCasesLabel
import kotlinx.android.synthetic.main.fragment_stats.statsNewCasesLoadingStub
import kotlinx.android.synthetic.main.fragment_stats.statsRetryButton
import kotlinx.android.synthetic.main.fragment_stats.statsScrollingContentView
import kotlinx.android.synthetic.main.fragment_stats.statsTotalCasesChart
import kotlinx.android.synthetic.main.fragment_stats.statsTotalCasesLabel
import kotlinx.android.synthetic.main.fragment_stats.statsTotalCasesLoadingStub
import timber.log.Timber

class StatsFragment : BaseFragment(R.layout.fragment_stats) {
  
  private lateinit var viewModel: StatsViewModel
  
  override fun onInit() {
    Timber.d("Logggging StatsFragment onInit")
    viewModel = StatsComponent.provideViewModel(this).also { model ->
      model.state.observe(this, Observer(::handleState))
      model.startLoadingData()
    }
    statsHeader.paddings(top = requireContext().statusBarHeight)
    initViews()
    initClickListeners()
    hostActivity.enableTouchesOnDrawer()
  }
  
  override fun onHiddenChanged(hidden: Boolean) {
    Timber.d("Logggging StatsFragment onHiddenChanged, isHidden=$hidden")
    if (hidden) hostActivity.enableTouchesOnDrawer()
  }
  
  override fun onDrawerOpened() = toggleScrollingContent(false)
  
  override fun onDrawerClosed() {
    val state = viewModel.state.value
    if (state is Loading || state is Failure) {
      toggleScrollingContent(false)
    } else {
      toggleScrollingContent(true)
    }
  }
  
  override fun onOrientationBecameLandscape() {
    statsImageFailure.gone()
  }
  
  override fun onOrientationBecamePortrait() {
    statsImageFailure.visible()
  }
  
  override fun onDestroyView() {
    Timber.d("Logggging StatsFragment onDestroyView")
    super.onDestroyView()
    (statsMainInfoLoadingStub.background as? MainStatsInfoLoadingStub)?.release()
  }
  
  private fun handleState(state: BaseScreenState) {
    when (state) {
      is Loading -> {
        updateContentView(putLoading = true)
      }
      is LoadedMainStatistics -> {
        hostActivity.enableTouchesOnDrawer()
        toggleScrollingContent(enable = true)
        renderGeneralInfo(state.mainStatistics.generalInfo)
        renderCharts(state.mainStatistics)
      }
      is Failure -> {
        renderFailure(state)
      }
    }
  }
  
  private fun renderCharts(mainStatistics: MainStatistics) {
    statsTotalCasesChart.update(mainStatistics.worldCasesInfo.totalDailyCases)
    statsNewCasesChart.update(mainStatistics.worldCasesInfo.newDailyCases)
    statsTotalCasesChart.animateVisible(andThen = { statsTotalCasesLoadingStub.background = null })
    statsNewCasesChart.animateVisible(andThen = { statsNewCasesLoadingStub.background = null })
    statsTotalCasesLabel.animateVisible()
    statsNewCasesLabel.animateVisible()
  }
  
  private fun renderGeneralInfo(generalInfo: GeneralInfo) {
    statsGeneralStatsView.updateNumbers(generalInfo)
    statsGeneralStatsView.animateVisible(andThen = { statsMainInfoLoadingStub.background = null })
  }
  
  private fun renderFailure(failure: Failure) {
    val reason = failure.reason
    Timber.w(failure.throwable)
    hostActivity.enableTouchesOnDrawer()
    updateContentView(putLoading = false)
    statsErrorMessage.setText(reason.getMessageRes())
    when (reason) {
      TIMEOUT, UNKNOWN -> statsImageFailure.setImageResource(R.drawable.image_unknown_error)
      NO_CONNECTION -> statsImageFailure.setImageResource(R.drawable.image_no_connection)
    }
  }
  
  private fun updateContentView(putLoading: Boolean) {
    toggleScrollingContent(enable = false)
    if (putLoading) {
      statsErrorLayout.animateInvisible()
      statsContentView.animateChildrenVisible()
    } else {
      statsErrorLayout.animateVisible()
      statsContentView.animateChildrenInvisible()
    }
  }
  
  private fun toggleScrollingContent(enable: Boolean) {
    val behavior = statsScrollingContentView.getBehavior<ScrollableContentBehavior<*>>()
    behavior.respondToTouches = enable
    statsScrollingContentView.isEnabled = enable
    statsNewCasesChart.isEnabled = enable
    statsTotalCasesChart.isEnabled = enable
  }
  
  private fun initClickListeners() {
    val onDown = { hostActivity.disableTouchesOnDrawer() }
    val onUp = { hostActivity.enableTouchesOnDrawer() }
    statsTotalCasesChart.onDown = onDown
    statsNewCasesChart.onDown = onDown
    statsTotalCasesChart.onUp = onUp
    statsNewCasesChart.onUp = onUp
    statsIconDrawer.setOnClickListener { hostActivity.openDrawer() }
    statsTotalCasesChart.onDailyCaseClicked { statsTotalCasesLabel.drawCase(it) }
    statsNewCasesChart.onDailyCaseClicked { statsNewCasesLabel.drawCase(it) }
    statsRetryButton.setOnClickListener { viewModel.retryLoadingData() }
  }
  
  private fun initViews() {
    statsTotalCasesChart.type = TOTAL_CASES
    statsNewCasesChart.type = NEW_CASES
    statsHeader.behavior(TitleHeaderBehavior { it.id == R.id.statsScrollingContentView })
    statsScrollingContentView.behavior(
      ScrollableContentBehavior<CustomNestedScrollView>(requireContext()))
    statsMainInfoLoadingStub.setLoadingDrawable(MainStatsInfoLoadingStub(requireContext()))
    statsTotalCasesLoadingStub.setLoadingDrawable(StatsGraphLoadingStub(requireContext()))
    statsNewCasesLoadingStub.setLoadingDrawable(StatsGraphLoadingStub(requireContext()))
  }
  
  override fun onAttach(context: Context) {
    super.onAttach(context)
    Timber.d("Logggging StatsFragment onAttach")
  }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Timber.d("Logggging StatsFragment onCreate")
  }
  
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    Timber.d("Logggging StatsFragment onCreateView")
    return super.onCreateView(inflater, container, savedInstanceState)
  }
  
  override fun onStart() {
    super.onStart()
    Timber.d("Logggging StatsFragment onStart")
  }
  
  override fun onResume() {
    super.onResume()
    Timber.d("Logggging StatsFragment onResume")
  }
  
  override fun onPause() {
    super.onPause()
    Timber.d("Logggging StatsFragment onPause")
  }
  
  override fun onStop() {
    super.onStop()
    Timber.d("Logggging StatsFragment onStop")
  }
  
  override fun onDestroy() {
    super.onDestroy()
    Timber.d("Logggging StatsFragment onDestroy")
  }
  
  override fun onDetach() {
    super.onDetach()
    Timber.d("Logggging StatsFragment onDetach")
  }
}