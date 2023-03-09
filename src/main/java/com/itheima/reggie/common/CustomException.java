package com.itheima.reggie.common;

/**
 * 自定义业务异常类，在判断分类是否删除时使用
 * @Author:zyc
 * @Date:2023-02-20-16:00
 * @Deacription:
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        //将提示信息传出
        super(message);
    }
}
