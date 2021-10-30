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
public final class ConcreteCommandHandler_Factory implements Factory<ConcreteCommandHandler> {
  private final Provider<CommandInvoker> commandInvokerProvider;

  public ConcreteCommandHandler_Factory(Provider<CommandInvoker> commandInvokerProvider) {
    this.commandInvokerProvider = commandInvokerProvider;
  }

  @Override
  public ConcreteCommandHandler get() {
    return newInstance(commandInvokerProvider.get());
  }

  public static ConcreteCommandHandler_Factory create(
      Provider<CommandInvoker> commandInvokerProvider) {
    return new ConcreteCommandHandler_Factory(commandInvokerProvider);
  }

  public static ConcreteCommandHandler newInstance(CommandInvoker commandInvoker) {
    return new ConcreteCommandHandler(commandInvoker);
  }
}
