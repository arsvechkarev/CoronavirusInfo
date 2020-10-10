package com.arsvechkarev.rankings.presentation

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsvechkarev.rankings.R
import com.arsvechkarev.rankings.di.RankingsDiInjector
import com.arsvechkarev.rankings.list.RankingsAdapter
import com.arsvechkarev.rankings.presentation.RankingsScreenState.Filtered
import com.arsvechkarev.rankings.presentation.RankingsScreenState.Loaded
import com.arsvechkarev.views.behaviors.BottomSheetBehavior.Companion.asBottomSheet
import com.arsvechkarev.views.behaviors.HeaderBehavior.Companion.asHeader
import com.arsvechkarev.views.drawables.BaseLoadingStub
import com.arsvechkarev.views.drawables.BaseLoadingStub.Companion.applyLoadingDrawable
import com.arsvechkarev.views.drawables.GradientHeaderStub.Companion.createGradientHeaderDrawable
import com.arsvechkarev.views.drawables.RankingsListLoadingStub
import com.arsvechkarev.views.drawables.SelectedChipsLoadingStub
import core.BaseFragment
import core.extenstions.animateInvisible
import core.extenstions.animateVisible
import core.extenstions.heightWithMargins
import core.extenstions.onClick
import core.hostActivity
import core.state.BaseScreenState
import core.state.Failure
import core.state.Failure.FailureReason
import core.state.Loading
import kotlinx.android.synthetic.main.fragment_rankings.chipAfrica
import kotlinx.android.synthetic.main.fragment_rankings.chipAsia
import kotlinx.android.synthetic.main.fragment_rankings.chipConfirmed
import kotlinx.android.synthetic.main.fragment_rankings.chipDeathRate
import kotlinx.android.synthetic.main.fragment_rankings.chipDeaths
import kotlinx.android.synthetic.main.fragment_rankings.chipEurope
import kotlinx.android.synthetic.main.fragment_rankings.chipNorthAmerica
import kotlinx.android.synthetic.main.fragment_rankings.chipOceania
import kotlinx.android.synthetic.main.fragment_rankings.chipPercentByCountry
import kotlinx.android.synthetic.main.fragment_rankings.chipRecovered
import kotlinx.android.synthetic.main.fragment_rankings.chipSouthAmerica
import kotlinx.android.synthetic.main.fragment_rankings.chipWorldwide
import kotlinx.android.synthetic.main.fragment_rankings.rankingsBottomSheet
import kotlinx.android.synthetic.main.fragment_rankings.rankingsBottomSheetCross
import kotlinx.android.synthetic.main.fragment_rankings.rankingsChipOptionType
import kotlinx.android.synthetic.main.fragment_rankings.rankingsChipWorldRegion
import kotlinx.android.synthetic.main.fragment_rankings.rankingsDivider
import kotlinx.android.synthetic.main.fragment_rankings.rankingsErrorLayout
import kotlinx.android.synthetic.main.fragment_rankings.rankingsErrorMessage
import kotlinx.android.synthetic.main.fragment_rankings.rankingsFabFilter
import kotlinx.android.synthetic.main.fragment_rankings.rankingsHeaderGradientView
import kotlinx.android.synthetic.main.fragment_rankings.rankingsHeaderLayout
import kotlinx.android.synthetic.main.fragment_rankings.rankingsIconDrawer
import kotlinx.android.synthetic.main.fragment_rankings.rankingsImageFailure
import kotlinx.android.synthetic.main.fragment_rankings.rankingsListLoadingStub
import kotlinx.android.synthetic.main.fragment_rankings.rankingsRecyclerView
import kotlinx.android.synthetic.main.fragment_rankings.rankingsRetryButton
import kotlinx.android.synthetic.main.fragment_rankings.rankingsSelectedChipsLoadingStub

class RankingsFragment : BaseFragment(R.layout.fragment_rankings) {
  
  private lateinit var chipHelper: ChipHelper
  private var viewModel: RankingsViewModel? = null
  private val adapter = RankingsAdapter()
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewModel = RankingsDiInjector.provideViewModel(this).also { model ->
      model.state.observe(this, Observer(::handleState))
      model.startLoadingData()
    }
    rankingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    rankingsRecyclerView.adapter = adapter
    setupClickListeners()
    setupBehavior()
    setupChips()
    setupDrawables()
  }
  
  override fun onNetworkAvailable() {
    val viewModel = viewModel ?: return
    val value = viewModel.state.value ?: return
    if (value !is Loaded && value !is Filtered) {
      viewModel.startLoadingData()
    }
  }
  
  override fun onDrawerOpened() = toggleItems(false)
  
  override fun onDrawerClosed() = toggleItems(true)
  
  private fun handleState(state: BaseScreenState) {
    when (state) {
      is Loading -> renderLoading()
      is Loaded -> renderLoaded(state)
      is Filtered -> renderFiltered(state)
      is Failure -> renderFailure(state.reason)
    }
  }
  
  private fun renderLoading() {
    rankingsFabFilter.isEnabled = false
    animateInvisible(rankingsErrorLayout, rankingsChipWorldRegion, rankingsChipOptionType,
      rankingsDivider, rankingsRecyclerView)
    (rankingsListLoadingStub.background as BaseLoadingStub).start()
    (rankingsSelectedChipsLoadingStub.background as BaseLoadingStub).start()
    animateVisible(rankingsListLoadingStub, rankingsSelectedChipsLoadingStub)
  }
  
  private fun renderLoaded(state: Loaded) {
    rankingsFabFilter.isEnabled = true
    stopLoadingStubs()
    animateVisible(rankingsRecyclerView, rankingsChipWorldRegion,
      rankingsChipOptionType, rankingsDivider)
    adapter.submitList(state.list)
  }
  
  private fun renderFiltered(state: Filtered) {
    rankingsFabFilter.isEnabled = true
    rankingsHeaderLayout.asHeader.animateScrollToTop(andThen = {
      adapter.submitList(state.list)
    })
  }
  
  private fun renderFailure(reason: FailureReason) {
    rankingsFabFilter.isEnabled = false
    stopLoadingStubs()
    rankingsErrorLayout.animateVisible()
    when (reason) {
      FailureReason.NO_CONNECTION -> {
        rankingsImageFailure.setImageResource(R.drawable.image_no_connection)
        rankingsErrorMessage.setText(R.string.text_no_connection)
      }
      FailureReason.TIMEOUT, FailureReason.UNKNOWN -> {
        rankingsImageFailure.setImageResource(R.drawable.image_unknown_error)
        rankingsErrorMessage.setText(R.string.text_unknown_error)
      }
    }
  }
  
  private fun toggleItems(enable: Boolean) {
    rankingsHeaderLayout.asHeader.isScrollable = enable
    rankingsRecyclerView.isEnabled = enable
    rankingsFabFilter.isEnabled = enable
  }
  
  private fun setupClickListeners() {
    rankingsIconDrawer.setOnClickListener { hostActivity.openDrawer() }
    rankingsRetryButton.setOnClickListener { viewModel!!.startLoadingData() }
    onClick(rankingsFabFilter, rankingsChipOptionType, rankingsChipWorldRegion, action = {
      rankingsBottomSheet.asBottomSheet.show()
      rankingsHeaderLayout.asHeader.isScrollable = false
      rankingsRecyclerView.isEnabled = false
      hostActivity.disableTouchesOnDrawer()
    })
    val whenBottomSheetClosed = {
      rankingsBottomSheet.asBottomSheet.hide()
      rankingsHeaderLayout.asHeader.isScrollable = true
      rankingsRecyclerView.isEnabled = true
      hostActivity.enableTouchesOnDrawer()
    }
    rankingsBottomSheetCross.setOnClickListener { whenBottomSheetClosed() }
    rankingsBottomSheet.asBottomSheet.onHide = { whenBottomSheetClosed() }
  }
  
  private fun stopLoadingStubs() {
    animateInvisible(rankingsListLoadingStub, rankingsSelectedChipsLoadingStub,
      andThen = {
        (rankingsListLoadingStub.background as BaseLoadingStub).stop()
        (rankingsSelectedChipsLoadingStub.background as BaseLoadingStub).stop()
      }
    )
  }
  
  private fun setupBehavior() {
    rankingsHeaderLayout.asHeader.apply {
      respondToHeaderTouches = false
      rankingsHeaderLayout.post {
        val height = calculateSelectedChipsHeight()
        slideRangeCoefficient = height.toFloat() / rankingsHeaderLayout.height
      }
    }
  }
  
  private fun calculateSelectedChipsHeight(): Int {
    return rankingsChipOptionType.heightWithMargins() + rankingsDivider.heightWithMargins()
  }
  
  private fun setupChips() {
    rankingsChipWorldRegion.isActive = true
    rankingsChipOptionType.isActive = true
    chipWorldwide.isActive = true
    chipConfirmed.isActive = true
    val groupWorldRegions = ChipGroup(chipWorldwide, chipEurope, chipAsia,
      chipAfrica, chipNorthAmerica, chipOceania, chipSouthAmerica)
    val groupOptionTypes = ChipGroup(chipConfirmed, chipRecovered, chipDeaths,
      chipPercentByCountry, chipDeathRate)
    chipHelper = ChipHelper(groupOptionTypes, groupWorldRegions,
      onNewChipSelected = { optionType, worldRegion ->
        viewModel!!.filter(optionType, worldRegion)
      },
      onOptionTypeChipSelected = { chip ->
        rankingsChipOptionType.text = chip.text
        rankingsChipOptionType.colorFill = chip.colorFill
      },
      onWorldRegionChipSelected = { chip ->
        rankingsChipWorldRegion.text = chip.text
      })
  }
  
  private fun setupDrawables() {
    rankingsHeaderGradientView.background =
        createGradientHeaderDrawable(curveSizeRes = R.dimen.rankings_header_curve_size)
    rankingsListLoadingStub.applyLoadingDrawable(RankingsListLoadingStub(requireContext()))
    rankingsSelectedChipsLoadingStub.applyLoadingDrawable(
      SelectedChipsLoadingStub(
        requireContext(), R.dimen.rankings_chip_text_size, R.dimen.rankings_header_chip_margin
      )
    )
  }
}