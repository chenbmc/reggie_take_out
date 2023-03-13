package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author:zyc
 * @Date:2023-02-20-9:53
 * @Deacription:
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作， 自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject){
        log.info("公共字段自动填充");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        //虽然用户成功后 我们将用户id存入HttpSession中，但该类无法获得HttpSession对象
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新操作 自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {

        long id = Thread.currentThread().getId();
        log.info("线程id：{}",id);

        log.info("公共字段自动填充");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

}
