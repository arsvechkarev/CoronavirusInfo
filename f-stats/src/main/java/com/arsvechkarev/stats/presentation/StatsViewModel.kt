package com.arsvechkarev.stats.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arsvechkarev.common.repositories.CountriesInfoInteractor
import com.arsvechkarev.common.repositories.GeneralInfoExecutor
import com.arsvechkarev.network.AsyncOperations
import com.arsvechkarev.stats.list.InfoType
import com.arsvechkarev.stats.list.InfoType.CONFIRMED
import com.arsvechkarev.stats.list.InfoType.DEATHS
import com.arsvechkarev.stats.list.InfoType.RECOVERED
import com.arsvechkarev.stats.presentation.StatsScreenState.Failure
import com.arsvechkarev.stats.presentation.StatsScreenState.Failure.FailureReason.TIMEOUT
import com.arsvechkarev.stats.presentation.StatsScreenState.GeneralInfoLoaded
import com.arsvechkarev.stats.presentation.StatsScreenState.LoadedAll
import com.arsvechkarev.stats.presentation.StatsScreenState.LoadingCountriesInfo
import com.arsvechkarev.stats.presentation.StatsScreenState.LoadingGeneralInfo
import core.ApplicationConfig
import core.Colors
import core.NetworkConnection
import core.StateHandle
import core.addOrUpdate
import core.model.Country
import core.model.DisplayableCountry
import core.model.GeneralInfo
import java.util.concurrent.TimeoutException

class StatsViewModel(
  private val connection: NetworkConnection,
  private val threader: ApplicationConfig.Threader,
  private val countriesInfoInteractor: CountriesInfoInteractor,
  private val generalInfoExecutor: GeneralInfoExecutor
) : ViewModel() {
  
  private val asyncOperations = AsyncOperations(2)
  
  private val _state = MutableLiveData<StateHandle<StatsScreenState>>(StateHandle())
  val state: LiveData<StateHandle<StatsScreenState>>
    get() = _state
  
  fun loadData() {
    _state.addOrUpdate(LoadingGeneralInfo)
    _state.addOrUpdate(LoadingCountriesInfo)
    generalInfoExecutor.getGeneralInfo(onSuccess = {
      asyncOperations.addValue("generalInfo", it)
      _state.addOrUpdate(GeneralInfoLoaded(it))
    }, onFailure = {
    
    })
    countriesInfoInteractor.updateCountriesInfo(onSuccess = {
      asyncOperations.addValue("countries", it)
    }, onFailure = {
      if (it is TimeoutException) {
        _state.addOrUpdate(Failure(TIMEOUT))
      }
    })
    asyncOperations.onDoneAll {
      threader.backgroundWorker.submit {
        val generalInfo = it["generalInfo"] as GeneralInfo
        val countries = it["countries"] as List<Country>
        val displayableCountries = countries.toDisplayableCountries(generalInfo, CONFIRMED)
        threader.mainThreadWorker.submit {
          _state.addOrUpdate(LoadedAll(CONFIRMED, generalInfo, displayableCountries))
        }
      }
    }
  }
  
  override fun onCleared() {
    countriesInfoInteractor.removeListener()
  }
  
  private fun List<Country>.toDisplayableCountries(
    generalInfo: GeneralInfo,
    type: InfoType
  ): List<DisplayableCountry> {
    val countries = ArrayList<DisplayableCountry>()
    val color = determineColor(type)
    forEach {
      val number = determineNumber(type, it)
      val percent = determinePercent(type, it, generalInfo)
      countries.add(DisplayableCountry(it.countryName, number, percent, color))
    }
    return countries
  }
  
  private fun determineNumber(infoType: InfoType, country: Country) = when (infoType) {
    CONFIRMED -> country.confirmed.toInt()
    DEATHS -> country.deaths.toInt()
    RECOVERED -> country.recovered.toInt()
  }
  
  private fun determinePercent(infoType: InfoType, country: Country, generalInfo: GeneralInfo): Float {
    return when (infoType) {
      CONFIRMED -> generalInfo.confirmed.toFloat() / country.confirmed.toFloat()
      DEATHS -> generalInfo.deaths.toFloat() / country.deaths.toFloat()
      RECOVERED -> generalInfo.recovered.toFloat() / country.recovered.toFloat()
    }
  }
  
  private fun determineColor(infoType: InfoType) = when (infoType) {
    CONFIRMED -> Colors.confirmedColor
    DEATHS -> Colors.deathsColor
    RECOVERED -> Colors.recoveredColor
  }
  
}
