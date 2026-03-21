package com.pricetracker.app.presentation.screens.add;

import com.pricetracker.app.data.repository.ProductRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AddProductViewModel_Factory implements Factory<AddProductViewModel> {
  private final Provider<ProductRepository> repositoryProvider;

  public AddProductViewModel_Factory(Provider<ProductRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddProductViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddProductViewModel_Factory create(Provider<ProductRepository> repositoryProvider) {
    return new AddProductViewModel_Factory(repositoryProvider);
  }

  public static AddProductViewModel newInstance(ProductRepository repository) {
    return new AddProductViewModel(repository);
  }
}
