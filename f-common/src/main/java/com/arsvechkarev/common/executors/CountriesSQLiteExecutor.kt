package com.arsvechkarev.common.executors

import android.content.ContentValues
import com.arsvechkarev.storage.CountriesTable
import com.arsvechkarev.storage.DatabaseExecutor
import com.arsvechkarev.storage.DatabaseManager
import com.arsvechkarev.storage.dao.CountriesDao
import core.model.Country

class CountriesSQLiteExecutor(
  private val countriesDao: CountriesDao
) {
  
  fun isTableNotEmpty(): Boolean {
    DatabaseManager.instance.readableDatabase.use {
      return DatabaseExecutor.isTableNotEmpty(it, CountriesTable.TABLE_NAME)
    }
  }
  
  fun getCountries(): List<Country> {
    val cursor = DatabaseExecutor.readAll(DatabaseManager.instance.readableDatabase,
      CountriesTable.TABLE_NAME)
    return countriesDao.getCountriesList(cursor)
  }
  
  fun saveCountriesInfo(list: List<Country>) {
    DatabaseManager.instance.writableDatabase.use { database ->
      val contentValues = ContentValues()
      for (country in list) {
        countriesDao.populateWithValues(country, contentValues)
        DatabaseExecutor.insertOrUpdate(
          database, CountriesTable.TABLE_NAME, CountriesTable.COLUMN_COUNTRY_ID,
          country.id, contentValues
        )
        contentValues.clear()
      }
    }
  }
}