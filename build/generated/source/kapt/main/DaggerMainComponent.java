import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.AuthComponent;
import remotecontrolbackend.Main;
import remotecontrolbackend.Main_MembersInjector;
import remotecontrolbackend.dagger.DnsSdSubComponent;
import remotecontrolbackend.dagger.NettyModule_Companion_ProvideAuthHandlerFactory;
import remotecontrolbackend.dagger.NettyModule_Companion_ProvideRequestHandlerFactory;
import remotecontrolbackend.dagger.NettySubComponent;
import remotecontrolbackend.dagger.RequestHandlerSubComponent;
import remotecontrolbackend.dns_sd_part.DnsSdManager;
import remotecontrolbackend.netty_part.NettyConnectionManager;
import remotecontrolbackend.netty_part.NettyConnectionManager_MembersInjector;
import remotecontrolbackend.netty_part.auth_part.AbstractAuthHandler;
import remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler;
import remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler_MembersInjector;
import remotecontrolbackend.netty_part.auth_part.MockUserRepo;
import remotecontrolbackend.netty_part.auth_part.MockUserRepo_Factory;
import remotecontrolbackend.netty_part.request_handler_part.AbstractRequestHandler;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class DaggerMainComponent implements MainComponent {
  private final Integer setPort;

  private final Boolean isTestRun;

  private final DaggerMainComponent mainComponent = this;

  private DaggerMainComponent(Integer setPortParam, Boolean isTestRunParam) {
    this.setPort = setPortParam;
    this.isTestRun = isTestRunParam;

  }

  public static MainComponent.MainBuilder builder() {
    return new Builder();
  }

  private NettyConnectionManager nettyConnectionManager() {
    return NettySubcomponentModule_Companion_ProvideNettyManagerFactory.provideNettyManager(new NettySubComponentBuilder(mainComponent), setPort);
  }

  private DnsSdManager dnsSdManager() {
    return DnsSdSubcomponentModule_Companion_ProvideDnsSdManagerFactory.provideDnsSdManager(new DnsSdSubComponentBuilder(mainComponent), setPort);
  }

  @Override
  public void inject(Main mainClass) {
    injectMain(mainClass);
  }

  private Main injectMain(Main instance) {
    Main_MembersInjector.injectNettyConnectionManager(instance, nettyConnectionManager());
    Main_MembersInjector.injectDnsSdManager(instance, dnsSdManager());
    return instance;
  }

  private static final class Builder implements MainComponent.MainBuilder {
    private Integer setPort;

    private Boolean isTestRun;

    @Override
    public Builder setPort(int portN) {
      this.setPort = Preconditions.checkNotNull(portN);
      return this;
    }

    @Override
    public Builder isTestRun(boolean isTest) {
      this.isTestRun = Preconditions.checkNotNull(isTest);
      return this;
    }

    @Override
    public MainComponent buildMainComponent() {
      Preconditions.checkBuilderRequirement(setPort, Integer.class);
      Preconditions.checkBuilderRequirement(isTestRun, Boolean.class);
      return new DaggerMainComponent(setPort, isTestRun);
    }
  }

  private static final class NettySubComponentBuilder implements NettySubComponent.NettySubComponentBuilder {
    private final DaggerMainComponent mainComponent;

    private NettySubComponentBuilder(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;
    }

    @Override
    public NettySubComponent buildNettySubcomponent() {
      return new NettySubComponentImpl(mainComponent);
    }
  }

  private static final class AuthComponentBuilder implements AuthComponent.AuthBuilder {
    private final DaggerMainComponent mainComponent;

    private final NettySubComponentImpl nettySubComponentImpl;

    private AuthComponentBuilder(DaggerMainComponent mainComponent,
        NettySubComponentImpl nettySubComponentImpl) {
      this.mainComponent = mainComponent;
      this.nettySubComponentImpl = nettySubComponentImpl;
    }

    @Override
    public AuthComponent build() {
      return new AuthComponentImpl(mainComponent, nettySubComponentImpl);
    }
  }

  private static final class RequestHandlerSubComponentBuilder implements RequestHandlerSubComponent.RhBuilder {
    private final DaggerMainComponent mainComponent;

    private final NettySubComponentImpl nettySubComponentImpl;

    private RequestHandlerSubComponentBuilder(DaggerMainComponent mainComponent,
        NettySubComponentImpl nettySubComponentImpl) {
      this.mainComponent = mainComponent;
      this.nettySubComponentImpl = nettySubComponentImpl;
    }

    @Override
    public RequestHandlerSubComponent buildRh() {
      return new RequestHandlerSubComponentImpl(mainComponent, nettySubComponentImpl);
    }
  }

  private static final class DnsSdSubComponentBuilder implements DnsSdSubComponent.DnsSdSubComponentBuilder {
    private final DaggerMainComponent mainComponent;

    private DnsSdSubComponentBuilder(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;
    }

    @Override
    public DnsSdSubComponent buildDnsSdSubComp() {
      return new DnsSdSubComponentImpl(mainComponent);
    }
  }

  private static final class AuthComponentImpl implements AuthComponent {
    private final DaggerMainComponent mainComponent;

    private final NettySubComponentImpl nettySubComponentImpl;

    private final AuthComponentImpl authComponentImpl = this;

    private Provider<MockUserRepo> mockUserRepoProvider;

    private AuthComponentImpl(DaggerMainComponent mainComponent,
        NettySubComponentImpl nettySubComponentImpl) {
      this.mainComponent = mainComponent;
      this.nettySubComponentImpl = nettySubComponentImpl;

      initialize();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.mockUserRepoProvider = DoubleCheck.provider(MockUserRepo_Factory.create());
    }

    @Override
    public void inject(ConcreteAuthHandler authHandler) {
      injectConcreteAuthHandler(authHandler);
    }

    private ConcreteAuthHandler injectConcreteAuthHandler(ConcreteAuthHandler instance) {
      ConcreteAuthHandler_MembersInjector.injectUserRepo(instance, mockUserRepoProvider.get());
      return instance;
    }
  }

  private static final class RequestHandlerSubComponentImpl implements RequestHandlerSubComponent {
    private final DaggerMainComponent mainComponent;

    private final NettySubComponentImpl nettySubComponentImpl;

    private final RequestHandlerSubComponentImpl requestHandlerSubComponentImpl = this;

    private RequestHandlerSubComponentImpl(DaggerMainComponent mainComponent,
        NettySubComponentImpl nettySubComponentImpl) {
      this.mainComponent = mainComponent;
      this.nettySubComponentImpl = nettySubComponentImpl;


    }
  }

  private static final class NettySubComponentImpl implements NettySubComponent {
    private final DaggerMainComponent mainComponent;

    private final NettySubComponentImpl nettySubComponentImpl = this;

    private NettySubComponentImpl(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;


    }

    private AbstractAuthHandler abstractAuthHandler() {
      return NettyModule_Companion_ProvideAuthHandlerFactory.provideAuthHandler(mainComponent.isTestRun, new AuthComponentBuilder(mainComponent, nettySubComponentImpl));
    }

    private AbstractRequestHandler abstractRequestHandler() {
      return NettyModule_Companion_ProvideRequestHandlerFactory.provideRequestHandler(mainComponent.isTestRun, new RequestHandlerSubComponentBuilder(mainComponent, nettySubComponentImpl));
    }

    @Override
    public void inject(NettyConnectionManager nettyConnectionManager) {
      injectNettyConnectionManager(nettyConnectionManager);
    }

    private NettyConnectionManager injectNettyConnectionManager(NettyConnectionManager instance) {
      NettyConnectionManager_MembersInjector.injectAuthHandler(instance, abstractAuthHandler());
      NettyConnectionManager_MembersInjector.injectRequestHandler(instance, abstractRequestHandler());
      return instance;
    }
  }

  private static final class DnsSdSubComponentImpl implements DnsSdSubComponent {
    private final DaggerMainComponent mainComponent;

    private final DnsSdSubComponentImpl dnsSdSubComponentImpl = this;

    private DnsSdSubComponentImpl(DaggerMainComponent mainComponent) {
      this.mainComponent = mainComponent;


    }

    @Override
    public void inject(DnsSdManager dnsSdManager) {
    }
  }
}
