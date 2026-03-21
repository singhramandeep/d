package com.pricetracker.app.data.repository;

import com.pricetracker.app.data.db.PriceHistoryDao;
import com.pricetracker.app.data.db.ProductDao;
import com.pricetracker.app.data.scraper.ProductScraper;
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
    "KotlinInternalInJava",
    "cast"
})
public final class ProductRepository_Factory implements Factory<ProductRepository> {
  private final Provider<ProductDao> productDaoProvider;

  private final Provider<PriceHistoryDao> priceHistoryDaoProvider;

  private final Provider<ProductScraper> scraperProvider;

  public ProductRepository_Factory(Provider<ProductDao> productDaoProvider,
      Provider<PriceHistoryDao> priceHistoryDaoProvider, Provider<ProductScraper> scraperProvider) {
    this.productDaoProvider = productDaoProvider;
    this.priceHistoryDaoProvider = priceHistoryDaoProvider;
    this.scraperProvider = scraperProvider;
  }

  @Override
  public ProductRepository get() {
    return newInstance(productDaoProvider.get(), priceHistoryDaoProvider.get(), scraperProvider.get());
  }

  public static ProductRepository_Factory create(Provider<ProductDao> productDaoProvider,
      Provider<PriceHistoryDao> priceHistoryDaoProvider, Provider<ProductScraper> scraperProvider) {
    return new ProductRepository_Factory(productDaoProvider, priceHistoryDaoProvider, scraperProvider);
  }

  public static ProductRepository newInstance(ProductDao productDao,
      PriceHistoryDao priceHistoryDao, ProductScraper scraper) {
    return new ProductRepository(productDao, priceHistoryDao, scraper);
  }
}
