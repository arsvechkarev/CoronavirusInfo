package com.arsvechkarev.rankings.presentation

import api.recycler.DifferentiableItem
import core.BaseScreenState
import core.model.OptionType
import core.model.WorldRegion
import core.model.ui.CountryFullInfo

class Success(
  val countries: List<DifferentiableItem>,
  val isListChanged: Boolean,
  val worldRegion: WorldRegion,
  val optionType: OptionType,
  val showFilterDialog: Boolean,
  val countryFullInfo: CountryFullInfo?
) : BaseScreenState()