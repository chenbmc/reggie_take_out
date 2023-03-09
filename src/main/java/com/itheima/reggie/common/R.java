package com.itheima.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;
//通用返回结果，服务端响应的数据最终都会封装成此对象
//只要controller有返回值 ，最终都会封装成R对象，<T>泛型 根据返回的数据提高通用性
@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据
//成功方法，如 成功调用 将object即传入的employee对象 返回到data
    public static <T> R<T> success(T object) {
        //封装成R对象
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }
//失败传入的方法 ， 将传入的msg传到前端r.msg
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }
//操作动态数据
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
