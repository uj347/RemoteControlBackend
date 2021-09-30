package remotecontrolbackend.auth_part;

import java.lang.System;

@kotlin.Metadata(mv = {1, 5, 1}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u000e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\bH\u0016J\u0010\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\n"}, d2 = {"Lremotecontrolbackend/auth_part/MockUserRepo;", "Lremotecontrolbackend/UserRepo;", "()V", "addUser", "", "user", "Lremotecontrolbackend/auth_part/AllowedUser;", "getAlowedUsers", "", "removeUser", "CoroutineTest"})
@remotecontrolbackend.AuthScope
public final class MockUserRepo implements remotecontrolbackend.UserRepo {
    
    @javax.inject.Inject
    public MockUserRepo() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @java.lang.Override
    public java.util.List<remotecontrolbackend.auth_part.AllowedUser> getAlowedUsers() {
        return null;
    }
    
    @java.lang.Override
    public void addUser(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.auth_part.AllowedUser user) {
    }
    
    @java.lang.Override
    public void removeUser(@org.jetbrains.annotations.NotNull
    remotecontrolbackend.auth_part.AllowedUser user) {
    }
}