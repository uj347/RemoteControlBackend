import remotecontrolbackend.dagger.NettySubComponent
import java.nio.file.Paths

class FileServiceTests {
    val nettySubcomponent: NettySubComponent = DaggerMainComponent
        .builder()
        .setWorkDirectory(Paths.get(TEST_DIRECTORY))
        .setPort(34444)
        .isTestRun(false)
        .isSSLEnabled(false)
        .isAuthEnabled(false)
        .buildMainComponent()
        .getNettySubcomponentBuilder()
        .buildNettySubcomponent()

}