package remotecontrolbackend

import remotecontrolbackend.auth_part.AllowedUser
@AuthScope
interface UserRepo {
    fun getAlowedUsers():List<AllowedUser>
    fun addUser(user: AllowedUser)
    fun removeUser(user: AllowedUser)
}
