package remotecontrolbackend;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.auth_part.AbstractAuthHandler;
import remotecontrolbackend.request_handler_part.AbstractRequestHandler;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class NettyKotlinShit_MembersInjector implements MembersInjector<NettyKotlinShit> {
  private final Provider<AbstractAuthHandler> authHandlerProvider;

  private final Provider<AbstractRequestHandler> requestHandlerProvider;

  public NettyKotlinShit_MembersInjector(Provider<AbstractAuthHandler> authHandlerProvider,
      Provider<AbstractRequestHandler> requestHandlerProvider) {
    this.authHandlerProvider = authHandlerProvider;
    this.requestHandlerProvider = requestHandlerProvider;
  }

  public static MembersInjector<NettyKotlinShit> create(
      Provider<AbstractAuthHandler> authHandlerProvider,
      Provider<AbstractRequestHandler> requestHandlerProvider) {
    return new NettyKotlinShit_MembersInjector(authHandlerProvider, requestHandlerProvider);
  }

  @Override
  public void injectMembers(NettyKotlinShit instance) {
    injectAuthHandler(instance, authHandlerProvider.get());
    injectRequestHandler(instance, requestHandlerProvider.get());
  }

  @InjectedFieldSignature("remotecontrolbackend.NettyKotlinShit.authHandler")
  public static void injectAuthHandler(NettyKotlinShit instance, AbstractAuthHandler authHandler) {
    instance.authHandler = authHandler;
  }

  @InjectedFieldSignature("remotecontrolbackend.NettyKotlinShit.requestHandler")
  public static void injectRequestHandler(NettyKotlinShit instance,
      AbstractRequestHandler requestHandler) {
    instance.requestHandler = requestHandler;
  }
}
