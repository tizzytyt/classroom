package com.edu.classroom.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 小程序端 JSON.parse 对超过 2^53-1 的整数会精度丢失。
 * 将全部 Long 以字符串输出，避免试卷/用户等 UUID_SHORT 型主键在请求 URL 中出错。
 */
@Configuration
public class JacksonConfig {
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer longAsStringJson() {
    return builder -> builder
        .serializerByType(Long.class, ToStringSerializer.instance)
        .serializerByType(Long.TYPE, ToStringSerializer.instance);
  }
}
