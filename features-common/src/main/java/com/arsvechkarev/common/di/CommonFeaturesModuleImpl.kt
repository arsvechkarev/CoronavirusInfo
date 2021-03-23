package com.arsvechkarev.common.di

import com.arsvechkarev.common.domain.CountriesMetaInfoRepository
import com.arsvechkarev.common.domain.CountriesMetaInfoRepositoryImpl
import com.arsvechkarev.common.domain.CountriesMetaInfoRepositoryImpl.Companion.DATABASE_NAME
import com.arsvechkarev.common.domain.CountriesMetaInfoRepositoryImpl.Companion.DATABASE_VERSION
import com.arsvechkarev.common.domain.TotalInfoDataSource
import com.arsvechkarev.common.domain.TotalInfoDataSourceImpl
import core.di.CoreModule

class CommonFeaturesModuleImpl(
  coreModule: CoreModule
) : CommonFeaturesModule {
  
  override val totalInfoDataSource: TotalInfoDataSource =
      TotalInfoDataSourceImpl(coreModule.webApi)
  
  override val countriesMetaInfoRepository: CountriesMetaInfoRepository by lazy {
    val database = coreModule.databaseCreator.provideDatabase(DATABASE_NAME, DATABASE_VERSION)
    CountriesMetaInfoRepositoryImpl(database)
  }
}