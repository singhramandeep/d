package com.pricetracker.app.data.repository;

import com.pricetracker.app.data.local.PriceHistoryDao;
import com.pricetracker.app.data.local.ProductDao;
import com.pricetracker.app.data.scraper.ScraperManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ProductRepository_Factory implements Factory<ProductRepository> {
  private final Provider<ProductDao> productDaoProvider;

  private final Provider<PriceHistoryDao> priceHistoryDaoProvider;

  private final Provider<ScraperManager> scraperManagerProvider;

  public ProductRepository_Factory(Provider<ProductDao> productDaoProvider,
      Provider<PriceHistoryDao> priceHistoryDaoProvider,
      Provider<ScraperManager> scraperManagerProvider) {
    this.productDaoProvider = productDaoProvider;
    this.priceHistoryDaoProvider = priceHistoryDaoProvider;
    this.scraperManagerProvider = scraperManagerProvider;
  }

  @Override
  public ProductRepository get() {
    return newInstance(productDaoProvider.get(), priceHistoryDaoProvider.get(), scraperManagerProvider.get());
  }

  public static ProductRepository_Factory create(Provider<ProductDao> productDaoProvider,
      Provider<PriceHistoryDao> priceHistoryDaoProvider,
      Provider<ScraperManager> scraperManagerProvider) {
    return new ProductRepository_Factory(productDaoProvider, priceHistoryDaoProvider, scraperManagerProvider);
  }

  public static ProductRepository newInstance(ProductDao productDao,
      PriceHistoryDao priceHistoryDao, ScraperManager scraperManager) {
    return new ProductRepository(productDao, priceHistoryDao, scraperManager);
  }
}
