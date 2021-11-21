package IntrestingTests

import dagger.Component
import dagger.Module
import dagger.Provides
import okio.buffer
import okio.source
import remotecontrolbackend.ROOT_DIR
import java.nio.file.Paths
import javax.inject.Inject
fun main(){
    val path= Paths.get(ROOT_DIR)
    val source=path.source().buffer()
}