package com.qiaoqiao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF，因为我们使用自定义的登录系统
                .csrf().disable()

                // 允许所有请求访问，我们将使用自定义的身份验证
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().permitAll()
                .and()

                // 禁用Spring Security的表单登录
                .formLogin().disable()

                // 禁用HTTP Basic认证
                .httpBasic().disable()

                // 允许H2控制台访问（仅用于开发环境）
                .headers()
                .frameOptions().sameOrigin();
    }

    // 密码加密器
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}