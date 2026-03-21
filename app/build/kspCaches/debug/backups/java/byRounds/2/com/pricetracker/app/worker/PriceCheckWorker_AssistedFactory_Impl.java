package com.pricetracker.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class PriceCheckWorker_AssistedFactory_Impl implements PriceCheckWorker_AssistedFactory {
  private final PriceCheckWorker_Factory delegateFactory;

  PriceCheckWorker_AssistedFactory_Impl(PriceCheckWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public PriceCheckWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<PriceCheckWorker_AssistedFactory> create(
      PriceCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PriceCheckWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<PriceCheckWorker_AssistedFactory> createFactoryProvider(
      PriceCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PriceCheckWorker_AssistedFactory_Impl(delegateFactory));
  }
}
