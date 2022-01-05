package com.uj.rcbackend.nettypart.authpart


import com.uj.rcbackend.UserRepo
import com.uj.rcbackend.dagger.NettyScope
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