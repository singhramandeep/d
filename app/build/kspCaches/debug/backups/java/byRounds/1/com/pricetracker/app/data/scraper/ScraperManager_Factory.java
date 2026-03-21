package com.pricetracker.app.data.scraper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ScraperManager_Factory implements Factory<ScraperManager> {
  @Override
  public ScraperManager get() {
    return newInstance();
  }

  public static ScraperManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ScraperManager newInstance() {
    return new ScraperManager();
  }

  private static final class InstanceHolder {
    private static final ScraperManager_Factory INSTANCE = new ScraperManager_Factory();
  }
}
