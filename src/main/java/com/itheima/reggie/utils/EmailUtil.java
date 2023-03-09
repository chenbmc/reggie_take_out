package com.itheima.reggie.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * 第二个工具类是邮箱对应发送信息使用的,参数1: 发送的邮箱,就是登录时你填的邮箱,参数2: 填写第一个工具类的随机生成的验证码
 */
public class EmailUtil {
    public static void sendAuthCodeEmail(String email, String authCode) {
        try {
            SimpleEmail mail = new SimpleEmail();
            mail.setHostName("smtp.qq.com");//发送邮件的服务器,这个是qq邮箱的，不用修改
            mail.setAuthentication("710067550@qq.com", "fzgllmhjnaawbfda");//第一个参数是对应的邮箱用户名一般就是自己的邮箱第二个参数就是SMTP的密码,我们上面获取过了
            mail.setFrom("710067550@qq.com","瑞吉外卖");  //发送邮件的邮箱和发件人
            mail.setSSLOnConnect(true); //使用安全链接
            mail.addTo(email);//接收的邮箱
            mail.setSubject("验证码");//设置邮件的主题
            mail.setMsg("尊敬的用户:你好!\n 登陆验证码为:" + authCode+"\n"+"     (有效期为一分钟)");//设置邮件的内容
            mail.send();//发送
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}