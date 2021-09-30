import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.AuthComponent;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class AuthSubcomponentModule_Companion_ProvideAuthComponentFactory implements Factory<AuthComponent> {
  private final Provider<AuthComponent.AuthBuilder> authComponentBuilderProvider;

  public AuthSubcomponentModule_Companion_ProvideAuthComponentFactory(
      Provider<AuthComponent.AuthBuilder> authComponentBuilderProvider) {
    this.authComponentBuilderProvider = authComponentBuilderProvider;
  }

  @Override
  public AuthComponent get() {
    return provideAuthComponent(authComponentBuilderProvider.get());
  }

  public static AuthSubcomponentModule_Companion_ProvideAuthComponentFactory create(
      Provider<AuthComponent.AuthBuilder> authComponentBuilderProvider) {
    return new AuthSubcomponentModule_Companion_ProvideAuthComponentFactory(authComponentBuilderProvider);
  }

  public static AuthComponent provideAuthComponent(AuthComponent.AuthBuilder authComponentBuilder) {
    return Preconditions.checkNotNullFromProvides(AuthSubcomponentModule.Companion.provideAuthComponent(authComponentBuilder));
  }
}
