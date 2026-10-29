package com.trace.interceptor;

import com.alibaba.fastjson.JSON;
import com.trace.common.Result;
import com.trace.common.UserContext;
import com.trace.dto.LoginDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final Map<String, LoginDTO> TOKEN_CACHE = new HashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        LoginDTO user = TOKEN_CACHE.get(token);
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(Result.unauthorized("未登录或 Token 已过期")));
            return false;
        }
        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}