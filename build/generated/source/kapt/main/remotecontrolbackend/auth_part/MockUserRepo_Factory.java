package remotecontrolbackend.auth_part;

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
public final class MockUserRepo_Factory implements Factory<MockUserRepo> {
  @Override
  public MockUserRepo get() {
    return newInstance();
  }

  public static MockUserRepo_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockUserRepo newInstance() {
    return new MockUserRepo();
  }

  private static final class InstanceHolder {
    private static final MockUserRepo_Factory INSTANCE = new MockUserRepo_Factory();
  }
}
