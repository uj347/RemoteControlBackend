package remotecontrolbackend;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u000e\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007H&J\u0010\u0010\b\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\t"}, d2 = {"Lremotecontrolbackend/UserRepo;", "", "addUser", "", "user", "Lremotecontrolbackend/netty_part/auth_part/AllowedUser;", "getAlowedUsers", "", "removeUser", "CoroutineTest"})
@remotecontrolbackend.dagger.NettyScope
public abstract interface UserRepo {
    
    @org.jetbrains.annotations.NotNull
    public abstract java.util.List<remotecontrolbackend.netty_part.auth_part.AllowedUser> getAlowedUsers();
    
    public abstract void addUser(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.netty_part.auth_part.AllowedUser user);
    
    public abstract void removeUser(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.netty_part.auth_part.AllowedUser user);
}