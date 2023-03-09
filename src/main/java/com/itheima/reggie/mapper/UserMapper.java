package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author:zyc
 * @Date:2023-02-27-15:37
 * @Deacription:
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
