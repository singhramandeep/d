package com.pricetracker.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.pricetracker.app.data.repository.ProductRepository;
import dagger.internal.DaggerGenerated;
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
public final class PriceCheckWorker_Factory {
  private final Provider<ProductRepository> repositoryProvider;

  public PriceCheckWorker_Factory(Provider<ProductRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public PriceCheckWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, repositoryProvider.get());
  }

  public static PriceCheckWorker_Factory create(Provider<ProductRepository> repositoryProvider) {
    return new PriceCheckWorker_Factory(repositoryProvider);
  }

  public static PriceCheckWorker newInstance(Context context, WorkerParameters workerParams,
      ProductRepository repository) {
    return new PriceCheckWorker(context, workerParams, repository);
  }
}
