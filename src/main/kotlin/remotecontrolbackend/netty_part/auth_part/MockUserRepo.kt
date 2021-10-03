package remotecontrolbackend.netty_part.auth_part

import remotecontrolbackend.AuthScope
import remotecontrolbackend.UserRepo
import javax.inject.Inject
@AuthScope
class MockUserRepo
@Inject
constructor():UserRepo {
    override fun getAlowedUsers(): List<AllowedUser> {
        return listOf(AllowedUser("test","test"))
    }

    override fun addUser(user: AllowedUser) {
        println("MockAdded: $ user")
    }

    override fun removeUser(user: AllowedUser) {
        println("MockRemoved: $ user")
    }
}