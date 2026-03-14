package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.entity.SysUser;
import com.logistics.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_HOURS = 24;

    /**
     * 根据钉钉ID获取或创建用户
     */
    public SysUser getOrCreateUser(String dingtalkId, String name, String phone) {
        SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDingtalkId, dingtalkId)
        );
        
        if (user == null) {
            user = new SysUser();
            user.setDingtalkId(dingtalkId);
            user.setName(name);
            user.setPhone(phone);
            user.setCreateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }
        return user;
    }

    /**
     * 生成登录Token
     */
    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 存入Redis
        redisTemplate.opsForValue().set(
            TOKEN_PREFIX + token,
            userId.toString(),
            TOKEN_EXPIRE_HOURS,
            TimeUnit.HOURS
        );
        
        // 更新数据库
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setToken(token);
            user.setTokenExpire(LocalDateTime.now().plusHours(TOKEN_EXPIRE_HOURS));
            sysUserMapper.updateById(user);
        }
        
        return token;
    }

    /**
     * 验证Token
     */
    public SysUser validateToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userIdStr == null) {
            return null;
        }
        
        Long userId = Long.parseLong(userIdStr);
        return sysUserMapper.selectById(userId);
    }

    /**
     * 登出（删除Token）
     */
    public void logout(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }
}