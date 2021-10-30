package remotecontrolbackend.netty_part.command_handler_part;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import remotecontrolbackend.command_invoker_part.command_invoker.CommandInvoker;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class MockCommandHandler_Factory implements Factory<MockCommandHandler> {
  private final Provider<CommandInvoker> commandInvokerProvider;

  public MockCommandHandler_Factory(Provider<CommandInvoker> commandInvokerProvider) {
    this.commandInvokerProvider = commandInvokerProvider;
  }

  @Override
  public MockCommandHandler get() {
    return newInstance(commandInvokerProvider.get());
  }

  public static MockCommandHandler_Factory create(Provider<CommandInvoker> commandInvokerProvider) {
    return new MockCommandHandler_Factory(commandInvokerProvider);
  }

  public static MockCommandHandler newInstance(CommandInvoker commandInvoker) {
    return new MockCommandHandler(commandInvoker);
  }
}
