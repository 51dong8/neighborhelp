package com.neighborhelp;

import com.neighborhelp.config.BaiduSpeechConfig;
import com.neighborhelp.config.OrderTimeoutProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 邻里帮帮后端服务启动类
 * 
 * 修复说明：
 * - 使用 @MapperScan 注解扫描 Mapper 接口，这是推荐的方式
 * - 避免使用 MapperScannerConfigurer Bean，防止 factoryBeanObjectType 错误
 * - @MapperScan 由 Spring 自动处理类型推断，不会出现字符串类型错误
 */
@SpringBootApplication
@MapperScan("com.neighborhelp.mapper")
@EnableConfigurationProperties({BaiduSpeechConfig.class, OrderTimeoutProperties.class})
@EnableScheduling
public class NeighborHelpApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeighborHelpApplication.class, args);
    }

}