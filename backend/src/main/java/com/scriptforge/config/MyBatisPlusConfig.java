package com.scriptforge.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.scriptforge.mapper")
public class MyBatisPlusConfig {
}
