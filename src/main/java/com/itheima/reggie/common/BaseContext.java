package com.itheima.reggie.common;

import javax.servlet.http.PushBuilder;

/**
 * 基于ThreadLocal封装的工具类，封装get set方法，解决MeataObjectHandler 中无法从session获取用户id问题
 * @Author:zyc
 * @Date:2023-02-20-10:10
 * @Deacription:
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //工具类方法 注明Static，设置值
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    //获取值
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
