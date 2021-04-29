package com.leyou.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

//申明这是一个配置类
@Configuration
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter() {
        //添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        //允许的域 只有配置的域才可以访问
        config.addAllowedOrigin("http://manage.leyou.com");
        //设置是否发送cookie
        config.setAllowCredentials(true);
        //设置允许的请求方式
        config.addAllowedMethod("*");
        //设置允许的头信息
        config.addAllowedHeader("*");

        //添加映射路径 拦截一切请求
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);

        return new CorsFilter(configurationSource);

    }



}
