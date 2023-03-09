package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.User;

/**
 * @Author:zyc
 * @Date:2023-02-27-15:34
 * @Deacription:
 */
public interface UserService extends IService<User> {
    //发送邮件
    //void sendMsg(String to,String subject,String text);

}
