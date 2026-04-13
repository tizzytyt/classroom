package com.edu.classroom.boot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitRunner implements CommandLineRunner {
  private final JdbcTemplate jdbcTemplate;

  public DataInitRunner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(String... args) {
    // 兼容：若历史数据曾用 BCrypt 写入 password_hash，这里统一改回明文默认值
    // 说明：当前 AuthService 为明文校验，因此 BCrypt 哈希会导致无法使用明文密码登录
    try {
      jdbcTemplate.update("update sys_user set password_hash='123456' where password_hash like '$2%'");
    } catch (Exception e) {
      // ignore
    }
  }
}
