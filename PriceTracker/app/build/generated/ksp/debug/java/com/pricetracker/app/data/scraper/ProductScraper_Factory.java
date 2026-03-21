package com.pricetracker.app.data.scraper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class ProductScraper_Factory implements Factory<ProductScraper> {
  private final Provider<OkHttpClient> httpClientProvider;

  public ProductScraper_Factory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public ProductScraper get() {
    return newInstance(httpClientProvider.get());
  }

  public static ProductScraper_Factory create(Provider<OkHttpClient> httpClientProvider) {
    return new ProductScraper_Factory(httpClientProvider);
  }

  public static ProductScraper newInstance(OkHttpClient httpClient) {
    return new ProductScraper(httpClient);
  }
}
