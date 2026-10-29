package com.trace.controller;

import com.trace.common.Result;
import com.trace.config.WebaseConfig;
import com.trace.dto.LoginDTO;
import com.trace.interceptor.AuthInterceptor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Api(tags = "登录认证模块")
public class AuthController {

    // 注入刚刚写好的配置类
    @Autowired
    private WebaseConfig webaseConfig;

    @PostMapping("/login")
    @ApiOperation("模拟登录 (根据 yml 配置的白名单校验)")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {

        System.out.println(loginDTO.getAddress());
        WebaseConfig.TestAccount matchedAccount = null;

        // 1. 遍历 yml 中配置的测试账户进行匹配
        for (WebaseConfig.TestAccount account : webaseConfig.getTestAccounts()) {
            // 同时校验用户名和地址是否匹配
            if (account.getName().equals(loginDTO.getName())
                    && account.getAddress().equalsIgnoreCase(loginDTO.getAddress())) {
                matchedAccount = account;
                break;
            }
        }

        // 2. 校验失败
        if (matchedAccount == null) {
            return Result.error("登录失败：用户名与 WeBASE 地址不匹配！");
        }

        // 3. 校验成功，组装真实的用户信息（防止前端篡改 Role）
        LoginDTO currentUser = new LoginDTO();
        currentUser.setName(matchedAccount.getName());
        currentUser.setAddress(matchedAccount.getAddress());
        currentUser.setRole(matchedAccount.getRole());

        // 4. 生成 Token 并缓存
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthInterceptor.TOKEN_CACHE.put(token, currentUser);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", currentUser);

        return Result.success(data);
    }
}