import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.AuthComponent;
import remotecontrolbackend.auth_part.MockAuthHandler;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class AuthSubcomponentModule_Companion_ProvideMockAuthHandlerFactory implements Factory<MockAuthHandler> {
  private final Provider<AuthComponent> authComponentProvider;

  public AuthSubcomponentModule_Companion_ProvideMockAuthHandlerFactory(
      Provider<AuthComponent> authComponentProvider) {
    this.authComponentProvider = authComponentProvider;
  }

  @Override
  public MockAuthHandler get() {
    return provideMockAuthHandler(authComponentProvider.get());
  }

  public static AuthSubcomponentModule_Companion_ProvideMockAuthHandlerFactory create(
      Provider<AuthComponent> authComponentProvider) {
    return new AuthSubcomponentModule_Companion_ProvideMockAuthHandlerFactory(authComponentProvider);
  }

  public static MockAuthHandler provideMockAuthHandler(AuthComponent authComponent) {
    return Preconditions.checkNotNullFromProvides(AuthSubcomponentModule.Companion.provideMockAuthHandler(authComponent));
  }
}
