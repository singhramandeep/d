package com.pricetracker.app.di;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class WorkManagerConfig_Factory implements Factory<WorkManagerConfig> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public WorkManagerConfig_Factory(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  @Override
  public WorkManagerConfig get() {
    return newInstance(workerFactoryProvider.get());
  }

  public static WorkManagerConfig_Factory create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new WorkManagerConfig_Factory(workerFactoryProvider);
  }

  public static WorkManagerConfig newInstance(HiltWorkerFactory workerFactory) {
    return new WorkManagerConfig(workerFactory);
  }
}
