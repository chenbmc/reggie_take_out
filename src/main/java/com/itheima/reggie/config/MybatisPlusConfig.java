package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分页插件 由mybatics plus提供 在员工分页查询的时候使用
 * @Author:zyc
 * @Date:2023-02-16-15:24
 * @Deacription:
 */
@Configuration
@Slf4j

public class MybatisPlusConfig {

    //该注释提示 该插件由spring接管，
    //用第三方库中的类需要装配到 Spring容器时，则只能通过 @Bean来实现。
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        log.info("分页插件启动");
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
