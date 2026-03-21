package com.pricetracker.app;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class PriceTrackerApp_MembersInjector implements MembersInjector<PriceTrackerApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public PriceTrackerApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<PriceTrackerApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new PriceTrackerApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(PriceTrackerApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.pricetracker.app.PriceTrackerApp.workerFactory")
  public static void injectWorkerFactory(PriceTrackerApp instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
