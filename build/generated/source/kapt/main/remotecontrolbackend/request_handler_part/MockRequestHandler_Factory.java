package remotecontrolbackend.request_handler_part;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import javax.annotation.processing.Generated;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class MockRequestHandler_Factory implements Factory<MockRequestHandler> {
  @Override
  public MockRequestHandler get() {
    return newInstance();
  }

  public static MockRequestHandler_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockRequestHandler newInstance() {
    return new MockRequestHandler();
  }

  private static final class InstanceHolder {
    private static final MockRequestHandler_Factory INSTANCE = new MockRequestHandler_Factory();
  }
}
