import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.AuthComponent;
import remotecontrolbackend.auth_part.ConcreteAuthHandler;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class AuthSubcomponentModule_Companion_ProvideConcreteAuthHandlerFactory implements Factory<ConcreteAuthHandler> {
  private final Provider<AuthComponent> authComponentProvider;

  public AuthSubcomponentModule_Companion_ProvideConcreteAuthHandlerFactory(
      Provider<AuthComponent> authComponentProvider) {
    this.authComponentProvider = authComponentProvider;
  }

  @Override
  public ConcreteAuthHandler get() {
    return provideConcreteAuthHandler(authComponentProvider.get());
  }

  public static AuthSubcomponentModule_Companion_ProvideConcreteAuthHandlerFactory create(
      Provider<AuthComponent> authComponentProvider) {
    return new AuthSubcomponentModule_Companion_ProvideConcreteAuthHandlerFactory(authComponentProvider);
  }

  public static ConcreteAuthHandler provideConcreteAuthHandler(AuthComponent authComponent) {
    return Preconditions.checkNotNullFromProvides(AuthSubcomponentModule.Companion.provideConcreteAuthHandler(authComponent));
  }
}
