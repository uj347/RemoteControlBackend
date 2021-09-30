import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.AuthComponent;
import remotecontrolbackend.NettyKotlinShit;
import remotecontrolbackend.NettyKotlinShit_MembersInjector;
import remotecontrolbackend.auth_part.ConcreteAuthHandler;
import remotecontrolbackend.auth_part.ConcreteAuthHandler_MembersInjector;
import remotecontrolbackend.auth_part.MockAuthHandler;
import remotecontrolbackend.auth_part.MockUserRepo;
import remotecontrolbackend.auth_part.MockUserRepo_Factory;
import remotecontrolbackend.request_handler_part.MockRequestHandler;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class DaggerMainComponent extends MainComponent {
  private final DaggerMainComponent mainComponent = this;

  private DaggerMainComponent() {


  }

  public static Builder builder() {
    return new Builder();
  }

  public static MainComponent create() {
    return new Builder().build();
  }

  private AuthComponent authComponent() {
    return AuthSubcomponentModule_Companion_ProvideAuthComponentFactory.provideAuthComponent(new AuthComponentBuilder(mainComponent));
  }

  private MockAuthHandler mockAuthHandler() {
    return AuthSubcomponentModule_Companion_ProvideMockAuthHandlerFactory.provideMockAuthHandler(authComponent());
  }

  @Override
  public void inject(NettyKotlinShit mainClass) {
    injectNettyKotlinShit(mainClass);
  }

  private NettyKotlinShit injectNettyKotlinShit(NettyKotlinShit instance) {
    NettyKotlinShit_MembersInjector.injectAuthHandler(instance, mockAuthHandler());
    NettyKotlinShit_MembersInjector.injectRequestHandler(instance, new MockRequestHandler());
    return instance;
  }

  public static final class Builder {
    private Builder() {
    }

    public MainComponent build() {
      return new DaggerMainComponent();
    }
  }

  private static final class AuthComponentBuilder implements AuthComponent.AuthBuilder {
    private final DaggerMainComponent mainComponent;

    private AuthComponentBuilder(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;
    }

    @Override
    public AuthComponent build() {
      return new AuthComponentImpl(mainComponent);
    }
  }

  private static final class AuthComponentImpl implements AuthComponent {
    private final DaggerMainComponent mainComponent;

    private final AuthComponentImpl authComponentImpl = this;

    private Provider<MockUserRepo> mockUserRepoProvider;

    private AuthComponentImpl(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;

      initialize();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.mockUserRepoProvider = DoubleCheck.provider(MockUserRepo_Factory.create());
    }

    @Override
    public void inject(ConcreteAuthHandler arg0) {
      injectConcreteAuthHandler(arg0);
    }

    private ConcreteAuthHandler injectConcreteAuthHandler(ConcreteAuthHandler instance) {
      ConcreteAuthHandler_MembersInjector.injectUserRepo(instance, mockUserRepoProvider.get());
      return instance;
    }
  }
}
