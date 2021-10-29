package remotecontrolbackend.netty_part.auth_part


import remotecontrolbackend.UserRepo
import remotecontrolbackend.dagger.NettyScope
import javax.inject.Inject
@NettyScope
class MockUserRepo
@Inject constructor():UserRepo {
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