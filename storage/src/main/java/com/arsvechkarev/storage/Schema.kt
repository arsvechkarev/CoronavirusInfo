package com.arsvechkarev.storage

import android.provider.BaseColumns

object CountriesTable : BaseColumns {
  const val TABLE_NAME = "main_info"
  const val COLUMN_COUNTRY_ID = "_id"
  const val COLUMN_COUNTRY_NAME = "name"
  const val COLUMN_COUNTRY_SLUG = "slug"
  const val COLUMN_COUNTRY_CODE = "iso2"
  const val COLUMN_CONFIRMED = "confirmed"
  const val COLUMN_DEATHS = "deaths"
  const val COLUMN_RECOVERED = "recovered"
}

object PopulationsTable {
  const val TABLE_NAME = "populations"
  const val COLUMN_ISO2 = "iso2"
  const val COLUMN_POPULATION = "population"
}