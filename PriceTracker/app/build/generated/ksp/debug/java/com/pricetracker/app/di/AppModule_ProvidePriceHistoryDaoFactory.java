package com.pricetracker.app.di;

import com.pricetracker.app.data.db.PriceHistoryDao;
import com.pricetracker.app.data.db.PriceTrackerDatabase;
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
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvidePriceHistoryDaoFactory implements Factory<PriceHistoryDao> {
  private final Provider<PriceTrackerDatabase> databaseProvider;

  public AppModule_ProvidePriceHistoryDaoFactory(Provider<PriceTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PriceHistoryDao get() {
    return providePriceHistoryDao(databaseProvider.get());
  }

  public static AppModule_ProvidePriceHistoryDaoFactory create(
      Provider<PriceTrackerDatabase> databaseProvider) {
    return new AppModule_ProvidePriceHistoryDaoFactory(databaseProvider);
  }

  public static PriceHistoryDao providePriceHistoryDao(PriceTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePriceHistoryDao(database));
  }
}
