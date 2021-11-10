package IntrestingTests

import dagger.Component
import dagger.Module
import dagger.Provides
import okio.buffer
import okio.source
import java.nio.file.Paths
import javax.inject.Inject
fun main(){
    val path= Paths.get("j:/")
    val source=path.source().buffer()
}