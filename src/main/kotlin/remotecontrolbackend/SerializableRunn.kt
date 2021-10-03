package remotecontrolbackend

 class SerializableRunn(val runFunc:()->Unit):Runnable,java.io.Serializable {
     override fun run()=runFunc.invoke()
 }