package com.arsvechkarev.common

import com.arsvechkarev.storage.Saver
import core.Loggable
import core.MAX_CACHE_MINUTES
import core.RxNetworker
import core.concurrency.AndroidSchedulers
import core.concurrency.Schedulers
import core.log
import core.model.Country
import core.model.GeneralInfo
import core.model.TotalData
import io.reactivex.Observable
import org.json.JSONObject

class AllCountriesRepository(
  private val networker: RxNetworker,
  private val saver: Saver,
  private val sqLiteExecutor: CountriesSQLiteExecutor,
  private val schedulers: Schedulers = AndroidSchedulers
) : Loggable {
  
  override val logTag = "Request_AllCountriesRepository"
  
  fun getData(): Observable<TotalData> {
    return createLoadingObservable()
  }
  
  private fun createLoadingObservable(): Observable<TotalData> {
    return Observable.concat(getFromCache(), getFromNetwork())
        .subscribeOn(schedulers.io())
        .firstElement()
        .toObservable()
        .share()
        .observeOn(schedulers.mainThread())
  }
  
  private fun getFromCache(): Observable<TotalData> = Observable.create { emitter ->
    val isUpToDate = saver.isUpToDate(COUNTRIES_LAST_UPDATE_TIME, MAX_CACHE_MINUTES)
    //    if (isUpToDate && sqLiteExecutor.isTableNotEmpty()) {
    //      emitter.onNext(sqLiteExecutor.getCountries())
    //      log { "Countries info found in cache" }
    //    }
    emitter.onComplete()
  }
  
  // TODO (6/13/2020): Add cache
  private fun getFromNetwork(): Observable<TotalData> {
    log { "getting countries" }
    return networker.requestObservable(URL)
        .map { transformJson(it) }
  }
  
  private fun transformJson(json: String): TotalData {
    val countriesList = ArrayList<Country>()
    val jsonObject = JSONObject(json)
    val jsonGlobalInfo = jsonObject.getJSONObject("Global")
    val generalInfo = GeneralInfo(
      jsonGlobalInfo.getString("TotalConfirmed").toInt(),
      jsonGlobalInfo.getString("TotalDeaths").toInt(),
      jsonGlobalInfo.getString("TotalRecovered").toInt()
    )
    val jsonArray = jsonObject.getJSONArray("Countries")
    for (i in 0 until jsonArray.length()) {
      val item = jsonArray.get(i) as JSONObject
      val country = Country(
        id = i,
        name = item.getString("Country"),
        slug = item.getString("Slug"),
        iso2 = item.getString("CountryCode"),
        confirmed = item.getString("TotalConfirmed").toInt(),
        deaths = item.getString("TotalDeaths").toInt(),
        recovered = item.getString("TotalRecovered").toInt()
      )
      countriesList.add(country)
    }
    return TotalData(countriesList, generalInfo)
  }
  
  companion object {
    
    const val SAVER_FILENAME = "AllCountriesRepository"
    
    private const val COUNTRIES_LAST_UPDATE_TIME = "countriesLastUpdateTime"
    private const val URL = "https://api.covid19api.com/summary"
  }
}