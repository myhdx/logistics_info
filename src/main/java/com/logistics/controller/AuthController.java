package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.entity.SysUser;
import com.logistics.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 钉钉免密登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String dingtalkId = params.get("dingtalkId");
        String name = params.get("name");
        String phone = params.get("phone");

        if (dingtalkId == null || name == null) {
            return Result.error("参数不完整");
        }

        // 获取或创建用户
        SysUser user = sysUserService.getOrCreateUser(dingtalkId, name, phone);

        // 生成Token
        String token = sysUserService.generateToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("name", user.getName());

        return Result.success(data);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        SysUser user = sysUserService.validateToken(token);

        if (user == null) {
            return Result.error("Token无效");
        }

        // 重新生成Token
        String newToken = sysUserService.generateToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);

        return Result.success(data);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        sysUserService.logout(token);
        return Result.success(null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<SysUser> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        SysUser user = sysUserService.validateToken(token);

        if (user == null) {
            return Result.error("未登录");
        }

        return Result.success(user);
    }
}