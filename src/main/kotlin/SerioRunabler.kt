import io.netty.handler.codec.base64.Base64Decoder
import io.netty.handler.codec.base64.Base64Encoder
import remotecontrolbackend.SerializableRunn
import java.io.*
import java.lang.reflect.Method
import java.util.*
fun main(){
    SerioRunabler.main()
}
class SerioRunabler {

    companion object {
        val serioRunable: SerializableRunn = SerializableRunn({ println("coco") })
        fun main() {
            val x: Method
            val decoder64 = Base64.getDecoder()
            val encoder64 = Base64.getEncoder()
            val byteOutput = ByteArrayOutputStream()
            val objectOut = ObjectOutputStream(byteOutput)

//            println("Coco before serialization")
//            serioRunable.run()
//            objectOut.writeObject(serioRunable)
//            val byteArr: ByteArray = byteOutput.toByteArray()
            val stringRepresentaion =
"rO0ABXNyACVyZW1vdGVjb250cm9sYmFja2VuZC5TZXJpYWxpemFibGVSdW5u4fjG5qHVZQgCAAFMAAdydW5GdW5jdAAgTGtvdGxpbi9qdm0vZnVuY3Rpb25zL0Z1bmN0aW9uMDt4cHNyACZTZXJpb1J1bmFibGVyJENvbXBhbmlvbiRzZXJpb1J1bmFibGUkMcOPzQm2OtyTAgAAeHIAGmtvdGxpbi5qdm0uaW50ZXJuYWwuTGFtYmRhkU4a8M/pOzcCAAFJAAVhcml0eXhwAAAAAA=="
//                encoder64.encodeToString(byteArr)
            println("Serialized coco represented in Base64: $stringRepresentaion")
            val decodedCoco = decoder64.decode(stringRepresentaion)

            val objectInput = ObjectInputStream(ByteArrayInputStream(decodedCoco))

            println("Deserialized coco")
            val deserialized = (objectInput.readObject()) as SerializableRunn
            deserialized.run()
        }
    }
}
