package com.pricetracker.app.di;

import com.pricetracker.app.data.db.PriceTrackerDatabase;
import com.pricetracker.app.data.db.ProductDao;
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
public final class AppModule_ProvideProductDaoFactory implements Factory<ProductDao> {
  private final Provider<PriceTrackerDatabase> databaseProvider;

  public AppModule_ProvideProductDaoFactory(Provider<PriceTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ProductDao get() {
    return provideProductDao(databaseProvider.get());
  }

  public static AppModule_ProvideProductDaoFactory create(
      Provider<PriceTrackerDatabase> databaseProvider) {
    return new AppModule_ProvideProductDaoFactory(databaseProvider);
  }

  public static ProductDao provideProductDao(PriceTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideProductDao(database));
  }
}
