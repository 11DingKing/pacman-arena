package com.pacman.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pacman.entity.User;
import com.pacman.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动时确保默认管理员账号 admin 的密码为 admin123（与登录页提示一致）。
 * 若数据库中 admin 的密码哈希与 admin123 不匹配则自动更新，避免 schema 中错误哈希导致无法登录。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminInitRunner implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_DEFAULT_PASSWORD = "admin123";

    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            User admin = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, ADMIN_USERNAME));
            if (admin == null) {
                return;
            }
            if (BCrypt.checkpw(ADMIN_DEFAULT_PASSWORD, admin.getPassword())) {
                return;
            }
            admin.setPassword(BCrypt.hashpw(ADMIN_DEFAULT_PASSWORD));
            admin.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(admin);
            log.info("已修复默认管理员 {} 的密码为提示的默认密码", ADMIN_USERNAME);
        } catch (Exception e) {
            log.warn("初始化管理员密码时出错（可忽略，若使用默认账号无法登录请检查数据库）: {}", e.getMessage());
        }
    }
}
