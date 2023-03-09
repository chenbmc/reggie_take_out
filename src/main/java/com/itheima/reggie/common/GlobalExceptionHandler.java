package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 * @Author:zyc
 * @Date:2023-02-15-18:36
 * @Deacription:
 */
//指定RestController类型（在启动类中有注明）中的class ，如出现异常，则就在此处理
@ControllerAdvice(annotations = {RestController.class})
@ResponseBody //将结果封装成jason数据
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     */
    //指定该方法处理哪种异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> eceptionHandler(SQLIntegrityConstraintViolationException ex){
        //记录异常信息
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 自定义 异常处理，为了使信息 通过页面显示
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> eceptionHandler(CustomException ex){
        //记录异常信息
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
