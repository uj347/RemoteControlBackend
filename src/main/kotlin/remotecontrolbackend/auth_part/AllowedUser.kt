package remotecontrolbackend.auth_part

data class AllowedUser(val login:String,val password:String)

fun AllowedUser.getBase64Credentials():String{
    val credentials="${this.login}:${this.password}"
    return credentials.asciiToBase64()
}