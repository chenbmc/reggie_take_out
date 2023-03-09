package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 继承了mybatics plus 对数据库的操作 都有
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
