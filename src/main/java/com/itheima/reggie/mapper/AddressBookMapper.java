package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.User;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author:zyc
 * @Date:2023-03-01-10:06
 * @Deacription:
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
