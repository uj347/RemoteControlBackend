package remotecontrolbackend

import remotecontrolbackend.dagger.NettyScope
import remotecontrolbackend.netty_part.auth_part.AllowedUser


@NettyScope
interface UserRepo {
    fun getAlowedUsers():List<AllowedUser>
    fun addUser(user: AllowedUser)
    fun removeUser(user: AllowedUser)
}
