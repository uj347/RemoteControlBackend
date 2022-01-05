package com.uj.rcbackend

import com.uj.rcbackend.dagger.NettyScope
import com.uj.rcbackend.nettypart.authpart.AllowedUser


@NettyScope
interface UserRepo {
    fun getAlowedUsers():List<AllowedUser>
    fun addUser(user: AllowedUser)
    fun removeUser(user: AllowedUser)
}
