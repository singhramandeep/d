package com.pricetracker.app.di;

import com.pricetracker.app.data.local.AppDatabase;
import com.pricetracker.app.data.local.PriceHistoryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvidePriceHistoryDaoFactory implements Factory<PriceHistoryDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvidePriceHistoryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PriceHistoryDao get() {
    return providePriceHistoryDao(databaseProvider.get());
  }

  public static AppModule_ProvidePriceHistoryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvidePriceHistoryDaoFactory(databaseProvider);
  }

  public static PriceHistoryDao providePriceHistoryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePriceHistoryDao(database));
  }
}
