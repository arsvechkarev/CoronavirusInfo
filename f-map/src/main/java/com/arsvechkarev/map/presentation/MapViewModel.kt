package com.arsvechkarev.map.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arsvechkarev.common.AllCountriesRepository
import com.arsvechkarev.common.CountriesMetaInfoRepository
import com.arsvechkarev.map.presentation.MapScreenState.FoundCountry
import com.arsvechkarev.map.presentation.MapScreenState.Loaded
import core.RxViewModel
import core.concurrency.AndroidSchedulers
import core.concurrency.AndroidThreader
import core.concurrency.Schedulers
import core.concurrency.Threader
import core.model.Country
import core.model.Location
import core.model.TotalData
import core.state.BaseScreenState
import core.state.Failure
import core.state.Failure.Companion.asFailureReason
import core.state.Loading
import io.reactivex.Observable

class MapViewModel(
  private val allCountriesRepository: AllCountriesRepository,
  private val countriesMetaInfoRepository: CountriesMetaInfoRepository,
  private val threader: Threader = AndroidThreader,
  private val schedulers: Schedulers = AndroidSchedulers
) : RxViewModel() {
  
  private val _state = MutableLiveData<BaseScreenState>()
  val state: LiveData<BaseScreenState>
    get() = _state
  
  fun startLoadingData() {
    rxCall {
      Observable.zip(
        allCountriesRepository.getData().subscribeOn(schedulers.io()),
        countriesMetaInfoRepository.getLocationsMap().subscribeOn(schedulers.io()),
        { map, countries -> Pair(map, countries) }
      ).observeOn(schedulers.mainThread())
          .map(::transformResult)
          .onErrorReturn { Failure(it.asFailureReason()) }
          .startWith(Loading)
          .subscribe(_state::setValue)
    }
  }
  
  fun showCountryInfo(country: Country) {
    when (val state = _state.value) {
      is Loaded -> notifyFoundCountry(state.countries, state.iso2ToLocations, country)
      is FoundCountry -> notifyFoundCountry(state.countries, state.iso2ToLocations, country)
    }
  }
  
  private fun transformResult(pair: Pair<TotalData, Map<String, Location>>): BaseScreenState =
      Loaded(pair.first.countries, pair.second)
  
  private fun notifyFoundCountry(
    countries: List<Country>,
    iso2ToLocations: Map<String, Location>,
    foundCountry: Country
  ) {
    threader.onMainThread { _state.value = FoundCountry(countries, iso2ToLocations, foundCountry) }
  }
}