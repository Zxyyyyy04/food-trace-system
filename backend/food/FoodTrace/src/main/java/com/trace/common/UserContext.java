package com.trace.common;
import com.trace.dto.LoginDTO;

public class UserContext {
    private static final ThreadLocal<LoginDTO> USER_HOLDER = new ThreadLocal<>();
    public static void setUser(LoginDTO user) { USER_HOLDER.set(user); }
    public static LoginDTO getUser() { return USER_HOLDER.get(); }
    public static void clear() { USER_HOLDER.remove(); }
}