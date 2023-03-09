package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j //记录日志

@Configuration//配置类必须加该注释
public class WebMvcConfig extends WebMvcConfigurationSupport {
    //进行静态资源映射，mvc框架默认但我们把图片等资源没有放到（static）文件夹下
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("进行静态资源映射。。");
        //**通配符，将该地址带有backend 映射到classpath（对应的是resources）这个地址
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }


    /**
     * 扩展mvc框架的消息转换器
     * 由于我们需要将Long型的数据转成String型，（定义的转换器 commom/JacksonObjectMapper），
     * 以至于消除自身转换器的精度损失而导致的数据修改失败
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        /**@Description: 扩展mvc框架的消息转换器,项目启动时即调用，
         */
        log.info("扩展消息转换器...");
        // 创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器，底层使用将jackson将java对象转化为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将上面的消息转化器对象追加到mvc的转化器集合中
        converters.add(0,messageConverter); // 将我们的转化器放在最前面，实现优先使用
                }

}
