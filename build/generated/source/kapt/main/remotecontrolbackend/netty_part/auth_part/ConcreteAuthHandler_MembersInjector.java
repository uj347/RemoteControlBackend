package remotecontrolbackend.netty_part.auth_part;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.UserRepo;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class ConcreteAuthHandler_MembersInjector implements MembersInjector<ConcreteAuthHandler> {
  private final Provider<UserRepo> userRepoProvider;

  public ConcreteAuthHandler_MembersInjector(Provider<UserRepo> userRepoProvider) {
    this.userRepoProvider = userRepoProvider;
  }

  public static MembersInjector<ConcreteAuthHandler> create(Provider<UserRepo> userRepoProvider) {
    return new ConcreteAuthHandler_MembersInjector(userRepoProvider);
  }

  @Override
  public void injectMembers(ConcreteAuthHandler instance) {
    injectUserRepo(instance, userRepoProvider.get());
  }

  @InjectedFieldSignature("remotecontrolbackend.netty_part.auth_part.ConcreteAuthHandler.userRepo")
  public static void injectUserRepo(ConcreteAuthHandler instance, UserRepo userRepo) {
    instance.userRepo = userRepo;
  }
}
