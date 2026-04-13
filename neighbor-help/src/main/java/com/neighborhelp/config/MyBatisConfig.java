package com.neighborhelp.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 
 * 修复说明：
 * - 移除了手动创建的 SqlSessionFactory Bean，避免与 MyBatis-Plus 自动配置冲突
 * - 移除了 @MapperScan 注解，统一在主类 NeighborHelpApplication 中使用
 * - MyBatis-Plus 会自动配置 SqlSessionFactory，不需要手动创建
 * - 这样可以防止 factoryBeanObjectType 错误（Spring Boot 3.x 兼容性问题）
 * 
 * 如果需要 XML 映射文件，MyBatis-Plus 会自动从 application.yml 中的
 * mybatis-plus.mapper-locations 配置读取
 */
@Configuration
public class MyBatisConfig {
    // MyBatis-Plus 会自动配置，不需要手动创建 Bean
    // 所有配置都在 application.yml 中完成
}