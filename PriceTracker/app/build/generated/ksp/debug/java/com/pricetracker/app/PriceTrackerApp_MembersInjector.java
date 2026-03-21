package com.pricetracker.app;

import androidx.hilt.work.HiltWorkerFactory;
import com.pricetracker.app.service.PriceCheckScheduler;
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
    "KotlinInternalInJava",
    "cast"
})
public final class PriceTrackerApp_MembersInjector implements MembersInjector<PriceTrackerApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<PriceCheckScheduler> priceCheckSchedulerProvider;

  public PriceTrackerApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<PriceCheckScheduler> priceCheckSchedulerProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
    this.priceCheckSchedulerProvider = priceCheckSchedulerProvider;
  }

  public static MembersInjector<PriceTrackerApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<PriceCheckScheduler> priceCheckSchedulerProvider) {
    return new PriceTrackerApp_MembersInjector(workerFactoryProvider, priceCheckSchedulerProvider);
  }

  @Override
  public void injectMembers(PriceTrackerApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectPriceCheckScheduler(instance, priceCheckSchedulerProvider.get());
  }

  @InjectedFieldSignature("com.pricetracker.app.PriceTrackerApp.workerFactory")
  public static void injectWorkerFactory(PriceTrackerApp instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.pricetracker.app.PriceTrackerApp.priceCheckScheduler")
  public static void injectPriceCheckScheduler(PriceTrackerApp instance,
      PriceCheckScheduler priceCheckScheduler) {
    instance.priceCheckScheduler = priceCheckScheduler;
  }
}
