package com.pricetracker.app.di;

import com.pricetracker.app.data.local.AppDatabase;
import com.pricetracker.app.data.local.ProductDao;
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
public final class AppModule_ProvideProductDaoFactory implements Factory<ProductDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideProductDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ProductDao get() {
    return provideProductDao(databaseProvider.get());
  }

  public static AppModule_ProvideProductDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideProductDaoFactory(databaseProvider);
  }

  public static ProductDao provideProductDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideProductDao(database));
  }
}
